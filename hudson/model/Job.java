package hudson.model;

import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.BulkChange;
import hudson.EnvVars;
import hudson.ExtensionPoint;
import hudson.PermalinkList;
import hudson.Util;
import hudson.cli.declarative.CLIResolver;
import hudson.model.listeners.ItemListener;
import hudson.scm.ChangeLogSet;
import hudson.scm.SCM;
import hudson.search.QuickSilver;
import hudson.search.SearchIndexBuilder;
import hudson.security.ACL;
import hudson.tasks.LogRotator;
import hudson.util.AlternativeUiTextProvider;
import hudson.util.CopyOnWriteList;
import hudson.util.DescribableList;
import hudson.util.FormApply;
import hudson.util.Graph;
import hudson.util.RunList;
import hudson.util.TextFile;
import hudson.widgets.HistoryWidget;
import hudson.widgets.Widget;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.BuildDiscarder;
import jenkins.model.BuildDiscarderProperty;
import jenkins.model.DirectlyModifiableTopLevelItemGroup;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithChildren;
import jenkins.model.ModelObjectWithContextMenu;
import jenkins.model.ProjectNamingStrategy;
import jenkins.model.RunIdMigrator;
import jenkins.scm.RunWithSCM;
import jenkins.security.HexStringConfidentialKey;
import jenkins.triggers.SCMTriggerItem;
import jenkins.widgets.HasWidgets;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.jvnet.localizer.Localizable;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.stapler.StaplerOverridable;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

@BridgeMethodsAdded
public abstract class Job<JobT extends Job<JobT, RunT>, RunT extends Run<JobT, RunT>> extends AbstractItem implements ExtensionPoint, StaplerOverridable, ModelObjectWithChildren, HasWidgets {
  private static final Logger LOGGER = Logger.getLogger(Job.class.getName());
  
  private Integer cachedBuildHealthReportsBuildNumber = null;
  
  private List<HealthReport> cachedBuildHealthReports = null;
  
  boolean keepDependencies;
  
  protected CopyOnWriteList<JobProperty<? super JobT>> properties = new CopyOnWriteList();
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public RunIdMigrator runIdMigrator;
  
  protected Job(ItemGroup parent, String name) { super(parent, name); }
  
  public void save() throws IOException {
    super.save();
    this.holdOffBuildUntilSave = this.holdOffBuildUntilUserSave;
  }
  
  public void onCreatedFromScratch() throws IOException {
    super.onCreatedFromScratch();
    this.runIdMigrator = new RunIdMigrator();
    this.runIdMigrator.created(getBuildDir());
  }
  
  public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
    super.onLoad(parent, name);
    File buildDir = getBuildDir();
    this.runIdMigrator = new RunIdMigrator();
    this.runIdMigrator.migrate(buildDir, Jenkins.get().getRootDir());
    TextFile f = getNextBuildNumberFile();
    if (f.exists()) {
      try {
        synchronized (this) {
          this.nextBuildNumber = Integer.parseInt(f.readTrim());
        } 
      } catch (NumberFormatException e) {
        LOGGER.log(Level.WARNING, "Corruption in {0}: {1}", new Object[] { f, e });
        if (!(this instanceof jenkins.model.lazy.LazyBuildMixIn.LazyLoadingJob)) {
          RunT lB = (RunT)getLastBuild();
          synchronized (this) {
            this.nextBuildNumber = (lB != null) ? (lB.getNumber() + 1) : 1;
          } 
          saveNextBuildNumber();
        } 
      } 
    } else {
      saveNextBuildNumber();
    } 
    if (this.properties == null)
      this.properties = new CopyOnWriteList(); 
    for (JobProperty p : this.properties)
      p.setOwner(this); 
  }
  
  public void onCopiedFrom(Item src) {
    super.onCopiedFrom(src);
    synchronized (this) {
      this.nextBuildNumber = 1;
      this.holdOffBuildUntilUserSave = true;
      this.holdOffBuildUntilSave = this.holdOffBuildUntilUserSave;
    } 
  }
  
  TextFile getNextBuildNumberFile() { return new TextFile(new File(getRootDir(), "nextBuildNumber")); }
  
  public boolean isHoldOffBuildUntilSave() { return this.holdOffBuildUntilSave; }
  
  protected void saveNextBuildNumber() throws IOException {
    if (this.nextBuildNumber == 0)
      this.nextBuildNumber = 1; 
    getNextBuildNumberFile().write(String.valueOf(this.nextBuildNumber) + "\n");
  }
  
  @Exported
  public boolean isInQueue() { return false; }
  
  @Exported
  public Queue.Item getQueueItem() { return null; }
  
  public boolean isBuilding() {
    RunT b = (RunT)getLastBuild();
    return (b != null && b.isBuilding());
  }
  
  public boolean isLogUpdated() {
    RunT b = (RunT)getLastBuild();
    return (b != null && b.isLogUpdated());
  }
  
  public String getPronoun() { return AlternativeUiTextProvider.get(PRONOUN, this, Messages.Job_Pronoun()); }
  
  public boolean isNameEditable() { return true; }
  
  @Exported
  public boolean isKeepDependencies() { return this.keepDependencies; }
  
  public int assignBuildNumber() throws IOException {
    int r = this.nextBuildNumber++;
    saveNextBuildNumber();
    return r;
  }
  
  @Exported
  public int getNextBuildNumber() throws IOException { return this.nextBuildNumber; }
  
  public EnvVars getCharacteristicEnvVars() {
    EnvVars env = new EnvVars();
    env.put("JENKINS_SERVER_COOKIE", SERVER_COOKIE.get());
    env.put("HUDSON_SERVER_COOKIE", SERVER_COOKIE.get());
    env.put("JOB_NAME", getFullName());
    env.put("JOB_BASE_NAME", getName());
    return env;
  }
  
  @NonNull
  public EnvVars getEnvironment(@CheckForNull Node node, @NonNull TaskListener listener) throws IOException, InterruptedException {
    EnvVars env = new EnvVars();
    if (node != null) {
      Computer computer = node.toComputer();
      if (computer != null) {
        env = computer.getEnvironment();
        env.putAll(computer.buildEnvironment(listener));
      } 
    } 
    env.putAll(getCharacteristicEnvVars());
    env.put("CLASSPATH", "");
    for (EnvironmentContributor ec : EnvironmentContributor.all().reverseView())
      ec.buildEnvironmentFor(this, env, listener); 
    return env;
  }
  
  public void updateNextBuildNumber(int next) throws IOException {
    RunT lb = (RunT)getLastBuild();
    if ((lb != null) ? (next > lb.getNumber()) : (next > 0)) {
      this.nextBuildNumber = next;
      saveNextBuildNumber();
    } 
  }
  
  public BuildDiscarder getBuildDiscarder() {
    BuildDiscarderProperty prop = (BuildDiscarderProperty)_getProperty(BuildDiscarderProperty.class);
    return (prop != null) ? prop.getStrategy() : this.logRotator;
  }
  
  public void setBuildDiscarder(BuildDiscarder bd) throws IOException {
    BulkChange bc = new BulkChange(this);
    try {
      removeProperty(BuildDiscarderProperty.class);
      if (bd != null)
        addProperty(new BuildDiscarderProperty(bd)); 
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
  }
  
  @Deprecated
  public LogRotator getLogRotator() {
    BuildDiscarder buildDiscarder = getBuildDiscarder();
    return (buildDiscarder instanceof LogRotator) ? (LogRotator)buildDiscarder : null;
  }
  
  @Deprecated
  public void setLogRotator(LogRotator logRotator) throws IOException { setBuildDiscarder(logRotator); }
  
  public void logRotate() throws IOException {
    BuildDiscarder bd = getBuildDiscarder();
    if (bd != null)
      bd.perform(this); 
  }
  
  public boolean supportsLogRotator() { return true; }
  
  protected SearchIndexBuilder makeSearchIndex() {
    return super.makeSearchIndex().add(new Object(this))


















      
      .add("configure", new String[] { "config", "configure" });
  }
  
  public Collection<? extends Job> getAllJobs() { return Set.of(this); }
  
  public void addProperty(JobProperty<? super JobT> jobProp) throws IOException {
    jobProp.setOwner(this);
    this.properties.add(jobProp);
    save();
  }
  
  public void removeProperty(JobProperty<? super JobT> jobProp) throws IOException {
    this.properties.remove(jobProp);
    save();
  }
  
  public <T extends JobProperty> T removeProperty(Class<T> clazz) throws IOException {
    for (JobProperty<? super JobT> p : this.properties) {
      if (clazz.isInstance(p)) {
        removeProperty(p);
        return (T)(JobProperty)clazz.cast(p);
      } 
    } 
    return null;
  }
  
  public Map<JobPropertyDescriptor, JobProperty<? super JobT>> getProperties() {
    Map result = Descriptor.toMap(this.properties);
    if (this.logRotator != null)
      result.put(Jenkins.get().getDescriptorByType(BuildDiscarderProperty.DescriptorImpl.class), new BuildDiscarderProperty(this.logRotator)); 
    return result;
  }
  
  @Exported(name = "property", inline = true)
  public List<JobProperty<? super JobT>> getAllProperties() { return this.properties.getView(); }
  
  public <T extends JobProperty> T getProperty(Class<T> clazz) throws IOException {
    if (clazz == BuildDiscarderProperty.class && this.logRotator != null)
      return (T)(JobProperty)clazz.cast(new BuildDiscarderProperty(this.logRotator)); 
    return (T)_getProperty(clazz);
  }
  
  private <T extends JobProperty> T _getProperty(Class<T> clazz) throws IOException {
    for (JobProperty p : this.properties) {
      if (clazz.isInstance(p))
        return (T)(JobProperty)clazz.cast(p); 
    } 
    return null;
  }
  
  public JobProperty getProperty(String className) {
    for (JobProperty p : this.properties) {
      if (p.getClass().getName().equals(className))
        return p; 
    } 
    return null;
  }
  
  public Collection<?> getOverrides() {
    List<Object> r = new ArrayList<Object>();
    for (JobProperty<? super JobT> p : this.properties)
      r.addAll(p.getJobOverrides()); 
    return r;
  }
  
  @Deprecated(forRemoval = true, since = "2.410")
  protected HistoryWidget createHistoryWidget() { return new HistoryWidget(this, getBuilds(), HISTORY_ADAPTER); }
  
  public static final HistoryWidget.Adapter<Run> HISTORY_ADAPTER = new Object();
  
  public void renameTo(String newName) throws IOException {
    File oldBuildDir = getBuildDir();
    super.renameTo(newName);
    File newBuildDir = getBuildDir();
    if (Files.isDirectory(Util.fileToPath(oldBuildDir), new java.nio.file.LinkOption[0]) && !Files.isDirectory(Util.fileToPath(newBuildDir), new java.nio.file.LinkOption[0])) {
      Util.createDirectories(Util.fileToPath(newBuildDir.getParentFile()), new java.nio.file.attribute.FileAttribute[0]);
      Files.move(Util.fileToPath(oldBuildDir), Util.fileToPath(newBuildDir), new java.nio.file.CopyOption[0]);
    } 
  }
  
  public void movedTo(DirectlyModifiableTopLevelItemGroup destination, AbstractItem newItem, File destDir) throws IOException {
    File oldBuildDir = getBuildDir();
    super.movedTo(destination, newItem, destDir);
    File newBuildDir = getBuildDir();
    if (oldBuildDir.isDirectory())
      FileUtils.moveDirectory(oldBuildDir, newBuildDir); 
  }
  
  public void delete() throws IOException {
    super.delete();
    Util.deleteRecursive(getBuildDir());
  }
  
  @Exported
  public abstract boolean isBuildable();
  
  @Exported(name = "allBuilds", visibility = -2)
  @WithBridgeMethods({List.class})
  public RunList<RunT> getBuilds() { return RunList.fromRuns(_getRuns().values()); }
  
  @Exported(name = "builds")
  public RunList<RunT> getNewBuilds() { return getBuilds().limit(100); }
  
  public List<RunT> getBuilds(Fingerprint.RangeSet rs) {
    List<RunT> builds = new ArrayList<RunT>();
    for (Fingerprint.Range r : rs.getRanges()) {
      for (RunT b = (RunT)getNearestBuild(r.start); b != null && b.getNumber() < r.end; b = (RunT)b.getNextBuild())
        builds.add(b); 
    } 
    return builds;
  }
  
  public SortedMap<Integer, RunT> getBuildsAsMap() { return Collections.unmodifiableSortedMap(_getRuns()); }
  
  public RunT getBuild(String id) {
    for (Iterator iterator = _getRuns().values().iterator(); iterator.hasNext(); ) {
      RunT r = (RunT)(Run)iterator.next();
      if (r.getId().equals(id))
        return r; 
    } 
    return null;
  }
  
  public RunT getBuildByNumber(int n) { return (RunT)(Run)_getRuns().get(Integer.valueOf(n)); }
  
  @Deprecated
  @WithBridgeMethods({List.class})
  public RunList<RunT> getBuildsByTimestamp(long start, long end) { return getBuilds().byTimestamp(start, end); }
  
  @CLIResolver
  public RunT getBuildForCLI(@Argument(required = true, metaVar = "BUILD#", usage = "Build number") String id) {
    try {
      int n = Integer.parseInt(id);
      RunT r = (RunT)getBuildByNumber(n);
      if (r == null)
        throw new CmdLineException(null, "No such build '#" + n + "' exists"); 
      return r;
    } catch (NumberFormatException e) {
      throw new CmdLineException(null, id + "is not a number", e);
    } 
  }
  
  public RunT getNearestBuild(int n) {
    SortedMap<Integer, ? extends RunT> m = _getRuns().headMap(Integer.valueOf(n - 1));
    if (m.isEmpty())
      return null; 
    return (RunT)(Run)m.get(m.lastKey());
  }
  
  public RunT getNearestOldBuild(int n) {
    SortedMap<Integer, ? extends RunT> m = _getRuns().tailMap(Integer.valueOf(n));
    if (m.isEmpty())
      return null; 
    return (RunT)(Run)m.get(m.firstKey());
  }
  
  public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
    try {
      return getBuildByNumber(Integer.parseInt(token));
    } catch (NumberFormatException e) {
      for (Widget w : getWidgets()) {
        if (w.getUrlName().equals(token))
          return w; 
      } 
      for (PermalinkProjectAction.Permalink p : getPermalinks()) {
        if (p.getId().equals(token))
          return p.resolve(this); 
      } 
      return super.getDynamic(token, req, rsp);
    } 
  }
  
  public File getBuildDir() {
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j == null)
      return new File(getRootDir(), "builds"); 
    return j.getBuildDirFor(this);
  }
  
  protected abstract SortedMap<Integer, ? extends RunT> _getRuns();
  
  protected abstract void removeRun(RunT paramRunT);
  
  @Exported
  @QuickSilver
  public RunT getLastBuild() {
    SortedMap<Integer, ? extends RunT> runs = _getRuns();
    if (runs.isEmpty())
      return null; 
    return (RunT)(Run)runs.get(runs.firstKey());
  }
  
  @Exported
  @QuickSilver
  public RunT getFirstBuild() {
    SortedMap<Integer, ? extends RunT> runs = _getRuns();
    if (runs.isEmpty())
      return null; 
    return (RunT)(Run)runs.get(runs.lastKey());
  }
  
  @Exported
  @QuickSilver
  public RunT getLastSuccessfulBuild() { return (RunT)PermalinkProjectAction.Permalink.LAST_SUCCESSFUL_BUILD.resolve(this); }
  
  @Exported
  @QuickSilver
  public RunT getLastUnsuccessfulBuild() { return (RunT)PermalinkProjectAction.Permalink.LAST_UNSUCCESSFUL_BUILD.resolve(this); }
  
  @Exported
  @QuickSilver
  public RunT getLastUnstableBuild() { return (RunT)PermalinkProjectAction.Permalink.LAST_UNSTABLE_BUILD.resolve(this); }
  
  @Exported
  @QuickSilver
  public RunT getLastStableBuild() { return (RunT)PermalinkProjectAction.Permalink.LAST_STABLE_BUILD.resolve(this); }
  
  @Exported
  @QuickSilver
  public RunT getLastFailedBuild() { return (RunT)PermalinkProjectAction.Permalink.LAST_FAILED_BUILD.resolve(this); }
  
  @Exported
  @QuickSilver
  public RunT getLastCompletedBuild() { return (RunT)PermalinkProjectAction.Permalink.LAST_COMPLETED_BUILD.resolve(this); }
  
  public List<RunT> getLastBuildsOverThreshold(int numberOfBuilds, Result threshold) {
    RunT r = (RunT)getLastBuild();
    return r.getBuildsOverThreshold(numberOfBuilds, threshold);
  }
  
  protected List<RunT> getEstimatedDurationCandidates() {
    List<RunT> candidates = new ArrayList<RunT>(3);
    RunT lastSuccessful = (RunT)getLastSuccessfulBuild();
    int lastSuccessfulNumber = -1;
    if (lastSuccessful != null) {
      candidates.add(lastSuccessful);
      lastSuccessfulNumber = lastSuccessful.getNumber();
    } 
    int i = 0;
    RunT r = (RunT)getLastBuild();
    List<RunT> fallbackCandidates = new ArrayList<RunT>(3);
    while (r != null && candidates.size() < 3 && i < 6) {
      if (!r.isBuilding() && r.getResult() != null && r.getNumber() != lastSuccessfulNumber) {
        Result result = r.getResult();
        if (result.isBetterOrEqualTo(Result.UNSTABLE)) {
          candidates.add(r);
        } else if (result.isCompleteBuild()) {
          fallbackCandidates.add(r);
        } 
      } 
      i++;
      r = (RunT)r.getPreviousBuild();
    } 
    while (candidates.size() < 3 && 
      !fallbackCandidates.isEmpty()) {
      RunT run = (RunT)(Run)fallbackCandidates.remove(0);
      candidates.add(run);
    } 
    return candidates;
  }
  
  public long getEstimatedDuration() {
    List<RunT> builds = getEstimatedDurationCandidates();
    if (builds.isEmpty())
      return -1L; 
    long totalDuration = 0L;
    for (Iterator iterator = builds.iterator(); iterator.hasNext(); ) {
      RunT b = (RunT)(Run)iterator.next();
      totalDuration += b.getDuration();
    } 
    if (totalDuration == 0L)
      return -1L; 
    return Math.round(totalDuration / builds.size());
  }
  
  public PermalinkList getPermalinks() {
    PermalinkList permalinks = new PermalinkList(PermalinkProjectAction.Permalink.BUILTIN);
    for (PermalinkProjectAction ppa : getActions(PermalinkProjectAction.class))
      permalinks.addAll(ppa.getPermalinks()); 
    return permalinks;
  }
  
  public void doRssChangelog(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    List<FeedItem> entries = new ArrayList<FeedItem>();
    String scmDisplayName = "";
    if (this instanceof SCMTriggerItem) {
      SCMTriggerItem scmItem = (SCMTriggerItem)this;
      List<String> scmNames = new ArrayList<String>();
      for (SCM s : scmItem.getSCMs())
        scmNames.add(s.getDescriptor().getDisplayName()); 
      scmDisplayName = " " + String.join(", ", scmNames);
    } 
    for (RunT r = (RunT)getLastBuild(); r != null; r = (RunT)r.getPreviousBuild()) {
      int idx = 0;
      if (r instanceof RunWithSCM)
        for (ChangeLogSet<? extends ChangeLogSet.Entry> c : ((RunWithSCM)r).getChangeSets()) {
          for (ChangeLogSet.Entry e : c)
            entries.add(new FeedItem(this, e, idx++)); 
        }  
    } 
    RSS.forwardToRss(
        getDisplayName() + getDisplayName() + " changes", 
        getUrl() + "changes", entries, new Object(this), req, rsp);
  }
  
  public ModelObjectWithContextMenu.ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
    ModelObjectWithContextMenu.ContextMenu menu = new ModelObjectWithContextMenu.ContextMenu();
    for (PermalinkProjectAction.Permalink p : getPermalinks()) {
      if (p.resolve(this) != null)
        menu.add(p.getId(), p.getDisplayName()); 
    } 
    return menu;
  }
  
  @Exported(visibility = 2, name = "color")
  public BallColor getIconColor() {
    RunT lastBuild = (RunT)getLastBuild();
    while (lastBuild != null && lastBuild.hasntStartedYet())
      lastBuild = (RunT)lastBuild.getPreviousBuild(); 
    if (lastBuild != null)
      return lastBuild.getIconColor(); 
    return BallColor.NOTBUILT;
  }
  
  public HealthReport getBuildHealth() {
    List<HealthReport> reports = getBuildHealthReports();
    return reports.isEmpty() ? new HealthReport() : (HealthReport)reports.get(0);
  }
  
  @Exported(name = "healthReport")
  public List<HealthReport> getBuildHealthReports() {
    List<HealthReport> reports = new ArrayList<HealthReport>();
    RunT lastBuild = (RunT)getLastBuild();
    if (lastBuild != null && lastBuild.isBuilding())
      lastBuild = (RunT)lastBuild.getPreviousBuild(); 
    if (this.cachedBuildHealthReportsBuildNumber != null && this.cachedBuildHealthReports != null && lastBuild != null && this.cachedBuildHealthReportsBuildNumber

      
      .intValue() == lastBuild
      .getNumber()) {
      reports.addAll(this.cachedBuildHealthReports);
    } else if (lastBuild != null) {
      for (HealthReportingAction healthReportingAction : lastBuild
        .getActions(HealthReportingAction.class)) {
        HealthReport report = healthReportingAction.getBuildHealth();
        if (report != null) {
          if (report.isAggregateReport()) {
            reports.addAll(report.getAggregatedReports());
            continue;
          } 
          reports.add(report);
        } 
      } 
      HealthReport report = getBuildStabilityHealthReport();
      if (report != null)
        if (report.isAggregateReport()) {
          reports.addAll(report.getAggregatedReports());
        } else {
          reports.add(report);
        }  
      Collections.sort(reports);
      this.cachedBuildHealthReportsBuildNumber = Integer.valueOf(lastBuild.getNumber());
      this.cachedBuildHealthReports = new ArrayList(reports);
    } 
    return reports;
  }
  
  private HealthReport getBuildStabilityHealthReport() {
    int failCount = 0;
    int totalCount = 0;
    RunT i = (RunT)getLastBuild();
    RunT u = (RunT)getLastFailedBuild();
    if (i != null && u == null)
      return new HealthReport(100, Messages._Job_BuildStability(Messages._Job_NoRecentBuildFailed())); 
    if (i != null && u.getNumber() <= i.getNumber()) {
      SortedMap<Integer, ? extends RunT> runs = _getRuns();
      if (runs instanceof RunMap) {
        RunMap<RunT> runMap = (RunMap)runs;
        for (int index = i.getNumber(); index > u.getNumber() && totalCount < 5; index--) {
          if (runMap.runExists(index))
            totalCount++; 
        } 
        if (totalCount < 5)
          i = u; 
      } 
    } 
    while (totalCount < 5 && i != null) {
      switch (null.$SwitchMap$hudson$model$BallColor[i.getIconColor().ordinal()]) {
        case 1:
        case 2:
          totalCount++;
          break;
        case 3:
          failCount++;
          totalCount++;
          break;
      } 
      i = (RunT)i.getPreviousBuild();
    } 
    if (totalCount > 0) {
      Localizable description;
      int score = (int)(100.0D * (totalCount - failCount) / totalCount);
      if (failCount == 0) {
        description = Messages._Job_NoRecentBuildFailed();
      } else if (totalCount == failCount) {
        description = Messages._Job_AllRecentBuildFailed();
      } else {
        description = Messages._Job_NOfMFailed(Integer.valueOf(failCount), Integer.valueOf(totalCount));
      } 
      return new HealthReport(score, Messages._Job_BuildStability(description));
    } 
    return null;
  }
  
  @POST
  public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(CONFIGURE);
    this.description = req.getParameter("description");
    JSONObject json = req.getSubmittedForm();
    try {
      BulkChange bc = new BulkChange(this);
      try {
        setDisplayName(json.optString("displayNameOrNull"));
        this.logRotator = null;
        DescribableList<JobProperty<?>, JobPropertyDescriptor> t = new DescribableList<JobProperty<?>, JobPropertyDescriptor>(NOOP, getAllProperties());
        JSONObject jsonProperties = json.optJSONObject("properties");
        if (jsonProperties != null) {
          t.rebuild(req, jsonProperties, JobPropertyDescriptor.getPropertyDescriptors(getClass()));
        } else {
          t.clear();
        } 
        this.properties.clear();
        for (JobProperty p : t) {
          p.setOwner(this);
          this.properties.add(p);
        } 
        submit(req, rsp);
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
      ItemListener.fireOnUpdated(this);
      ProjectNamingStrategy namingStrategy = Jenkins.get().getProjectNamingStrategy();
      if (namingStrategy.isForceExistingJobs())
        namingStrategy.checkName(getParent().getFullName(), this.name); 
      FormApply.success(".").generateResponse(req, rsp, null);
    } catch (JSONException e) {
      LOGGER.log(Level.WARNING, "failed to parse " + json, e);
      sendError(e, req, rsp);
    } 
  }
  
  protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {}
  
  public void doDescription(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    if (req.getMethod().equals("GET")) {
      rsp.setContentType("text/plain;charset=UTF-8");
      rsp.getWriter().write(Util.fixNull(getDescription()));
      return;
    } 
    if (req.getMethod().equals("POST")) {
      checkPermission(CONFIGURE);
      if (req.getParameter("description") != null) {
        setDescription(req.getParameter("description"));
        rsp.sendError(204);
        return;
      } 
    } 
    rsp.sendError(400);
  }
  
  public void doBuildStatus(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { rsp.sendRedirect2(req.getContextPath() + "/images/48x48/" + req.getContextPath()); }
  
  public String getBuildStatusUrl() { return getIconColor().getImage(); }
  
  public String getBuildStatusIconClassName() { return getIconColor().getIconClassName(); }
  
  public Graph getBuildTimeGraph() { return new Object(this, getLastBuildTime(), 500, 400); }
  
  private Calendar getLastBuildTime() {
    RunT lastBuild = (RunT)getLastBuild();
    if (lastBuild == null) {
      GregorianCalendar neverBuiltCalendar = new GregorianCalendar();
      neverBuiltCalendar.setTimeInMillis(0L);
      return neverBuiltCalendar;
    } 
    return lastBuild.getTimestamp();
  }
  
  @Deprecated
  @RequirePOST
  public void doDoRename(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    String newName = req.getParameter("newName");
    doConfirmRename(newName).generateResponse(req, rsp, null);
  }
  
  protected void checkRename(String newName) throws IOException {
    if (isBuilding())
      throw new Failure(Messages.Job_NoRenameWhileBuilding()); 
  }
  
  public void doRssAll(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (all builds)", getUrl(), getBuilds().newBuilds()); }
  
  public void doRssFailed(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { RSS.rss(req, rsp, "Jenkins:" + getDisplayName() + " (failed builds)", getUrl(), getBuilds().failureOnly().newBuilds()); }
  
  public ACL getACL() { return Jenkins.get().getAuthorizationStrategy().getACL(this); }
  
  public BuildTimelineWidget getTimeline() { return new BuildTimelineWidget(getBuilds()); }
  
  private static final HexStringConfidentialKey SERVER_COOKIE = new HexStringConfidentialKey(Job.class, "serverCookie", 16);
}
