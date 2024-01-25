package hudson.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import com.thoughtworks.xstream.XStream;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.Util;
import hudson.XmlFile;
import hudson.cli.declarative.CLIResolver;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.listeners.SaveableListener;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.FoldableAction;
import hudson.model.queue.MappingWorksheet;
import hudson.model.queue.QueueSorter;
import hudson.model.queue.QueueTaskDispatcher;
import hudson.model.queue.ScheduleResult;
import hudson.model.queue.SubTask;
import hudson.model.queue.WorkUnit;
import hudson.model.queue.WorkUnitContext;
import hudson.remoting.Callable;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.util.ConsistentHash;
import hudson.util.Iterators;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.AtmostOneTaskExecutor;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;

@ExportedBean
@BridgeMethodsAdded
public class Queue extends ResourceController implements Saveable {
  private final Set<WaitingItem> waitingList;
  
  private final ItemList<BlockedItem> blockedProjects;
  
  private final ItemList<BuildableItem> buildables;
  
  private final ItemList<BuildableItem> pendings;
  
  private final Cache<Long, LeftItem> leftItems;
  
  private final AtmostOneTaskExecutor<Void> maintainerThread;
  
  private final ReentrantLock lock;
  
  private final Condition condition;
  
  public Queue(@NonNull LoadBalancer loadBalancer) {
    this.waitingList = new TreeSet();
    this.blockedProjects = new ItemList(this);
    this.buildables = new ItemList(this);
    this.pendings = new ItemList(this);
    this.snapshot = new Snapshot(this.waitingList, this.blockedProjects, this.buildables, this.pendings);
    this.leftItems = CacheBuilder.newBuilder().expireAfterWrite(300L, TimeUnit.SECONDS).build();
    this.maintainerThread = new AtmostOneTaskExecutor(new Object(this));
    this.lock = new ReentrantLock();
    this.condition = this.lock.newCondition();
    this.loadBalancer = loadBalancer.sanitize();
    (new MaintainTask(this)).periodic();
  }
  
  public LoadBalancer getLoadBalancer() { return this.loadBalancer; }
  
  public void setLoadBalancer(@NonNull LoadBalancer loadBalancer) { this.loadBalancer = loadBalancer.sanitize(); }
  
  public QueueSorter getSorter() { return this.sorter; }
  
  public void setSorter(QueueSorter sorter) { this.sorter = sorter; }
  
  public void load() {
    this.lock.lock();
    try {
      try {
        this.waitingList.clear();
        this.blockedProjects.clear();
        this.buildables.clear();
        this.pendings.clear();
        File queueFile = getXMLQueueFile();
        if (Files.exists(queueFile.toPath(), new java.nio.file.LinkOption[0])) {
          List items;
          Object unmarshaledObj = (new XmlFile(XSTREAM, queueFile)).read();
          if (unmarshaledObj instanceof State) {
            State state = (State)unmarshaledObj;
            items = state.items;
            WaitingItem.COUNTER.set(state.counter);
          } else {
            items = (List)unmarshaledObj;
            long maxId = 0L;
            for (Object o : items) {
              if (o instanceof Item)
                maxId = Math.max(maxId, ((Item)o).id); 
            } 
            WaitingItem.COUNTER.set(maxId);
          } 
          for (Object o : items) {
            if (o instanceof Task) {
              schedule((Task)o, 0);
              continue;
            } 
            if (o instanceof Item) {
              Item item = (Item)o;
              if (item.task == null)
                continue; 
              if (item instanceof WaitingItem) {
                item.enter(this);
                continue;
              } 
              if (item instanceof BlockedItem) {
                item.enter(this);
                continue;
              } 
              if (item instanceof BuildableItem) {
                item.enter(this);
                continue;
              } 
              throw new IllegalStateException("Unknown item type! " + item);
            } 
          } 
          File bk = new File(queueFile.getPath() + ".bak");
          Files.move(queueFile.toPath(), bk.toPath(), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
        } 
      } catch (IOException|java.nio.file.InvalidPathException e) {
        LOGGER.log(Level.WARNING, "Failed to load the queue file " + getXMLQueueFile(), e);
      } finally {
        updateSnapshot();
      } 
    } finally {
      this.lock.unlock();
    } 
  }
  
  public void save() {
    if (BulkChange.contains(this))
      return; 
    if (Jenkins.getInstanceOrNull() == null)
      return; 
    XmlFile queueFile = new XmlFile(XSTREAM, getXMLQueueFile());
    this.lock.lock();
    try {
      State state = new State();
      state.counter = WaitingItem.COUNTER.longValue();
      for (Item item : getItems()) {
        if (!(item.task instanceof TransientTask))
          state.items.add(item); 
      } 
      try {
        queueFile.write(state);
      } catch (IOException e) {
        LOGGER.log((e instanceof java.nio.channels.ClosedByInterruptException) ? Level.FINE : Level.WARNING, "Failed to write out the queue file " + getXMLQueueFile(), e);
      } 
    } finally {
      this.lock.unlock();
    } 
    SaveableListener.fireOnChange(this, queueFile);
  }
  
  public void clear() {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    this.lock.lock();
    try {
      try {
        for (WaitingItem i : new ArrayList(this.waitingList))
          i.cancel(this); 
        this.blockedProjects.cancelAll();
        this.pendings.cancelAll();
        this.buildables.cancelAll();
      } finally {
        updateSnapshot();
      } 
    } finally {
      this.lock.unlock();
    } 
    scheduleMaintenance();
  }
  
  File getXMLQueueFile() {
    String id = SystemProperties.getString(Queue.class.getName() + ".id");
    if (id != null)
      return new File(Jenkins.get().getRootDir(), "queue/" + id + ".xml"); 
    return new File(Jenkins.get().getRootDir(), "queue.xml");
  }
  
  @Deprecated
  public boolean add(AbstractProject p) { return (schedule(p) != null); }
  
  @CheckForNull
  public WaitingItem schedule(AbstractProject p) { return schedule(p, p.getQuietPeriod()); }
  
  @Deprecated
  public boolean add(AbstractProject p, int quietPeriod) { return (schedule(p, quietPeriod) != null); }
  
  @Deprecated
  public WaitingItem schedule(Task p, int quietPeriod, List<Action> actions) { return schedule2(p, quietPeriod, actions).getCreateItem(); }
  
  @NonNull
  public ScheduleResult schedule2(Task p, int quietPeriod, List<Action> actions) {
    actions = new ArrayList<Action>(actions);
    actions.removeIf(Objects::isNull);
    this.lock.lock();
    try {
      try {
        for (QueueDecisionHandler h : QueueDecisionHandler.all()) {
          if (!h.shouldSchedule(p, actions))
            return ScheduleResult.refused(); 
        } 
        return scheduleInternal(p, quietPeriod, actions);
      } finally {
        updateSnapshot();
      } 
    } finally {
      this.lock.unlock();
    } 
  }
  
  @NonNull
  private ScheduleResult scheduleInternal(Task p, int quietPeriod, List<Action> actions) {
    this.lock.lock();
    try {
      try {
        Calendar due = new GregorianCalendar();
        due.add(13, quietPeriod);
        List<Item> duplicatesInQueue = new ArrayList<Item>();
        for (Item item : liveGetItems(p)) {
          boolean shouldScheduleItem = false;
          for (QueueAction action : item.getActions(QueueAction.class))
            shouldScheduleItem |= action.shouldSchedule(actions); 
          for (QueueAction action : Util.filter(actions, QueueAction.class))
            shouldScheduleItem |= action.shouldSchedule(new ArrayList(item.getAllActions())); 
          if (!shouldScheduleItem)
            duplicatesInQueue.add(item); 
        } 
        if (duplicatesInQueue.isEmpty()) {
          LOGGER.log(Level.FINE, "{0} added to queue", p);
          WaitingItem added = new WaitingItem(due, p, actions);
          added.enter(this);
          scheduleMaintenance();
          return ScheduleResult.created(added);
        } 
        LOGGER.log(Level.FINE, "{0} is already in the queue", p);
        for (Item item : duplicatesInQueue) {
          for (FoldableAction a : Util.filter(actions, FoldableAction.class)) {
            a.foldIntoExisting(item, p, actions);
            if (LOGGER.isLoggable(Level.FINE))
              LOGGER.log(Level.FINE, "after folding {0}, {1} includes {2}", new Object[] { a, item, item.getAllActions() }); 
          } 
        } 
        boolean queueUpdated = false;
        for (WaitingItem wi : Util.filter(duplicatesInQueue, WaitingItem.class)) {
          if (wi.timestamp.before(due))
            continue; 
          wi.leave(this);
          wi.timestamp = due;
          wi.enter(this);
          queueUpdated = true;
        } 
        if (queueUpdated)
          scheduleMaintenance(); 
        return ScheduleResult.existing((Item)duplicatesInQueue.get(0));
      } finally {
        updateSnapshot();
      } 
    } finally {
      this.lock.unlock();
    } 
  }
  
  @Deprecated
  public boolean add(Task p, int quietPeriod) { return (schedule(p, quietPeriod) != null); }
  
  @CheckForNull
  public WaitingItem schedule(Task p, int quietPeriod) { return schedule(p, quietPeriod, new Action[0]); }
  
  @Deprecated
  public boolean add(Task p, int quietPeriod, Action... actions) { return (schedule(p, quietPeriod, actions) != null); }
  
  @CheckForNull
  public WaitingItem schedule(Task p, int quietPeriod, Action... actions) { return schedule2(p, quietPeriod, actions).getCreateItem(); }
  
  @NonNull
  public ScheduleResult schedule2(Task p, int quietPeriod, Action... actions) { return schedule2(p, quietPeriod, Arrays.asList(actions)); }
  
  public boolean cancel(Task p) {
    this.lock.lock();
    try {
      try {
        LOGGER.log(Level.FINE, "Cancelling {0}", p);
        for (WaitingItem item : this.waitingList) {
          if (item.task.equals(p))
            return item.cancel(this); 
        } 
        return ((this.blockedProjects.cancel(p) != null) ? 1 : 0) | ((this.buildables.cancel(p) != null) ? 1 : 0);
      } finally {
        updateSnapshot();
      } 
    } finally {
      this.lock.unlock();
    } 
  }
  
  private void updateSnapshot() {
    Snapshot revised = new Snapshot(this.waitingList, this.blockedProjects, this.buildables, this.pendings);
    if (LOGGER.isLoggable(Level.FINEST))
      LOGGER.log(Level.FINEST, "{0} → {1}; leftItems={2}", new Object[] { this.snapshot, revised, this.leftItems.asMap() }); 
    this.snapshot = revised;
  }
  
  public boolean cancel(Item item) {
    LOGGER.log(Level.FINE, "Cancelling {0} item#{1}", new Object[] { item.task, Long.valueOf(item.id) });
    this.lock.lock();
    try {
      try {
        return item.cancel(this);
      } finally {
        updateSnapshot();
      } 
    } finally {
      this.lock.unlock();
    } 
  }
  
  @RequirePOST
  public HttpResponse doCancelItem(@QueryParameter long id) throws IOException, ServletException {
    Item item = getItem(id);
    if (item != null && !hasReadPermission(item, true))
      item = null; 
    if (item != null) {
      if (item.hasCancelPermission()) {
        if (cancel(item))
          return HttpResponses.status(204); 
        return HttpResponses.error(500, "Could not cancel run for id " + id);
      } 
      return HttpResponses.error(422, "Item for id (" + id + ") is not cancellable");
    } 
    return HttpResponses.error(404, "Provided id (" + id + ") not found");
  }
  
  public boolean isEmpty() {
    Snapshot snapshot = this.snapshot;
    return (snapshot.waitingList.isEmpty() && snapshot.blockedProjects.isEmpty() && snapshot.buildables.isEmpty() && snapshot.pendings
      .isEmpty());
  }
  
  private WaitingItem peek() { return (WaitingItem)this.waitingList.iterator().next(); }
  
  @Exported(inline = true)
  public Item[] getItems() {
    Snapshot s = this.snapshot;
    List<Item> r = new ArrayList<Item>();
    for (WaitingItem p : s.waitingList)
      r = checkPermissionsAndAddToList(r, p); 
    for (BlockedItem p : s.blockedProjects)
      r = checkPermissionsAndAddToList(r, p); 
    for (BuildableItem p : Iterators.reverse(s.buildables))
      r = checkPermissionsAndAddToList(r, p); 
    for (BuildableItem p : Iterators.reverse(s.pendings))
      r = checkPermissionsAndAddToList(r, p); 
    Item[] arrayOfItem = new Item[r.size()];
    r.toArray(arrayOfItem);
    return arrayOfItem;
  }
  
  private List<Item> checkPermissionsAndAddToList(List<Item> r, Item t) {
    if (hasReadPermission(t.task, false))
      r.add(t); 
    return r;
  }
  
  private static boolean hasReadPermission(Item t, boolean valueIfNotAccessControlled) { return hasReadPermission(t.task, valueIfNotAccessControlled); }
  
  private static boolean hasReadPermission(Task t, boolean valueIfNotAccessControlled) {
    if (t instanceof AccessControlled) {
      AccessControlled taskAC = (AccessControlled)t;
      if (taskAC.hasPermission(Item.READ) || taskAC
        .hasPermission(Permission.READ))
        return true; 
      return false;
    } 
    return valueIfNotAccessControlled;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Exported(inline = true)
  public StubItem[] getDiscoverableItems() {
    Snapshot s = this.snapshot;
    List<StubItem> r = new ArrayList<StubItem>();
    for (WaitingItem p : s.waitingList)
      r = filterDiscoverableItemListBasedOnPermissions(r, p); 
    for (BlockedItem p : s.blockedProjects)
      r = filterDiscoverableItemListBasedOnPermissions(r, p); 
    for (BuildableItem p : Iterators.reverse(s.buildables))
      r = filterDiscoverableItemListBasedOnPermissions(r, p); 
    for (BuildableItem p : Iterators.reverse(s.pendings))
      r = filterDiscoverableItemListBasedOnPermissions(r, p); 
    StubItem[] arrayOfStubItem = new StubItem[r.size()];
    r.toArray(arrayOfStubItem);
    return arrayOfStubItem;
  }
  
  private List<StubItem> filterDiscoverableItemListBasedOnPermissions(List<StubItem> r, Item t) {
    if (t.task instanceof Item) {
      Item taskAsItem = (Item)t.task;
      if (!taskAsItem.hasPermission(Item.READ) && taskAsItem
        .hasPermission(Item.DISCOVER))
        r.add(new StubItem(new StubTask(t.task))); 
    } 
    return r;
  }
  
  @Deprecated
  public List<Item> getApproximateItemsQuickly() { return Arrays.asList(getItems()); }
  
  public Item getItem(long id) {
    Snapshot snapshot = this.snapshot;
    for (Item item : snapshot.blockedProjects) {
      if (item.id == id)
        return item; 
    } 
    for (Item item : snapshot.buildables) {
      if (item.id == id)
        return item; 
    } 
    for (Item item : snapshot.pendings) {
      if (item.id == id)
        return item; 
    } 
    for (Item item : snapshot.waitingList) {
      if (item.id == id)
        return item; 
    } 
    return (Item)this.leftItems.getIfPresent(Long.valueOf(id));
  }
  
  public List<BuildableItem> getBuildableItems(Computer c) {
    Snapshot snapshot = this.snapshot;
    List<BuildableItem> result = new ArrayList<BuildableItem>();
    _getBuildableItems(c, snapshot.buildables, result);
    _getBuildableItems(c, snapshot.pendings, result);
    return result;
  }
  
  private void _getBuildableItems(Computer c, List<BuildableItem> col, List<BuildableItem> result) {
    Node node = c.getNode();
    if (node == null)
      return; 
    for (BuildableItem p : col) {
      if (node.canTake(p) == null)
        result.add(p); 
    } 
  }
  
  public List<BuildableItem> getBuildableItems() {
    Snapshot snapshot = this.snapshot;
    ArrayList<BuildableItem> r = new ArrayList<BuildableItem>(snapshot.buildables);
    r.addAll(snapshot.pendings);
    return r;
  }
  
  public List<BuildableItem> getPendingItems() { return new ArrayList(this.snapshot.pendings); }
  
  protected List<BlockedItem> getBlockedItems() { return new ArrayList(this.snapshot.blockedProjects); }
  
  public Collection<LeftItem> getLeftItems() { return Collections.unmodifiableCollection(this.leftItems.asMap().values()); }
  
  public void clearLeftItems() { this.leftItems.invalidateAll(); }
  
  public List<Item> getUnblockedItems() {
    Snapshot snapshot = this.snapshot;
    List<Item> queuedNotBlocked = new ArrayList<Item>();
    queuedNotBlocked.addAll(snapshot.waitingList);
    queuedNotBlocked.addAll(snapshot.buildables);
    queuedNotBlocked.addAll(snapshot.pendings);
    return queuedNotBlocked;
  }
  
  public Set<Task> getUnblockedTasks() {
    List<Item> items = getUnblockedItems();
    Set<Task> unblockedTasks = new HashSet<Task>(items.size());
    for (Item t : items)
      unblockedTasks.add(t.task); 
    return unblockedTasks;
  }
  
  public boolean isPending(Task t) {
    Snapshot snapshot = this.snapshot;
    for (BuildableItem i : snapshot.pendings) {
      if (i.task.equals(t))
        return true; 
    } 
    return false;
  }
  
  public int countBuildableItemsFor(@CheckForNull Label l) {
    Snapshot snapshot = this.snapshot;
    int r = 0;
    for (BuildableItem bi : snapshot.buildables) {
      for (SubTask st : bi.task.getSubTasks()) {
        if (null == l || bi.getAssignedLabelFor(st) == l)
          r++; 
      } 
    } 
    for (BuildableItem bi : snapshot.pendings) {
      for (SubTask st : bi.task.getSubTasks()) {
        if (null == l || bi.getAssignedLabelFor(st) == l)
          r++; 
      } 
    } 
    return r;
  }
  
  public int strictCountBuildableItemsFor(@CheckForNull Label l) {
    Snapshot _snapshot = this.snapshot;
    int r = 0;
    for (BuildableItem bi : _snapshot.buildables) {
      for (SubTask st : bi.task.getSubTasks()) {
        if (bi.getAssignedLabelFor(st) == l)
          r++; 
      } 
    } 
    for (BuildableItem bi : _snapshot.pendings) {
      for (SubTask st : bi.task.getSubTasks()) {
        if (bi.getAssignedLabelFor(st) == l)
          r++; 
      } 
    } 
    return r;
  }
  
  public int countBuildableItems() { return countBuildableItemsFor(null); }
  
  public Item getItem(Task t) {
    Snapshot snapshot = this.snapshot;
    for (Item item : snapshot.blockedProjects) {
      if (item.task.equals(t))
        return item; 
    } 
    for (Item item : snapshot.buildables) {
      if (item.task.equals(t))
        return item; 
    } 
    for (Item item : snapshot.pendings) {
      if (item.task.equals(t))
        return item; 
    } 
    for (Item item : snapshot.waitingList) {
      if (item.task.equals(t))
        return item; 
    } 
    return null;
  }
  
  private List<Item> liveGetItems(Task t) {
    this.lock.lock();
    try {
      List<Item> result = new ArrayList<Item>();
      result.addAll(this.blockedProjects.getAll(t));
      result.addAll(this.buildables.getAll(t));
      if (LOGGER.isLoggable(Level.FINE)) {
        List<BuildableItem> thePendings = this.pendings.getAll(t);
        if (!thePendings.isEmpty())
          LOGGER.log(Level.FINE, "ignoring {0} during scheduleInternal", thePendings); 
      } 
      for (Item item : this.waitingList) {
        if (item.task.equals(t))
          result.add(item); 
      } 
      return result;
    } finally {
      this.lock.unlock();
    } 
  }
  
  public List<Item> getItems(Task t) {
    Snapshot snapshot = this.snapshot;
    List<Item> result = new ArrayList<Item>();
    for (Item item : snapshot.blockedProjects) {
      if (item.task.equals(t))
        result.add(item); 
    } 
    for (Item item : snapshot.buildables) {
      if (item.task.equals(t))
        result.add(item); 
    } 
    for (Item item : snapshot.pendings) {
      if (item.task.equals(t))
        result.add(item); 
    } 
    for (Item item : snapshot.waitingList) {
      if (item.task.equals(t))
        result.add(item); 
    } 
    return result;
  }
  
  public boolean contains(Task t) { return (getItem(t) != null); }
  
  void onStartExecuting(Executor exec) throws InterruptedException {
    this.lock.lock();
    try {
      try {
        WorkUnit wu = exec.getCurrentWorkUnit();
        this.pendings.remove(wu.context.item);
        LeftItem li = new LeftItem(wu.context);
        li.enter(this);
      } finally {
        updateSnapshot();
      } 
    } finally {
      this.lock.unlock();
    } 
  }
  
  @WithBridgeMethods({V.class})
  public Future<?> scheduleMaintenance() { return this.maintainerThread.submit(); }
  
  @CheckForNull
  private CauseOfBlockage getCauseOfBlockageForItem(Item i) {
    CauseOfBlockage causeOfBlockage = getCauseOfBlockageForTask(i.task);
    if (causeOfBlockage != null)
      return causeOfBlockage; 
    for (QueueTaskDispatcher d : QueueTaskDispatcher.all()) {
      try {
        causeOfBlockage = d.canRun(i);
      } catch (Throwable t) {
        LOGGER.log(Level.WARNING, t, () -> String.format("Exception evaluating if the queue can run the task '%s'", new Object[] { i.task.getName() }));
        causeOfBlockage = CauseOfBlockage.fromMessage(Messages._Queue_ExceptionCanRun());
      } 
      if (causeOfBlockage != null)
        return causeOfBlockage; 
    } 
    if (!(i instanceof BuildableItem))
      if (!i.task.isConcurrentBuild() && (this.buildables.containsKey(i.task) || this.pendings.containsKey(i.task)))
        return CauseOfBlockage.fromMessage(Messages._Queue_InProgress());  
    return null;
  }
  
  @CheckForNull
  private CauseOfBlockage getCauseOfBlockageForTask(Task task) {
    CauseOfBlockage causeOfBlockage = task.getCauseOfBlockage();
    if (causeOfBlockage != null)
      return causeOfBlockage; 
    if (!canRun(task.getResourceList())) {
      ResourceActivity r = getBlockingActivity(task);
      if (r != null) {
        if (r == task)
          return CauseOfBlockage.fromMessage(Messages._Queue_InProgress()); 
        return CauseOfBlockage.fromMessage(Messages._Queue_BlockedBy(r.getDisplayName()));
      } 
    } 
    return null;
  }
  
  public static void withLock(Runnable runnable) {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    Queue queue = (jenkins == null) ? null : jenkins.getQueue();
    if (queue == null) {
      runnable.run();
    } else {
      queue._withLock(runnable);
    } 
  }
  
  public static <V, T extends Throwable> V withLock(Callable<V, T> callable) throws T {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    Queue queue = (jenkins == null) ? null : jenkins.getQueue();
    if (queue == null)
      return (V)callable.call(); 
    return (V)queue._withLock(callable);
  }
  
  public static <V> V withLock(Callable<V> callable) throws Exception {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    Queue queue = (jenkins == null) ? null : jenkins.getQueue();
    if (queue == null)
      return (V)callable.call(); 
    return (V)queue._withLock(callable);
  }
  
  public static boolean tryWithLock(Runnable runnable) {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    Queue queue = (jenkins == null) ? null : jenkins.getQueue();
    if (queue == null) {
      runnable.run();
      return true;
    } 
    return queue._tryWithLock(runnable);
  }
  
  public static Runnable wrapWithLock(Runnable runnable) {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    Queue queue = (jenkins == null) ? null : jenkins.getQueue();
    return (queue == null) ? runnable : new LockedRunnable(runnable);
  }
  
  public static <V, T extends Throwable> Callable<V, T> wrapWithLock(Callable<V, T> callable) {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    Queue queue = (jenkins == null) ? null : jenkins.getQueue();
    return (queue == null) ? callable : new LockedHRCallable(callable);
  }
  
  public static <V> Callable<V> wrapWithLock(Callable<V> callable) {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    Queue queue = (jenkins == null) ? null : jenkins.getQueue();
    return (queue == null) ? callable : new LockedJUCCallable(callable);
  }
  
  @SuppressFBWarnings(value = {"WA_AWAIT_NOT_IN_LOOP"}, justification = "the caller does indeed call this method in a loop")
  protected void _await() { this.condition.await(); }
  
  protected void _signalAll() { this.condition.signalAll(); }
  
  protected void _withLock(Runnable runnable) {
    this.lock.lock();
    try {
      runnable.run();
    } finally {
      this.lock.unlock();
    } 
  }
  
  protected boolean _tryWithLock(Runnable runnable) {
    if (this.lock.tryLock()) {
      try {
        runnable.run();
      } finally {
        this.lock.unlock();
      } 
      return true;
    } 
    return false;
  }
  
  protected <V, T extends Throwable> V _withLock(Callable<V, T> callable) throws T {
    this.lock.lock();
    try {
      object = callable.call();
      return (V)object;
    } finally {
      this.lock.unlock();
    } 
  }
  
  protected <V> V _withLock(Callable<V> callable) throws Exception {
    this.lock.lock();
    try {
      object = callable.call();
      return (V)object;
    } finally {
      this.lock.unlock();
    } 
  }
  
  public void maintain() {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    if (jenkins == null)
      return; 
    this.lock.lock();
    try {
      try {
        LOGGER.log(Level.FINE, "Queue maintenance started on {0} with {1}", new Object[] { this, this.snapshot });
        Map<Executor, JobOffer> parked = new HashMap<Executor, JobOffer>();
        List<BuildableItem> lostPendings = new ArrayList<BuildableItem>(this.pendings);
        for (Computer c : jenkins.getComputers()) {
          for (Executor e : c.getAllExecutors()) {
            if (e.isInterrupted()) {
              lostPendings.clear();
              LOGGER.log(Level.FINEST, "Interrupt thread for executor {0} is set and we do not know what work unit was on the executor.", e
                  
                  .getDisplayName());
              continue;
            } 
            if (e.isParking()) {
              LOGGER.log(Level.FINEST, "{0} is parking and is waiting for a job to execute.", e.getDisplayName());
              parked.put(e, new JobOffer(e));
            } 
            WorkUnit workUnit = e.getCurrentWorkUnit();
            if (workUnit != null)
              lostPendings.remove(workUnit.context.item); 
          } 
        } 
        for (BuildableItem p : lostPendings) {
          if (LOGGER.isLoggable(Level.FINE))
            LOGGER.log(Level.FINE, "BuildableItem {0}: pending -> buildable as the assigned executor disappeared", p.task
                
                .getFullDisplayName()); 
          p.isPending = false;
          this.pendings.remove(p);
          makeBuildable(p);
        } 
        QueueSorter s = this.sorter;
        blockedItems = new ArrayList<BlockedItem>(this.blockedProjects.values());
        if (s != null) {
          s.sortBlockedItems(blockedItems);
        } else {
          blockedItems.sort(QueueSorter.DEFAULT_BLOCKED_ITEM_COMPARATOR);
        } 
        for (BlockedItem p : blockedItems) {
          String taskDisplayName = LOGGER.isLoggable(Level.FINEST) ? p.task.getFullDisplayName() : null;
          LOGGER.log(Level.FINEST, "Current blocked item: {0}", taskDisplayName);
          CauseOfBlockage causeOfBlockage = getCauseOfBlockageForItem(p);
          if (causeOfBlockage == null) {
            LOGGER.log(Level.FINEST, "BlockedItem {0}: blocked -> buildable as the build is not blocked and new tasks are allowed", taskDisplayName);
            Runnable r = makeBuildable(new BuildableItem(p));
            if (r != null) {
              p.leave(this);
              r.run();
              updateSnapshot();
            } 
            continue;
          } 
          p.setCauseOfBlockage(causeOfBlockage);
        } 
        while (!this.waitingList.isEmpty()) {
          WaitingItem top = peek();
          if (top.timestamp.compareTo(new GregorianCalendar()) > 0) {
            LOGGER.log(Level.FINEST, "Finished moving all ready items from queue.");
            break;
          } 
          top.leave(this);
          CauseOfBlockage causeOfBlockage = getCauseOfBlockageForItem(top);
          if (causeOfBlockage == null) {
            Runnable r = makeBuildable(new BuildableItem(top));
            String topTaskDisplayName = LOGGER.isLoggable(Level.FINEST) ? top.task.getFullDisplayName() : null;
            if (r != null) {
              LOGGER.log(Level.FINEST, "Executing runnable {0}", topTaskDisplayName);
              r.run();
              continue;
            } 
            LOGGER.log(Level.FINEST, "Item {0} was unable to be made a buildable and is now a blocked item.", topTaskDisplayName);
            (new BlockedItem(this, top, CauseOfBlockage.fromMessage(Messages._Queue_HudsonIsAboutToShutDown()))).enter(this);
            continue;
          } 
          (new BlockedItem(this, top, causeOfBlockage)).enter(this);
        } 
        if (s != null)
          try {
            s.sortBuildableItems(this.buildables);
          } catch (Throwable blockedItems) {
            Throwable e;
            LOGGER.log(Level.WARNING, "s.sortBuildableItems() threw Throwable: {0}", e);
          }  
        updateSnapshot();
        for (BuildableItem p : new ArrayList(this.buildables)) {
          CauseOfBlockage causeOfBlockage = getCauseOfBlockageForItem(p);
          if (causeOfBlockage != null) {
            p.leave(this);
            (new BlockedItem(this, p, causeOfBlockage)).enter(this);
            LOGGER.log(Level.FINE, "Catching that {0} is blocked in the last minute", p);
            updateSnapshot();
            continue;
          } 
          String taskDisplayName = LOGGER.isLoggable(Level.FINEST) ? p.task.getFullDisplayName() : null;
          if (p.task instanceof FlyweightTask) {
            Runnable r = makeFlyWeightTaskBuildable(new BuildableItem(p));
            if (r != null) {
              p.leave(this);
              LOGGER.log(Level.FINEST, "Executing flyweight task {0}", taskDisplayName);
              r.run();
              updateSnapshot();
            } 
            continue;
          } 
          List<JobOffer> candidates = new ArrayList<JobOffer>(parked.size());
          Map<Node, CauseOfBlockage> reasonMap = new HashMap<Node, CauseOfBlockage>();
          for (JobOffer j : parked.values()) {
            CauseOfBlockage reason;
            Node offerNode = j.getNode();
            if (reasonMap.containsKey(offerNode)) {
              reason = (CauseOfBlockage)reasonMap.get(offerNode);
            } else {
              reason = j.getCauseOfBlockage(p);
              reasonMap.put(offerNode, reason);
            } 
            if (reason == null) {
              LOGGER.log(Level.FINEST, "{0} is a potential candidate for task {1}", new Object[] { j, taskDisplayName });
              candidates.add(j);
              continue;
            } 
            LOGGER.log(Level.FINEST, "{0} rejected {1}: {2}", new Object[] { j, taskDisplayName, reason });
          } 
          MappingWorksheet ws = new MappingWorksheet(p, candidates);
          MappingWorksheet.Mapping m = this.loadBalancer.map(p.task, ws);
          if (m == null) {
            LOGGER.log(Level.FINER, "Failed to map {0} to executors. candidates={1} parked={2}", new Object[] { p, candidates, parked
                  .values() });
            List<CauseOfBlockage> reasons = (List)reasonMap.values().stream().filter(Objects::nonNull).collect(Collectors.toList());
            p.transientCausesOfBlockage = reasons.isEmpty() ? null : reasons;
            continue;
          } 
          WorkUnitContext wuc = new WorkUnitContext(p);
          LOGGER.log(Level.FINEST, "Found a matching executor for {0}. Using it.", taskDisplayName);
          m.execute(wuc);
          p.leave(this);
          if (!wuc.getWorkUnits().isEmpty()) {
            LOGGER.log(Level.FINEST, "BuildableItem {0} marked as pending.", taskDisplayName);
            makePending(p);
          } else {
            LOGGER.log(Level.FINEST, "BuildableItem {0} with empty work units!?", p);
          } 
          updateSnapshot();
        } 
      } finally {
        updateSnapshot();
      } 
    } finally {
      this.lock.unlock();
    } 
  }
  
  @CheckForNull
  private Runnable makeBuildable(BuildableItem p) {
    if (p.task instanceof FlyweightTask) {
      String taskDisplayName = LOGGER.isLoggable(Level.FINEST) ? p.task.getFullDisplayName() : null;
      if (!isBlockedByShutdown(p.task)) {
        Runnable runnable = makeFlyWeightTaskBuildable(p);
        LOGGER.log(Level.FINEST, "Converting flyweight task: {0} into a BuildableRunnable", taskDisplayName);
        if (runnable != null)
          return runnable; 
        LOGGER.log(Level.FINEST, "Flyweight task {0} is entering as buildable to provision a node.", taskDisplayName);
        return new BuildableRunnable(this, p);
      } 
      LOGGER.log(Level.FINEST, "Task {0} is blocked by shutdown.", taskDisplayName);
      return null;
    } 
    return new BuildableRunnable(this, p);
  }
  
  @CheckForNull
  private Runnable makeFlyWeightTaskBuildable(BuildableItem p) {
    if (p.task instanceof FlyweightTask) {
      Jenkins h = Jenkins.get();
      Label lbl = p.getAssignedLabel();
      Computer masterComputer = h.toComputer();
      if (lbl != null && lbl.equals(h.getSelfLabel())) {
        if (h.canTake(p) == null)
          return createFlyWeightTaskRunnable(p, masterComputer); 
        return null;
      } 
      if (lbl == null && h.canTake(p) == null && masterComputer.isOnline() && masterComputer.isAcceptingTasks())
        return createFlyWeightTaskRunnable(p, masterComputer); 
      Map<Node, Integer> hashSource = new HashMap<Node, Integer>(h.getNodes().size());
      for (Node n : h.getNodes())
        hashSource.put(n, Integer.valueOf(n.getNumExecutors() * 100)); 
      ConsistentHash<Node> hash = new ConsistentHash<Node>(NODE_HASH);
      hash.addAll(hashSource);
      String fullDisplayName = p.task.getFullDisplayName();
      for (Node n : hash.list(fullDisplayName)) {
        Computer c = n.toComputer();
        if (c == null || c.isOffline())
          continue; 
        if (lbl != null && !lbl.contains(n))
          continue; 
        if (n.canTake(p) != null)
          continue; 
        return createFlyWeightTaskRunnable(p, c);
      } 
    } 
    return null;
  }
  
  private Runnable createFlyWeightTaskRunnable(BuildableItem p, Computer c) {
    if (LOGGER.isLoggable(Level.FINEST))
      LOGGER.log(Level.FINEST, "Creating flyweight task {0} for computer {1}", new Object[] { p.task
            .getFullDisplayName(), c.getName() }); 
    return () -> {
        c.startFlyWeightTask((new WorkUnitContext(p)).createWorkUnit(p.task));
        makePending(p);
      };
  }
  
  private static final ConsistentHash.Hash<Node> NODE_HASH = Node::getNodeName;
  
  private boolean makePending(BuildableItem p) {
    p.isPending = true;
    return this.pendings.add(p);
  }
  
  @Deprecated
  public static boolean ifBlockedByHudsonShutdown(Task task) { return isBlockedByShutdown(task); }
  
  public static boolean isBlockedByShutdown(Task task) { return (Jenkins.get().isQuietingDown() && !(task instanceof NonBlockingTask)); }
  
  public Api getApi() { return new Api(this); }
  
  private static final Logger LOGGER = Logger.getLogger(Queue.class.getName());
  
  public static final XStream XSTREAM = new XStream2();
  
  static  {
    XSTREAM.registerConverter(new Object());
    XSTREAM.registerConverter(new Object());
    XSTREAM.registerConverter(new Object());
  }
  
  @CLIResolver
  public static Queue getInstance() { return Jenkins.get().getQueue(); }
  
  @Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED)
  public static void init(Jenkins h) {
    Queue queue = h.getQueue();
    Item[] arrayOfItem = queue.getItems();
    if (arrayOfItem.length > 0)
      LOGGER.warning(() -> "Loading queue will discard previously scheduled items: " + Arrays.toString(items)); 
    queue.load();
  }
}
