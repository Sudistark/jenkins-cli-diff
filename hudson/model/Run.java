package hudson.model;

import com.jcraft.jzlib.GZIPInputStream;
import com.thoughtworks.xstream.XStream;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.BulkChange;
import hudson.EnvVars;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.FeedAdapter;
import hudson.Functions;
import hudson.Util;
import hudson.XmlFile;
import hudson.cli.declarative.CLIMethod;
import hudson.console.AnnotatedLargeText;
import hudson.console.ConsoleLogFilter;
import hudson.console.ConsoleNote;
import hudson.console.ModelHyperlinkNote;
import hudson.console.PlainTextConsoleOutputStream;
import hudson.model.listeners.RunListener;
import hudson.model.listeners.SaveableListener;
import hudson.search.SearchIndexBuilder;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Fingerprinter;
import hudson.util.FormApply;
import hudson.util.LogTaskListener;
import hudson.util.XStream2;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.ArtifactManager;
import jenkins.model.ArtifactManagerConfiguration;
import jenkins.model.ArtifactManagerFactory;
import jenkins.model.Jenkins;
import jenkins.model.RunAction2;
import jenkins.model.StandardArtifactManager;
import jenkins.model.lazy.BuildReference;
import jenkins.util.SystemProperties;
import jenkins.util.VirtualFile;
import jenkins.util.io.OnMaster;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.ArrayUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@ExportedBean
public abstract class Run<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> extends Actionable implements ExtensionPoint, Comparable<RunT>, AccessControlled, PersistenceRoot, DescriptorByNameOwner, OnMaster, StaplerProxy {
  public static final long QUEUE_ID_UNKNOWN = -1L;
  
  private static int TRUNCATED_DESCRIPTION_LIMIT = SystemProperties.getInteger("historyWidget.descriptionLimit", Integer.valueOf(100)).intValue();
  
  @NonNull
  protected final JobT project;
  
  public int number;
  
  private long queueId;
  
  @CheckForNull
  private String id;
  
  protected long timestamp;
  
  private long startTime;
  
  protected long duration;
  
  protected String charset;
  
  private boolean keepLog;
  
  @CheckForNull
  private ArtifactManager artifactManager;
  
  private boolean isPendingDelete;
  
  protected Run(@NonNull JobT job) throws IOException {
    this(job, System.currentTimeMillis());
    this.number = this.project.assignBuildNumber();
    LOGGER.log(Level.FINER, "new {0} @{1}", new Object[] { this, Integer.valueOf(hashCode()) });
  }
  
  protected Run(@NonNull JobT job, @NonNull Calendar timestamp) { this(job, timestamp.getTimeInMillis()); }
  
  protected Run(@NonNull JobT job, long timestamp) {
    this.queueId = -1L;
    this.project = job;
    this.timestamp = timestamp;
    this.state = State.NOT_STARTED;
  }
  
  protected Run(@NonNull JobT project, @NonNull File buildDir) throws IOException {
    this.queueId = -1L;
    this.project = project;
    this.previousBuildInProgress = _this();
    this.number = Integer.parseInt(buildDir.getName());
    reload();
  }
  
  public void reload() throws IOException {
    this.state = State.COMPLETED;
    this.result = Result.FAILURE;
    getDataFile().unmarshal(this);
    if (this.state == State.COMPLETED) {
      LOGGER.log(Level.FINER, "reload {0} @{1}", new Object[] { this, Integer.valueOf(hashCode()) });
    } else {
      LOGGER.log(Level.WARNING, "reload {0} @{1} with anomalous state {2}", new Object[] { this, Integer.valueOf(hashCode()), this.state });
    } 
  }
  
  protected void onLoad() throws IOException {
    for (Action a : getAllActions()) {
      if (a instanceof RunAction2)
        try {
          ((RunAction2)a).onLoad(this);
          continue;
        } catch (RuntimeException x) {
          LOGGER.log(Level.WARNING, "failed to load " + a + " from " + getDataFile(), x);
          removeAction(a);
          continue;
        }  
      if (a instanceof RunAction)
        ((RunAction)a).onLoad(); 
    } 
    if (this.artifactManager != null)
      this.artifactManager.onLoad(this); 
  }
  
  @Deprecated
  public List<Action> getTransientActions() {
    List<Action> actions = new ArrayList<Action>();
    for (TransientBuildActionFactory factory : TransientBuildActionFactory.all()) {
      for (Action created : factory.createFor(this)) {
        if (created == null) {
          LOGGER.log(Level.WARNING, "null action added by {0}", factory);
          continue;
        } 
        actions.add(created);
      } 
    } 
    return Collections.unmodifiableList(actions);
  }
  
  public void addAction(@NonNull Action a) {
    super.addAction(a);
    if (a instanceof RunAction2) {
      ((RunAction2)a).onAttached(this);
    } else if (a instanceof RunAction) {
      ((RunAction)a).onAttached(this);
    } 
  }
  
  @NonNull
  protected RunT _this() { return (RunT)this; }
  
  public int compareTo(@NonNull RunT that) {
    int res = this.number - that.number;
    if (res == 0)
      return getParent().getFullName().compareTo(that.getParent().getFullName()); 
    return res;
  }
  
  @Exported
  public long getQueueId() { return this.queueId; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void setQueueId(long queueId) { this.queueId = queueId; }
  
  @Exported
  @CheckForNull
  public Result getResult() { return this.result; }
  
  public void setResult(@NonNull Result r) {
    if (this.state != State.BUILDING)
      throw new IllegalStateException("cannot change build result while in " + this.state); 
    if (this.result == null || r.isWorseThan(this.result)) {
      this.result = r;
      LOGGER.log(Level.FINE, "" + this + " in " + this + ": result is set to " + getRootDir(), LOGGER.isLoggable(Level.FINER) ? new Exception() : null);
    } 
  }
  
  @NonNull
  public List<BuildBadgeAction> getBadgeActions() {
    List<BuildBadgeAction> r = getActions(BuildBadgeAction.class);
    if (isKeepLog()) {
      r = new ArrayList<BuildBadgeAction>(r);
      r.add(new KeepLogBuildBadge(this));
    } 
    return r;
  }
  
  @Exported
  public boolean isBuilding() { return (this.state.compareTo(State.POST_PRODUCTION) < 0); }
  
  @Exported
  public boolean isInProgress() { return (this.state.equals(State.BUILDING) || this.state.equals(State.POST_PRODUCTION)); }
  
  public boolean isLogUpdated() { return (this.state.compareTo(State.COMPLETED) < 0); }
  
  @Exported
  @CheckForNull
  public Executor getExecutor() { return (this instanceof Queue.Executable) ? Executor.of((Queue.Executable)this) : null; }
  
  @CheckForNull
  public Executor getOneOffExecutor() {
    for (Computer c : Jenkins.get().getComputers()) {
      for (Executor e : c.getOneOffExecutors()) {
        if (e.getCurrentExecutable() == this)
          return e; 
      } 
    } 
    return null;
  }
  
  @NonNull
  public final Charset getCharset() {
    if (this.charset == null)
      return Charset.defaultCharset(); 
    return Charset.forName(this.charset);
  }
  
  @NonNull
  public List<Cause> getCauses() {
    CauseAction a = (CauseAction)getAction(CauseAction.class);
    if (a == null)
      return Collections.emptyList(); 
    return Collections.unmodifiableList(a.getCauses());
  }
  
  @CheckForNull
  public <T extends Cause> T getCause(Class<T> type) {
    for (Cause c : getCauses()) {
      if (type.isInstance(c))
        return (T)(Cause)type.cast(c); 
    } 
    return null;
  }
  
  @Exported
  public final boolean isKeepLog() { return (getWhyKeepLog() != null); }
  
  @CheckForNull
  public String getWhyKeepLog() {
    if (this.keepLog)
      return Messages.Run_MarkedExplicitly(); 
    return null;
  }
  
  @NonNull
  public JobT getParent() { return (JobT)this.project; }
  
  @Exported
  @NonNull
  public Calendar getTimestamp() {
    GregorianCalendar c = new GregorianCalendar();
    c.setTimeInMillis(this.timestamp);
    return c;
  }
  
  @NonNull
  public final Date getTime() { return new Date(this.timestamp); }
  
  public final long getTimeInMillis() { return this.timestamp; }
  
  public final long getStartTimeInMillis() {
    if (this.startTime == 0L)
      return this.timestamp; 
    return this.startTime;
  }
  
  @Exported
  @CheckForNull
  public String getDescription() { return this.description; }
  
  @Deprecated
  @CheckForNull
  public String getTruncatedDescription() {
    if (TRUNCATED_DESCRIPTION_LIMIT < 0)
      return this.description; 
    if (TRUNCATED_DESCRIPTION_LIMIT == 0)
      return ""; 
    int maxDescrLength = TRUNCATED_DESCRIPTION_LIMIT;
    String localDescription = this.description;
    if (localDescription == null || localDescription.length() < maxDescrLength)
      return localDescription; 
    String ending = "...";
    int sz = localDescription.length(), maxTruncLength = maxDescrLength - "...".length();
    boolean inTag = false;
    int displayChars = 0;
    int lastTruncatablePoint = -1;
    for (int i = 0; i < sz; i++) {
      char ch = localDescription.charAt(i);
      if (ch == '<') {
        inTag = true;
      } else if (ch == '>') {
        inTag = false;
        if (displayChars <= maxTruncLength)
          lastTruncatablePoint = i + 1; 
      } 
      displayChars++;
      if (!inTag && displayChars <= maxTruncLength && ch == ' ')
        lastTruncatablePoint = i; 
    } 
    String truncDesc = localDescription;
    if (lastTruncatablePoint == -1)
      lastTruncatablePoint = maxTruncLength; 
    if (displayChars >= maxDescrLength)
      truncDesc = truncDesc.substring(0, lastTruncatablePoint) + "..."; 
    return truncDesc;
  }
  
  @NonNull
  public String getTimestampString() {
    long duration = (new GregorianCalendar()).getTimeInMillis() - this.timestamp;
    return Util.getTimeSpanString(duration);
  }
  
  @NonNull
  public String getTimestampString2() { return Util.XS_DATETIME_FORMATTER.format(new Date(this.timestamp)); }
  
  @NonNull
  public String getDurationString() {
    if (hasntStartedYet())
      return Messages.Run_NotStartedYet(); 
    if (isBuilding())
      return Messages.Run_InProgressDuration(
          Util.getTimeSpanString(System.currentTimeMillis() - this.startTime)); 
    return Util.getTimeSpanString(this.duration);
  }
  
  @Exported
  public long getDuration() { return this.duration; }
  
  @NonNull
  public BallColor getIconColor() {
    BallColor baseColor;
    if (!isBuilding())
      return (getResult()).color; 
    RunT pb = (RunT)getPreviousBuild();
    if (pb == null) {
      baseColor = BallColor.NOTBUILT;
    } else {
      baseColor = pb.getIconColor();
    } 
    return baseColor.anime();
  }
  
  public boolean hasntStartedYet() { return (this.state == State.NOT_STARTED); }
  
  @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"}, justification = "see JENKINS-45892")
  public String toString() {
    if (this.project == null)
      return "<broken data JENKINS-45892>"; 
    return this.project.getFullName() + " #" + this.project.getFullName();
  }
  
  @Exported
  public String getFullDisplayName() { return this.project.getFullDisplayName() + " " + this.project.getFullDisplayName(); }
  
  @Exported
  public String getDisplayName() { return (this.displayName != null) ? this.displayName : ("#" + this.number); }
  
  public boolean hasCustomDisplayName() { return (this.displayName != null); }
  
  public void setDisplayName(String value) throws IOException {
    checkPermission(UPDATE);
    this.displayName = value;
    save();
  }
  
  @Exported(visibility = 2)
  public int getNumber() { return this.number; }
  
  @NonNull
  protected BuildReference<RunT> createReference() { return new BuildReference(getId(), _this()); }
  
  protected void dropLinks() throws IOException {
    if (this.nextBuild != null)
      this.nextBuild.previousBuild = this.previousBuild; 
    if (this.previousBuild != null)
      this.previousBuild.nextBuild = this.nextBuild; 
  }
  
  @CheckForNull
  public RunT getPreviousBuild() { return (RunT)this.previousBuild; }
  
  @CheckForNull
  public final RunT getPreviousCompletedBuild() {
    RunT r = (RunT)getPreviousBuild();
    while (r != null && r.isBuilding())
      r = (RunT)r.getPreviousBuild(); 
    return r;
  }
  
  @CheckForNull
  public final RunT getPreviousBuildInProgress() {
    RunT answer;
    if (this.previousBuildInProgress == this)
      return null; 
    List<RunT> fixUp = new ArrayList<RunT>();
    RunT r = (RunT)_this();
    while (true) {
      RunT n = (RunT)r.previousBuildInProgress;
      if (n == null) {
        n = (RunT)r.getPreviousBuild();
        fixUp.add(r);
      } 
      if (r == n || n == null) {
        answer = null;
        break;
      } 
      if (n.isBuilding()) {
        answer = n;
        break;
      } 
      fixUp.add(r);
      r = n;
    } 
    for (Iterator iterator = fixUp.iterator(); iterator.hasNext(); ) {
      RunT f = (RunT)(Run)iterator.next();
      f.previousBuildInProgress = (answer == null) ? f : answer;
    } 
    return answer;
  }
  
  @CheckForNull
  public RunT getPreviousBuiltBuild() {
    RunT r = (RunT)getPreviousBuild();
    while (r != null && (r.getResult() == null || r.getResult() == Result.NOT_BUILT))
      r = (RunT)r.getPreviousBuild(); 
    return r;
  }
  
  @CheckForNull
  public RunT getPreviousNotFailedBuild() {
    RunT r = (RunT)getPreviousBuild();
    while (r != null && r.getResult() == Result.FAILURE)
      r = (RunT)r.getPreviousBuild(); 
    return r;
  }
  
  @CheckForNull
  public RunT getPreviousFailedBuild() {
    RunT r = (RunT)getPreviousBuild();
    while (r != null && r.getResult() != Result.FAILURE)
      r = (RunT)r.getPreviousBuild(); 
    return r;
  }
  
  @CheckForNull
  public RunT getPreviousSuccessfulBuild() {
    RunT r = (RunT)getPreviousBuild();
    while (r != null && r.getResult() != Result.SUCCESS)
      r = (RunT)r.getPreviousBuild(); 
    return r;
  }
  
  @NonNull
  public List<RunT> getPreviousBuildsOverThreshold(int numberOfBuilds, @NonNull Result threshold) {
    RunT r = (RunT)getPreviousBuild();
    if (r != null)
      return r.getBuildsOverThreshold(numberOfBuilds, threshold); 
    return new ArrayList(numberOfBuilds);
  }
  
  @NonNull
  protected List<RunT> getBuildsOverThreshold(int numberOfBuilds, @NonNull Result threshold) {
    List<RunT> builds = new ArrayList<RunT>(numberOfBuilds);
    RunT r = (RunT)_this();
    while (r != null && builds.size() < numberOfBuilds) {
      if (!r.isBuilding() && r
        .getResult() != null && r.getResult().isBetterOrEqualTo(threshold))
        builds.add(r); 
      r = (RunT)r.getPreviousBuild();
    } 
    return builds;
  }
  
  @CheckForNull
  public RunT getNextBuild() { return (RunT)this.nextBuild; }
  
  @NonNull
  public String getUrl() {
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req != null) {
      String seed = Functions.getNearestAncestorUrl(req, this);
      if (seed != null)
        return seed.substring(req.getContextPath().length() + 1) + "/"; 
    } 
    return this.project.getUrl() + this.project.getUrl() + "/";
  }
  
  @Exported(visibility = 2, name = "url")
  @Deprecated
  @NonNull
  public final String getAbsoluteUrl() {
    return this.project.getAbsoluteUrl() + this.project.getAbsoluteUrl() + "/";
  }
  
  @NonNull
  public final String getSearchUrl() {
    return "" + getNumber() + "/";
  }
  
  @Exported
  @NonNull
  public String getId() { return (this.id != null) ? this.id : Integer.toString(this.number); }
  
  @NonNull
  public File getRootDir() { return new File(this.project.getBuildDir(), Integer.toString(this.number)); }
  
  @NonNull
  public final ArtifactManager getArtifactManager() { return (this.artifactManager != null) ? this.artifactManager : new StandardArtifactManager(this); }
  
  @NonNull
  public final ArtifactManager pickArtifactManager() {
    if (this.artifactManager != null)
      return this.artifactManager; 
    for (ArtifactManagerFactory f : ArtifactManagerConfiguration.get().getArtifactManagerFactories()) {
      ArtifactManager mgr = f.managerFor(this);
      if (mgr != null) {
        this.artifactManager = mgr;
        save();
        return mgr;
      } 
    } 
    return new StandardArtifactManager(this);
  }
  
  @Deprecated
  public File getArtifactsDir() { return new File(getRootDir(), "archive"); }
  
  @Exported
  @NonNull
  public List<Artifact> getArtifacts() { return getArtifactsUpTo(2147483647); }
  
  @NonNull
  public List<Artifact> getArtifactsUpTo(int artifactsNumber) {
    SerializableArtifactList sal;
    VirtualFile root = getArtifactManager().root();
    try {
      sal = (SerializableArtifactList)root.run(new AddArtifacts(root, artifactsNumber));
    } catch (IOException x) {
      LOGGER.log(Level.WARNING, null, x);
      sal = new SerializableArtifactList();
    } 
    ArtifactList r = new ArtifactList(this);
    r.updateFrom(sal);
    r.computeDisplayName();
    return r;
  }
  
  public boolean getHasArtifacts() { return !getArtifactsUpTo(1).isEmpty(); }
  
  private static int addArtifacts(@NonNull VirtualFile dir, @NonNull String path, @NonNull String pathHref, @NonNull SerializableArtifactList r, @CheckForNull SerializableArtifact parent, int upTo) throws IOException {
    VirtualFile[] kids = dir.list();
    Arrays.sort(kids);
    int n = 0;
    for (VirtualFile sub : kids) {
      SerializableArtifact a;
      String child = sub.getName();
      String childPath = path + path;
      String childHref = pathHref + pathHref;
      String length = sub.isFile() ? String.valueOf(sub.length()) : "";
      boolean collapsed = (kids.length == 1 && parent != null);
      if (collapsed) {
        a = new SerializableArtifact(parent.name + "/" + parent.name, childPath, sub.isDirectory() ? null : childHref, length, parent.treeNodeId);
        r.tree.put(a, (String)r.tree.remove(parent));
      } else {
        a = new SerializableArtifact(child, childPath, sub.isDirectory() ? null : childHref, length, "n" + ++r.idSeq);
        r.tree.put(a, (parent != null) ? parent.treeNodeId : null);
      } 
      if (sub.isDirectory()) {
        n += addArtifacts(sub, childPath + "/", childHref + "/", r, a, upTo - n);
        if (n >= upTo)
          break; 
      } else {
        r.add(collapsed ? new SerializableArtifact(child, a.relativePath, a.href, length, a.treeNodeId) : a);
        if (++n >= upTo)
          break; 
      } 
    } 
    return n;
  }
  
  public static final int LIST_CUTOFF = Integer.parseInt(SystemProperties.getString("hudson.model.Run.ArtifactList.listCutoff", "20"));
  
  @Exported(name = "fingerprint", inline = true, visibility = -1)
  @NonNull
  public Collection<Fingerprint> getBuildFingerprints() {
    Fingerprinter.FingerprintAction fingerprintAction = (Fingerprinter.FingerprintAction)getAction(Fingerprinter.FingerprintAction.class);
    if (fingerprintAction != null)
      return fingerprintAction.getFingerprints().values(); 
    return Collections.emptyList();
  }
  
  @Deprecated
  @NonNull
  public File getLogFile() {
    File rawF = new File(getRootDir(), "log");
    if (rawF.isFile())
      return rawF; 
    File gzF = new File(getRootDir(), "log.gz");
    if (gzF.isFile())
      return gzF; 
    return rawF;
  }
  
  @NonNull
  public InputStream getLogInputStream() throws IOException {
    File logFile = getLogFile();
    if (logFile.exists())
      try {
        InputStream fis = Files.newInputStream(logFile.toPath(), new OpenOption[0]);
        if (logFile.getName().endsWith(".gz"))
          return new GZIPInputStream(fis); 
        return fis;
      } catch (InvalidPathException e) {
        throw new IOException(e);
      }  
    String message = "No such file: " + logFile;
    return new ByteArrayInputStream((this.charset != null) ? message.getBytes(this.charset) : message.getBytes(Charset.defaultCharset()));
  }
  
  @NonNull
  public Reader getLogReader() throws IOException {
    if (this.charset == null)
      return new InputStreamReader(getLogInputStream(), Charset.defaultCharset()); 
    return new InputStreamReader(getLogInputStream(), this.charset);
  }
  
  @SuppressFBWarnings(value = {"RV_RETURN_VALUE_IGNORED"}, justification = "method signature does not permit plumbing through the return value")
  public void writeLogTo(long offset, @NonNull XMLOutput out) throws IOException {
    long start = offset;
    if (offset > 0L) {
      BufferedInputStream bufferedInputStream = new BufferedInputStream(getLogInputStream());
      try {
        if (offset == bufferedInputStream.skip(offset)) {
          int r;
          do {
            r = bufferedInputStream.read();
            start = (r == -1) ? 0L : (start + 1L);
          } while (r != -1 && r != 10);
        } 
        bufferedInputStream.close();
      } catch (Throwable throwable) {
        try {
          bufferedInputStream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } 
    getLogText().writeHtmlTo(start, out.asWriter());
  }
  
  public void writeWholeLogTo(@NonNull OutputStream out) throws IOException, InterruptedException {
    long pos = 0L;
    AnnotatedLargeText logText = getLogText();
    pos = logText.writeLogTo(pos, out);
    while (!logText.isComplete()) {
      Thread.sleep(1000L);
      logText = getLogText();
      pos = logText.writeLogTo(pos, out);
    } 
  }
  
  @NonNull
  public AnnotatedLargeText getLogText() { return new AnnotatedLargeText(getLogFile(), getCharset(), !isLogUpdated(), this); }
  
  @NonNull
  protected SearchIndexBuilder makeSearchIndex() {
    SearchIndexBuilder builder = super.makeSearchIndex().add("console").add("changes");
    for (Action a : getAllActions()) {
      if (a.getIconFileName() != null)
        builder.add(a.getUrlName()); 
    } 
    return builder;
  }
  
  @NonNull
  public Api getApi() { return new Api(this); }
  
  @NonNull
  public ACL getACL() { return getParent().getACL(); }
  
  public void deleteArtifacts() throws IOException {
    try {
      getArtifactManager().delete();
    } catch (InterruptedException x) {
      throw new IOException(x);
    } 
  }
  
  public void delete() throws IOException {
    synchronized (this) {
      if (this.isPendingDelete)
        return; 
      this.isPendingDelete = true;
    } 
    File rootDir = getRootDir();
    if (!rootDir.isDirectory()) {
      LOGGER.warning(String.format("%s: %s looks to have already been deleted, assuming build dir was already cleaned up", new Object[] { this, rootDir }));
      RunListener.fireDeleted(this);
      synchronized (this) {
        removeRunFromParent();
      } 
      return;
    } 
    RunListener.fireDeleted(this);
    if (this.artifactManager != null)
      deleteArtifacts(); 
    synchronized (this) {
      File tmp = new File(rootDir.getParentFile(), "." + rootDir.getName());
      if (tmp.exists())
        Util.deleteRecursive(tmp); 
      try {
        Files.move(
            Util.fileToPath(rootDir), 
            Util.fileToPath(tmp), new CopyOption[] { StandardCopyOption.ATOMIC_MOVE });
      } catch (UnsupportedOperationException|SecurityException ex) {
        throw new IOException("" + rootDir + " is in use", ex);
      } 
      Util.deleteRecursive(tmp);
      if (tmp.exists())
        tmp.deleteOnExit(); 
      LOGGER.log(Level.FINE, "{0}: {1} successfully deleted", new Object[] { this, rootDir });
      removeRunFromParent();
    } 
  }
  
  private void removeRunFromParent() throws IOException { getParent().removeRun(this); }
  
  static void reportCheckpoint(@NonNull CheckPoint id) {
    RunExecution exec = RunnerStack.INSTANCE.peek();
    if (exec == null)
      return; 
    exec.checkpoints.report(id);
  }
  
  static void waitForCheckpoint(@NonNull CheckPoint id, @CheckForNull BuildListener listener, @CheckForNull String waiter) throws InterruptedException {
    while (true) {
      RunExecution exec = RunnerStack.INSTANCE.peek();
      if (exec == null)
        return; 
      Run b = exec.getBuild().getPreviousBuildInProgress();
      if (b == null)
        return; 
      RunExecution runner = b.runner;
      if (runner == null) {
        Thread.sleep(0L);
        continue;
      } 
      if (runner.checkpoints.waitForCheckPoint(id, listener, waiter))
        break; 
    } 
  }
  
  @Deprecated
  protected final void run(@NonNull Runner job) { execute(job); }
  
  protected final void execute(@NonNull RunExecution job) {
    if (this.result != null)
      return; 
    logger = null;
    listener = null;
    this.runner = job;
    onStartBuilding();
    try {
      start = System.currentTimeMillis();
      try {
        try {
          Computer computer = Computer.currentComputer();
          Charset charset = null;
          if (computer != null) {
            charset = computer.getDefaultCharset();
            this.charset = charset.name();
          } 
          logger = createLogger();
          listener = createBuildListener(job, logger, charset);
          listener.started(getCauses());
          Authentication auth = Jenkins.getAuthentication2();
          if (auth.equals(ACL.SYSTEM2)) {
            listener.getLogger().println(Messages.Run_running_as_SYSTEM());
          } else {
            String id = auth.getName();
            if (!auth.equals(Jenkins.ANONYMOUS2)) {
              User usr = User.getById(id, false);
              if (usr != null)
                id = ModelHyperlinkNote.encodeTo(usr); 
            } 
            listener.getLogger().println(Messages.Run_running_as_(id));
          } 
          RunListener.fireStarted(this, listener);
          setResult(job.run(listener));
          LOGGER.log(Level.FINE, "{0} main build action completed: {1}", new Object[] { this, this.result });
          CheckPoint.MAIN_COMPLETED.report();
        } catch (AbortException e) {
          this.result = Result.FAILURE;
          listener.error(e.getMessage());
          LOGGER.log(Level.FINE, "Build " + this + " aborted", e);
        } catch (RunnerAbortedException e) {
          this.result = Result.FAILURE;
          LOGGER.log(Level.FINE, "Build " + this + " aborted", e);
        } catch (InterruptedException e) {
          this.result = Executor.currentExecutor().abortResult();
          listener.getLogger().println(Messages.Run_BuildAborted());
          Executor.currentExecutor().recordCauseOfInterruption(this, listener);
          LOGGER.log(Level.INFO, "" + this + " aborted", e);
        } catch (Throwable e) {
          handleFatalBuildProblem(listener, e);
          this.result = Result.FAILURE;
        } 
        job.post((BuildListener)Objects.requireNonNull(listener));
      } catch (Throwable e) {
        handleFatalBuildProblem(listener, e);
        this.result = Result.FAILURE;
      } finally {
        long end = System.currentTimeMillis();
        this.duration = Math.max(end - start, 0L);
        LOGGER.log(Level.FINER, "moving into POST_PRODUCTION on {0}", this);
        this.state = State.POST_PRODUCTION;
        if (listener != null) {
          RunListener.fireCompleted(this, listener);
          try {
            job.cleanUp(listener);
          } catch (Exception e) {
            handleFatalBuildProblem(listener, e);
          } 
          listener.finished(this.result);
          listener.closeQuietly();
        } 
        try {
          save();
        } catch (IOException e) {
          LOGGER.log(Level.SEVERE, "Failed to save build record", e);
        } 
      } 
      try {
        getParent().logRotate();
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Failed to rotate log", e);
      } 
    } finally {
      onEndBuilding();
      if (logger != null)
        try {
          logger.close();
        } catch (IOException x) {
          LOGGER.log(Level.WARNING, "failed to close log for " + this, x);
        }  
    } 
  }
  
  private OutputStream createLogger() throws IOException {
    try {
      return Files.newOutputStream(getLogFile().toPath(), new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND });
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  private StreamBuildListener createBuildListener(@NonNull RunExecution job, OutputStream logger, Charset charset) throws IOException, InterruptedException {
    RunT build = (RunT)job.getBuild();
    for (ConsoleLogFilter filter : ConsoleLogFilter.all())
      logger = filter.decorateLogger(build, logger); 
    if (this.project instanceof BuildableItemWithBuildWrappers && build instanceof AbstractBuild) {
      BuildableItemWithBuildWrappers biwbw = (BuildableItemWithBuildWrappers)this.project;
      for (BuildWrapper bw : biwbw.getBuildWrappersList())
        logger = bw.decorateLogger((AbstractBuild)build, logger); 
    } 
    return new StreamBuildListener(logger, charset);
  }
  
  @Deprecated
  public final void updateSymlinks(@NonNull TaskListener listener) throws InterruptedException {}
  
  private void handleFatalBuildProblem(@NonNull BuildListener listener, @NonNull Throwable e) {
    if (listener != null) {
      LOGGER.log(Level.FINE, getDisplayName() + " failed to build", e);
      if (e instanceof IOException)
        Util.displayIOException((IOException)e, listener); 
      Functions.printStackTrace(e, listener.fatalError(e.getMessage()));
    } else {
      LOGGER.log(Level.SEVERE, getDisplayName() + " failed to build and we don't even have a listener", e);
    } 
  }
  
  protected void onStartBuilding() throws IOException {
    LOGGER.log(Level.FINER, "moving to BUILDING on {0}", this);
    this.state = State.BUILDING;
    this.startTime = System.currentTimeMillis();
    if (this.runner != null)
      RunnerStack.INSTANCE.push(this.runner); 
    RunListener.fireInitialize(this);
  }
  
  protected void onEndBuilding() throws IOException {
    this.state = State.COMPLETED;
    LOGGER.log(Level.FINER, "moving to COMPLETED on {0}", this);
    if (this.runner != null) {
      this.runner.checkpoints.allDone();
      this.runner = null;
      RunnerStack.INSTANCE.pop();
    } 
    if (this.result == null) {
      this.result = Result.FAILURE;
      LOGGER.log(Level.WARNING, "{0}: No build result is set, so marking as failure. This should not happen.", this);
    } 
    RunListener.fireFinalized(this);
  }
  
  public void save() throws IOException {
    if (BulkChange.contains(this))
      return; 
    getDataFile().write(this);
    SaveableListener.fireOnChange(this, getDataFile());
  }
  
  @NonNull
  private XmlFile getDataFile() { return new XmlFile(XSTREAM, new File(getRootDir(), "build.xml")); }
  
  protected Object writeReplace() { return XmlFile.replaceIfNotAtTopLevel(this, () -> new Replacer(this)); }
  
  @Deprecated
  @NonNull
  public String getLog() { return Util.loadFile(getLogFile(), getCharset()); }
  
  @NonNull
  public List<String> getLog(int maxLines) throws IOException {
    long filePointer;
    if (maxLines == 0)
      return Collections.emptyList(); 
    int lines = 0;
    List<String> lastLines = new ArrayList<String>(Math.min(maxLines, 128));
    List<Byte> bytes = new ArrayList<Byte>();
    RandomAccessFile fileHandler = new RandomAccessFile(getLogFile(), "r");
    try {
      long fileLength = fileHandler.length() - 1L;
      for (filePointer = fileLength; filePointer != -1L && maxLines != lines; filePointer--) {
        fileHandler.seek(filePointer);
        byte readByte = fileHandler.readByte();
        if (readByte == 10) {
          if (filePointer < fileLength) {
            lines++;
            lastLines.add(convertBytesToString(bytes));
            bytes.clear();
          } 
        } else if (readByte != 13) {
          bytes.add(Byte.valueOf(readByte));
        } 
      } 
      fileHandler.close();
    } catch (Throwable throwable) {
      try {
        fileHandler.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    if (lines != maxLines)
      lastLines.add(convertBytesToString(bytes)); 
    Collections.reverse(lastLines);
    if (lines == maxLines)
      lastLines.set(0, "[...truncated " + Functions.humanReadableByteSize(filePointer) + "...]"); 
    return ConsoleNote.removeNotes(lastLines);
  }
  
  private String convertBytesToString(List<Byte> bytes) {
    Collections.reverse(bytes);
    Byte[] byteArray = (Byte[])bytes.toArray(new Byte[0]);
    return new String(ArrayUtils.toPrimitive(byteArray), getCharset());
  }
  
  public void doBuildStatus(StaplerRequest req, StaplerResponse rsp) throws IOException { rsp.sendRedirect2(req.getContextPath() + "/images/48x48/" + req.getContextPath()); }
  
  @NonNull
  public String getBuildStatusUrl() { return getIconColor().getImage(); }
  
  public String getBuildStatusIconClassName() { return getIconColor().getIconClassName(); }
  
  @NonNull
  public Summary getBuildStatusSummary() {
    RunT failedBuild, since;
    if (isBuilding())
      return new Summary(false, Messages.Run_Summary_Unknown()); 
    ResultTrend trend = ResultTrend.getResultTrend(this);
    for (StatusSummarizer summarizer : ExtensionList.lookup(StatusSummarizer.class)) {
      Summary summary = summarizer.summarize(this, trend);
      if (summary != null)
        return summary; 
    } 
    switch (null.$SwitchMap$hudson$model$ResultTrend[trend.ordinal()]) {
      case 1:
        return new Summary(false, Messages.Run_Summary_Aborted());
      case 2:
        return new Summary(false, Messages.Run_Summary_NotBuilt());
      case 3:
        return new Summary(true, Messages.Run_Summary_BrokenSinceThisBuild());
      case 4:
        since = (RunT)getPreviousNotFailedBuild();
        if (since == null)
          return new Summary(false, Messages.Run_Summary_BrokenForALongTime()); 
        failedBuild = (RunT)since.getNextBuild();
        return new Summary(false, Messages.Run_Summary_BrokenSince(failedBuild.getDisplayName()));
      case 5:
      case 6:
        return new Summary(false, Messages.Run_Summary_Unstable());
      case 7:
        return new Summary(true, Messages.Run_Summary_Unstable());
      case 8:
        return new Summary(false, Messages.Run_Summary_Stable());
      case 9:
        return new Summary(false, Messages.Run_Summary_BackToNormal());
    } 
    return new Summary(false, Messages.Run_Summary_Unknown());
  }
  
  @NonNull
  public DirectoryBrowserSupport doArtifact() {
    if (Functions.isArtifactsPermissionEnabled())
      checkPermission(ARTIFACTS); 
    return new DirectoryBrowserSupport(this, getArtifactManager().root(), Messages.Run_ArtifactsBrowserTitle(this.project.getDisplayName(), getDisplayName()), "package.png", true);
  }
  
  public void doBuildNumber(StaplerResponse rsp) throws IOException {
    rsp.setContentType("text/plain");
    rsp.setCharacterEncoding("US-ASCII");
    rsp.setStatus(200);
    rsp.getWriter().print(this.number);
  }
  
  public void doBuildTimestamp(StaplerRequest req, StaplerResponse rsp, @QueryParameter String format) throws IOException {
    rsp.setContentType("text/plain");
    rsp.setCharacterEncoding("US-ASCII");
    rsp.setStatus(200);
    DateFormat df = (format == null) ? DateFormat.getDateTimeInstance(3, 3, Locale.ENGLISH) : new SimpleDateFormat(format, req.getLocale());
    rsp.getWriter().print(df.format(getTime()));
  }
  
  public void doConsoleText(StaplerRequest req, StaplerResponse rsp) throws IOException {
    rsp.setContentType("text/plain;charset=UTF-8");
    InputStream input = getLogInputStream();
    try {
      OutputStream os = rsp.getCompressedOutputStream(req);
      try {
        PlainTextConsoleOutputStream out = new PlainTextConsoleOutputStream(os);
        try {
          IOUtils.copy(input, out);
          out.close();
        } catch (Throwable throwable) {
          try {
            out.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
        if (os != null)
          os.close(); 
      } catch (Throwable throwable) {
        if (os != null)
          try {
            os.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
      if (input != null)
        input.close(); 
    } catch (Throwable throwable) {
      if (input != null)
        try {
          input.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  @Deprecated
  public void doProgressiveLog(StaplerRequest req, StaplerResponse rsp) throws IOException { getLogText().doProgressText(req, rsp); }
  
  public boolean canToggleLogKeep() {
    if (!this.keepLog && isKeepLog())
      return false; 
    return true;
  }
  
  @RequirePOST
  public void doToggleLogKeep(StaplerRequest req, StaplerResponse rsp) throws IOException {
    keepLog(!this.keepLog);
    rsp.forwardToPreviousPage(req);
  }
  
  @CLIMethod(name = "keep-build")
  public final void keepLog() throws IOException { keepLog(true); }
  
  public void keepLog(boolean newValue) throws IOException {
    checkPermission(newValue ? UPDATE : DELETE);
    this.keepLog = newValue;
    save();
  }
  
  @RequirePOST
  public void doDoDelete(StaplerRequest req, StaplerResponse rsp) throws IOException {
    checkPermission(DELETE);
    String why = getWhyKeepLog();
    if (why != null) {
      sendError(Messages.Run_UnableToDelete(getFullDisplayName(), why), req, rsp);
      return;
    } 
    try {
      delete();
    } catch (IOException ex) {
      req.setAttribute("stackTraces", Functions.printThrowable(ex));
      req.getView(this, "delete-retry.jelly").forward(req, rsp);
      return;
    } 
    rsp.sendRedirect2(req.getContextPath() + "/" + req.getContextPath());
  }
  
  public void setDescription(String description) throws IOException {
    checkPermission(UPDATE);
    this.description = description;
    save();
  }
  
  @RequirePOST
  public void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException {
    setDescription(req.getParameter("description"));
    rsp.sendRedirect(".");
  }
  
  @Deprecated
  public Map<String, String> getEnvVars() {
    LOGGER.log(Level.WARNING, "deprecated call to Run.getEnvVars\n\tat {0}", (new Throwable()).getStackTrace()[1]);
    try {
      return getEnvironment(new LogTaskListener(LOGGER, Level.INFO));
    } catch (IOException|InterruptedException e) {
      return new EnvVars();
    } 
  }
  
  @Deprecated
  public EnvVars getEnvironment() throws IOException, InterruptedException {
    LOGGER.log(Level.WARNING, "deprecated call to Run.getEnvironment\n\tat {0}", (new Throwable()).getStackTrace()[1]);
    return getEnvironment(new LogTaskListener(LOGGER, Level.INFO));
  }
  
  @NonNull
  public EnvVars getEnvironment(@NonNull TaskListener listener) throws IOException, InterruptedException {
    Computer c = Computer.currentComputer();
    Node n = (c == null) ? null : c.getNode();
    EnvVars env = getParent().getEnvironment(n, listener);
    env.putAll(getCharacteristicEnvVars());
    for (EnvironmentContributor ec : EnvironmentContributor.all().reverseView())
      ec.buildEnvironmentFor(this, env, listener); 
    if (!(this instanceof AbstractBuild))
      for (EnvironmentContributingAction a : getActions(EnvironmentContributingAction.class))
        a.buildEnvironment(this, env);  
    return env;
  }
  
  @NonNull
  public final EnvVars getCharacteristicEnvVars() throws IOException, InterruptedException {
    EnvVars env = getParent().getCharacteristicEnvVars();
    env.put("BUILD_NUMBER", String.valueOf(this.number));
    env.put("BUILD_ID", getId());
    env.put("BUILD_TAG", "jenkins-" + getParent().getFullName().replace('/', '-') + "-" + this.number);
    return env;
  }
  
  @NonNull
  public String getExternalizableId() { return this.project.getFullName() + "#" + this.project.getFullName(); }
  
  @CheckForNull
  public static Run<?, ?> fromExternalizableId(String id) throws IllegalArgumentException, AccessDeniedException {
    int number, hash = id.lastIndexOf('#');
    if (hash <= 0)
      throw new IllegalArgumentException("Invalid id"); 
    String jobName = id.substring(0, hash);
    try {
      number = Integer.parseInt(id.substring(hash + 1));
    } catch (NumberFormatException x) {
      throw new IllegalArgumentException(x);
    } 
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j == null)
      return null; 
    Job<?, ?> job = (Job)j.getItemByFullName(jobName, Job.class);
    if (job == null)
      return null; 
    return job.getBuildByNumber(number);
  }
  
  @Exported
  public long getEstimatedDuration() { return this.project.getEstimatedDuration(); }
  
  @POST
  @NonNull
  public HttpResponse doConfigSubmit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
    checkPermission(UPDATE);
    BulkChange bc = new BulkChange(this);
    try {
      JSONObject json = req.getSubmittedForm();
      submit(json);
      bc.commit();
      bc.close();
    } catch (Throwable throwable) {
      try {
        bc.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    return FormApply.success(".");
  }
  
  protected void submit(JSONObject json) throws IOException {
    setDisplayName(Util.fixEmptyAndTrim(json.getString("displayName")));
    setDescription(json.getString("description"));
  }
  
  public static final XStream XSTREAM = new XStream2();
  
  public static final XStream2 XSTREAM2 = (XStream2)XSTREAM;
  
  private static final Logger LOGGER;
  
  public static final Comparator<Run> ORDER_BY_DATE;
  
  public static final FeedAdapter<Run> FEED_ADAPTER;
  
  public static final FeedAdapter<Run> FEED_ADAPTER_LATEST;
  
  public static final PermissionGroup PERMISSIONS;
  
  public static final Permission DELETE;
  
  public static final Permission UPDATE;
  
  public static final Permission ARTIFACTS;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK;
  
  static  {
    XSTREAM.alias("build", FreeStyleBuild.class);
    XSTREAM.registerConverter(Result.conv);
    LOGGER = Logger.getLogger(Run.class.getName());
    ORDER_BY_DATE = new Object();
    FEED_ADAPTER = new DefaultFeedAdapter();
    FEED_ADAPTER_LATEST = new Object();
    PERMISSIONS = new PermissionGroup(Run.class, Messages._Run_Permissions_Title());
    DELETE = new Permission(PERMISSIONS, "Delete", Messages._Run_DeletePermission_Description(), Permission.DELETE, PermissionScope.RUN);
    UPDATE = new Permission(PERMISSIONS, "Update", Messages._Run_UpdatePermission_Description(), Permission.UPDATE, PermissionScope.RUN);
    ARTIFACTS = new Permission(PERMISSIONS, "Artifacts", Messages._Run_ArtifactsPermission_Description(), null, Functions.isArtifactsPermissionEnabled(), new PermissionScope[] { PermissionScope.RUN });
    SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(Run.class.getName() + ".skipPermissionCheck");
  }
  
  public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
    Object returnedResult = super.getDynamic(token, req, rsp);
    if (returnedResult == null) {
      for (Action action : getTransientActions()) {
        String urlName = action.getUrlName();
        if (urlName == null)
          continue; 
        if (urlName.equals(token))
          return action; 
      } 
      returnedResult = new RedirectUp();
    } 
    return returnedResult;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK) {
      if (!getParent().hasPermission(Item.DISCOVER))
        return null; 
      getParent().checkPermission(Item.READ);
    } 
    return this;
  }
}
