package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.Functions;
import hudson.Util;
import hudson.model.queue.Executables;
import hudson.model.queue.SubTask;
import hudson.model.queue.WorkUnit;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.AccessControlled;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import jenkins.model.CauseOfInterruption;
import jenkins.model.InterruptedBuildAction;
import jenkins.model.Jenkins;
import jenkins.model.queue.AsynchronousExecution;
import jenkins.security.QueueItemAuthenticatorConfiguration;
import jenkins.security.QueueItemAuthenticatorDescriptor;
import net.jcip.annotations.GuardedBy;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.springframework.security.core.Authentication;

@ExportedBean
public class Executor extends Thread implements ModelObject {
  @NonNull
  protected final Computer owner;
  
  private final Queue queue;
  
  private final ReadWriteLock lock = new ReentrantReadWriteLock();
  
  private static final int DEFAULT_ESTIMATED_DURATION = -1;
  
  @GuardedBy("lock")
  private long startTime;
  
  private final long creationTime = System.currentTimeMillis();
  
  private int number;
  
  @GuardedBy("lock")
  private Queue.Executable executable;
  
  private long executableEstimatedDuration = -1L;
  
  @GuardedBy("lock")
  private AsynchronousExecution asynchronousExecution;
  
  @GuardedBy("lock")
  private WorkUnit workUnit;
  
  @GuardedBy("lock")
  private boolean started;
  
  @GuardedBy("lock")
  private Result interruptStatus;
  
  @GuardedBy("lock")
  private final List<CauseOfInterruption> causes = new Vector();
  
  public Executor(@NonNull Computer owner, int n) {
    super("Executor #" + n + " for " + owner.getDisplayName());
    this.owner = owner;
    this.queue = Jenkins.get().getQueue();
    this.number = n;
  }
  
  public void interrupt() {
    if (Thread.currentThread() == this) {
      super.interrupt();
    } else {
      interrupt(Result.ABORTED);
    } 
  }
  
  void interruptForShutdown() { interrupt(Result.ABORTED, true); }
  
  public void interrupt(Result result) { interrupt(result, false); }
  
  private void interrupt(Result result, boolean forShutdown) {
    Authentication a = Jenkins.getAuthentication2();
    if (a.equals(ACL.SYSTEM2)) {
      interrupt(result, forShutdown, new CauseOfInterruption[0]);
    } else {
      interrupt(result, forShutdown, new CauseOfInterruption[] { new CauseOfInterruption.UserInterruption(a.getName()) });
    } 
  }
  
  public void interrupt(Result result, CauseOfInterruption... causes) { interrupt(result, false, causes); }
  
  private void interrupt(Result result, boolean forShutdown, CauseOfInterruption... causes) {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.log(Level.FINE, String.format("%s is interrupted(%s): %s", new Object[] { getDisplayName(), result, Arrays.stream(causes).map(Object::toString).collect(Collectors.joining(",")) }), new InterruptedException()); 
    this.lock.writeLock().lock();
    try {
      if (!this.started) {
        this.owner.removeExecutor(this);
        return;
      } 
      this.interruptStatus = result;
      for (CauseOfInterruption c : causes) {
        if (!this.causes.contains(c))
          this.causes.add(c); 
      } 
      if (this.asynchronousExecution != null) {
        this.asynchronousExecution.interrupt(forShutdown);
      } else {
        super.interrupt();
      } 
    } finally {
      this.lock.writeLock().unlock();
    } 
  }
  
  public Result abortResult() {
    Thread.interrupted();
    this.lock.writeLock().lock();
    try {
      Result r = this.interruptStatus;
      if (r == null)
        r = Result.ABORTED; 
      return r;
    } finally {
      this.lock.writeLock().unlock();
    } 
  }
  
  public void recordCauseOfInterruption(Run<?, ?> build, TaskListener listener) {
    List<CauseOfInterruption> r;
    this.lock.writeLock().lock();
    try {
      if (this.causes.isEmpty())
        return; 
      r = new ArrayList<CauseOfInterruption>(this.causes);
      this.causes.clear();
    } finally {
      this.lock.writeLock().unlock();
    } 
    build.addAction(new InterruptedBuildAction(r));
    for (CauseOfInterruption c : r)
      c.print(listener); 
  }
  
  private void resetWorkUnit(String reason) {
    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);
    try {
      pw.printf("%s grabbed %s from queue but %s %s. ", new Object[] { getName(), this.workUnit, this.owner.getDisplayName(), reason });
      if (this.owner.getTerminatedBy().isEmpty()) {
        pw.print("No termination trace available.");
      } else {
        pw.println("Termination trace follows:");
        for (Computer.TerminationRequest request : this.owner.getTerminatedBy())
          Functions.printStackTrace(request, pw); 
      } 
      pw.close();
    } catch (Throwable throwable) {
      try {
        pw.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    LOGGER.log(Level.WARNING, writer.toString());
    this.lock.writeLock().lock();
    try {
      if (this.executable != null)
        throw new IllegalStateException("Cannot reset the work unit after the executable has been created"); 
      this.workUnit = null;
    } finally {
      this.lock.writeLock().unlock();
    } 
  }
  
  public void run() {
    WorkUnit workUnit;
    if (!this.owner.isOnline()) {
      resetWorkUnit("went off-line before the task's worker thread started");
      this.owner.removeExecutor(this);
      this.queue.scheduleMaintenance();
      return;
    } 
    if (this.owner.getNode() == null) {
      resetWorkUnit("was removed before the task's worker thread started");
      this.owner.removeExecutor(this);
      this.queue.scheduleMaintenance();
      return;
    } 
    this.lock.writeLock().lock();
    try {
      this.startTime = System.currentTimeMillis();
      workUnit = this.workUnit;
    } finally {
      this.lock.writeLock().unlock();
    } 
    try {
      ctx = ACL.as2(ACL.SYSTEM2);
      try {
        Queue.Executable executable;
        SubTask task = (SubTask)Queue.withLock(new Object(this, workUnit));
        this.lock.readLock().lock();
        try {
          if (this.workUnit == null) {
            this.lock.readLock().unlock();
            return;
          } 
          executable = this.executable;
        } finally {
          this.lock.readLock().unlock();
        } 
        if (LOGGER.isLoggable(Level.FINE))
          LOGGER.log(Level.FINE, getName() + " is going to execute " + getName()); 
        problems = null;
        try {
          workUnit.context.synchronizeStart();
          if (executable == null) {
            this.lock.readLock().lock();
            return;
          } 
          this.executableEstimatedDuration = executable.getEstimatedDuration();
          if (executable instanceof Actionable) {
            if (LOGGER.isLoggable(Level.FINER))
              LOGGER.log(Level.FINER, "when running {0} from {1} we are copying {2} actions whereas the item currently has {3}", new Object[] { executable, workUnit.context.item, workUnit.context.actions, workUnit.context.item.getAllActions() }); 
            for (Action action : workUnit.context.actions)
              ((Actionable)executable).addAction(action); 
          } 
          setName(getName() + " : executing " + getName());
          Authentication auth = workUnit.context.item.authenticate2();
          LOGGER.log(Level.FINE, "{0} is now executing {1} as {2}", new Object[] { getName(), executable, auth });
          if (LOGGER.isLoggable(Level.FINE) && auth.equals(ACL.SYSTEM2))
            if (QueueItemAuthenticatorDescriptor.all().isEmpty()) {
              LOGGER.fine("no QueueItemAuthenticator implementations installed");
            } else if (QueueItemAuthenticatorConfiguration.get().getAuthenticators().isEmpty()) {
              LOGGER.fine("no QueueItemAuthenticator implementations configured");
            } else {
              LOGGER.log(Level.FINE, "some QueueItemAuthenticator implementations configured but neglected to authenticate {0}", executable);
            }  
          ACLContext context = ACL.as2(auth);
          try {
            this.queue.execute(executable, task);
            if (context != null)
              context.close(); 
          } catch (Throwable throwable) {
            if (context != null)
              try {
                context.close();
              } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
              }  
            throw throwable;
          } 
        } catch (AsynchronousExecution x) {
          AsynchronousExecution asynchronousExecution1;
          this.lock.writeLock().lock();
          try {
            asynchronousExecution1.setExecutorWithoutCompleting(this);
            this.asynchronousExecution = asynchronousExecution1;
          } finally {
            this.lock.writeLock().unlock();
          } 
          asynchronousExecution1.maybeComplete();
        } catch (Throwable e) {
          Throwable throwable;
          problems = throwable;
        } finally {
          boolean needFinish1;
          this.lock.readLock().lock();
          try {
            needFinish1 = (this.asynchronousExecution == null);
          } finally {
            this.lock.readLock().unlock();
          } 
          if (needFinish1)
            finish1(problems); 
        } 
        if (ctx != null)
          ctx.close(); 
      } catch (Throwable throwable) {
        if (ctx != null)
          try {
            ctx.close();
          } catch (Throwable executable) {
            throwable.addSuppressed(executable);
          }  
        throw throwable;
      } 
    } catch (InterruptedException e) {
      LOGGER.log(Level.FINE, getName() + " interrupted", e);
    } catch (Exception|Error e) {
      LOGGER.log(Level.SEVERE, getName() + ": Unexpected executor death", e);
    } finally {
      if (this.asynchronousExecution == null)
        finish2(); 
    } 
  }
  
  private void finish1(@CheckForNull Throwable problems) {
    if (problems != null) {
      LOGGER.log(Level.SEVERE, "Executor threw an exception", problems);
      this.workUnit.context.abort(problems);
    } 
    long time = System.currentTimeMillis() - this.startTime;
    LOGGER.log(Level.FINE, "{0} completed {1} in {2}ms", new Object[] { getName(), this.executable, Long.valueOf(time) });
    try {
      this.workUnit.context.synchronizeEnd(this, this.executable, problems, time);
    } catch (InterruptedException e) {
      this.workUnit.context.abort(e);
    } finally {
      this.workUnit.setExecutor(null);
    } 
  }
  
  private void finish2() {
    for (RuntimeException e1 : this.owner.getTerminatedBy()) {
      LOGGER.log(Level.FINE, String.format("%s termination trace", new Object[] { getName() }), e1);
    } 
    this.owner.removeExecutor(this);
    if (this instanceof OneOffExecutor)
      this.owner.remove((OneOffExecutor)this); 
    this.executableEstimatedDuration = -1L;
    this.queue.scheduleMaintenance();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void completedAsynchronous(@CheckForNull Throwable error) {
    try {
      finish1(error);
    } finally {
      finish2();
    } 
    this.asynchronousExecution = null;
  }
  
  @CheckForNull
  public Queue.Executable getCurrentExecutable() {
    this.lock.readLock().lock();
    try {
      return this.executable;
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  @Exported(name = "currentExecutable")
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public Queue.Executable getCurrentExecutableForApi() {
    Queue.Executable candidate = getCurrentExecutable();
    return (candidate instanceof AccessControlled && ((AccessControlled)candidate).hasPermission(Item.READ)) ? candidate : null;
  }
  
  @NonNull
  public Collection<CauseOfInterruption> getCausesOfInterruption() { return Collections.unmodifiableCollection(this.causes); }
  
  @CheckForNull
  public WorkUnit getCurrentWorkUnit() {
    this.lock.readLock().lock();
    try {
      return this.workUnit;
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  public FilePath getCurrentWorkspace() {
    this.lock.readLock().lock();
    try {
      if (this.executable == null)
        return null; 
      if (this.executable instanceof AbstractBuild) {
        AbstractBuild ab = (AbstractBuild)this.executable;
        return ab.getWorkspace();
      } 
      return null;
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  public String getDisplayName() { return "Executor #" + getNumber(); }
  
  @Exported
  public int getNumber() { return this.number; }
  
  @Exported
  public boolean isIdle() {
    this.lock.readLock().lock();
    try {
      return (this.workUnit == null && this.executable == null);
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  public boolean isBusy() {
    this.lock.readLock().lock();
    try {
      return (this.workUnit != null || this.executable != null);
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  public boolean isActive() {
    this.lock.readLock().lock();
    try {
      return (!this.started || this.asynchronousExecution != null || isAlive());
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  @CheckForNull
  public AsynchronousExecution getAsynchronousExecution() {
    this.lock.readLock().lock();
    try {
      return this.asynchronousExecution;
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  public boolean isDisplayCell() {
    AsynchronousExecution asynchronousExecution = getAsynchronousExecution();
    return (asynchronousExecution == null || asynchronousExecution.displayCell());
  }
  
  public boolean isParking() {
    this.lock.readLock().lock();
    try {
      return !this.started;
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  @Deprecated
  @CheckForNull
  public Throwable getCauseOfDeath() { return null; }
  
  @Exported
  public int getProgress() {
    long d = this.executableEstimatedDuration;
    if (d <= 0L)
      return -1; 
    int num = (int)(getElapsedTime() * 100L / d);
    if (num >= 100)
      num = 99; 
    return num;
  }
  
  @Exported
  public boolean isLikelyStuck() {
    this.lock.readLock().lock();
    try {
      if (this.executable == null)
        return false; 
    } finally {
      this.lock.readLock().unlock();
    } 
    long elapsed = getElapsedTime();
    long d = this.executableEstimatedDuration;
    if (d >= 0L)
      return (d * 10L < elapsed); 
    return (TimeUnit.MILLISECONDS.toHours(elapsed) > 24L);
  }
  
  public long getElapsedTime() {
    this.lock.readLock().lock();
    try {
      return System.currentTimeMillis() - this.startTime;
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  public long getTimeSpentInQueue() {
    this.lock.readLock().lock();
    try {
      return this.startTime - this.workUnit.context.item.buildableStartMilliseconds;
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  public String getTimestampString() { return Util.getTimeSpanString(getElapsedTime()); }
  
  public String getEstimatedRemainingTime() {
    long d = this.executableEstimatedDuration;
    if (d < 0L)
      return Messages.Executor_NotAvailable(); 
    long eta = d - getElapsedTime();
    if (eta <= 0L)
      return Messages.Executor_NotAvailable(); 
    return Util.getTimeSpanString(eta);
  }
  
  public long getEstimatedRemainingTimeMillis() {
    long d = this.executableEstimatedDuration;
    if (d < 0L)
      return -1L; 
    long eta = d - getElapsedTime();
    if (eta <= 0L)
      return -1L; 
    return eta;
  }
  
  public void start() { throw new UnsupportedOperationException(); }
  
  void start(WorkUnit task) {
    this.lock.writeLock().lock();
    try {
      this.workUnit = task;
      super.start();
      this.started = true;
    } finally {
      this.lock.writeLock().unlock();
    } 
  }
  
  @RequirePOST
  @Deprecated
  public void doStop(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { doStop().generateResponse(req, rsp, this); }
  
  @RequirePOST
  public HttpResponse doStop() { return doStopBuild(null); }
  
  @RequirePOST
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public HttpResponse doStopBuild(@QueryParameter(fixEmpty = true) @CheckForNull String runExtId) {
    this.lock.writeLock().lock();
    try {
      if (this.executable != null && (
        runExtId == null || runExtId.isEmpty() || !(this.executable instanceof Run) || runExtId
        .equals(((Run)this.executable).getExternalizableId()))) {
        Queue.Task ownerTask = Executables.getParentOf(this.executable).getOwnerTask();
        boolean canAbort = ownerTask.hasAbortPermission();
        if (canAbort && ownerTask instanceof AccessControlled && 
          !((AccessControlled)ownerTask).hasPermission(Item.READ))
          return HttpResponses.forwardToPreviousPage(); 
        ownerTask.checkAbortPermission();
        interrupt();
      } 
    } finally {
      this.lock.writeLock().unlock();
    } 
    return HttpResponses.forwardToPreviousPage();
  }
  
  @Deprecated
  public HttpResponse doYank() { return HttpResponses.redirectViaContextPath("/"); }
  
  public boolean hasStopPermission() {
    this.lock.readLock().lock();
    try {
      return (this.executable != null && Executables.getParentOf(this.executable).getOwnerTask().hasAbortPermission());
    } catch (RuntimeException ex) {
      if (!(ex instanceof org.springframework.security.access.AccessDeniedException))
        LOGGER.log(Level.WARNING, "Unhandled exception", ex); 
      return false;
    } finally {
      this.lock.readLock().unlock();
    } 
  }
  
  @NonNull
  public Computer getOwner() { return this.owner; }
  
  public long getIdleStartMilliseconds() {
    if (isIdle())
      return Math.max(this.creationTime, this.owner.getConnectTime()); 
    return Math.max(this.startTime + Math.max(0L, this.executableEstimatedDuration), System.currentTimeMillis() + 15000L);
  }
  
  public Api getApi() { return new Api(this); }
  
  public <T> T newImpersonatingProxy(Class<T> type, T core) { return (T)(new Object(this))









      
      .wrap(type, core); }
  
  @CheckForNull
  public static Executor currentExecutor() {
    t = Thread.currentThread();
    if (t instanceof Executor)
      return (Executor)t; 
    return (Executor)IMPERSONATION.get();
  }
  
  @CheckForNull
  public static Executor of(Queue.Executable executable) {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    if (jenkins == null)
      return null; 
    for (Computer computer : jenkins.getComputers()) {
      for (Executor executor : computer.getAllExecutors()) {
        if (executor.getCurrentExecutable() == executable)
          return executor; 
      } 
    } 
    return null;
  }
  
  @Deprecated
  public static long getEstimatedDurationFor(Queue.Executable e) { return (e == null) ? -1L : e.getEstimatedDuration(); }
  
  private static final ThreadLocal<Executor> IMPERSONATION = new ThreadLocal();
  
  private static final Logger LOGGER = Logger.getLogger(Executor.class.getName());
}
