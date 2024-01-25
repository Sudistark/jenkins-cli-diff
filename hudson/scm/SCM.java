package hudson.scm;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.AbortException;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Api;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.TopLevelItemDescriptor;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class SCM extends Object implements Describable<SCM>, ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(SCM.class.getName());
  
  private static boolean useAutoBrowserHolder = SystemProperties.getBoolean(SCM.class.getName() + ".useAutoBrowserHolder");
  
  @Deprecated
  private AutoBrowserHolder autoBrowserHolder;
  
  public Api getApi() { return new Api(this); }
  
  @CheckForNull
  public RepositoryBrowser<?> getBrowser() { return null; }
  
  @Exported
  public String getType() { return getClass().getName(); }
  
  @Exported(name = "browser")
  @CheckForNull
  public final RepositoryBrowser<?> getEffectiveBrowser() {
    RepositoryBrowser<?> b = getBrowser();
    if (b != null)
      return b; 
    if (useAutoBrowserHolder) {
      if (this.autoBrowserHolder == null)
        this.autoBrowserHolder = new AutoBrowserHolder(this); 
      return this.autoBrowserHolder.get();
    } 
    try {
      return guessBrowser();
    } catch (RuntimeException x) {
      LOGGER.log(Level.WARNING, null, x);
      return null;
    } 
  }
  
  public boolean supportsPolling() { return true; }
  
  public boolean requiresWorkspaceForPolling() { return true; }
  
  public boolean processWorkspaceBeforeDeletion(@NonNull Job<?, ?> project, @NonNull FilePath workspace, @NonNull Node node) throws IOException, InterruptedException {
    if (project instanceof AbstractProject)
      return processWorkspaceBeforeDeletion((AbstractProject)project, workspace, node); 
    return true;
  }
  
  @Deprecated
  public boolean processWorkspaceBeforeDeletion(AbstractProject<?, ?> project, FilePath workspace, Node node) throws IOException, InterruptedException {
    if (Util.isOverridden(SCM.class, getClass(), "processWorkspaceBeforeDeletion", new Class[] { Job.class, FilePath.class, Node.class }))
      return processWorkspaceBeforeDeletion(project, workspace, node); 
    return true;
  }
  
  @Deprecated
  public boolean pollChanges(AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException { throw new AbstractMethodError("you must override compareRemoteRevisionWith"); }
  
  @CheckForNull
  public SCMRevisionState calcRevisionsFromBuild(@NonNull Run<?, ?> build, @Nullable FilePath workspace, @Nullable Launcher launcher, @NonNull TaskListener listener) throws IOException, InterruptedException {
    if (build instanceof AbstractBuild && Util.isOverridden(SCM.class, getClass(), "calcRevisionsFromBuild", new Class[] { AbstractBuild.class, Launcher.class, TaskListener.class }))
      return calcRevisionsFromBuild((AbstractBuild)build, launcher, listener); 
    throw new AbstractMethodError("you must override the new calcRevisionsFromBuild overload");
  }
  
  @Deprecated
  public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException { return calcRevisionsFromBuild(build, (launcher != null) ? build.getWorkspace() : null, launcher, listener); }
  
  @Deprecated
  public SCMRevisionState _calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws IOException, InterruptedException { return calcRevisionsFromBuild(build, launcher, listener); }
  
  public PollingResult compareRemoteRevisionWith(@NonNull Job<?, ?> project, @Nullable Launcher launcher, @Nullable FilePath workspace, @NonNull TaskListener listener, @NonNull SCMRevisionState baseline) throws IOException, InterruptedException {
    if (project instanceof AbstractProject && 
      Util.isOverridden(SCM.class, 
        
        getClass(), "compareRemoteRevisionWith", new Class[] { AbstractProject.class, Launcher.class, FilePath.class, TaskListener.class, SCMRevisionState.class }))
      return compareRemoteRevisionWith((AbstractProject)project, launcher, workspace, listener, baseline); 
    throw new AbstractMethodError("you must override the new overload of compareRemoteRevisionWith");
  }
  
  @Deprecated
  protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline) throws IOException, InterruptedException { return compareRemoteRevisionWith(project, launcher, workspace, listener, baseline); }
  
  public final PollingResult poll(AbstractProject<?, ?> project, Launcher launcher, FilePath workspace, TaskListener listener, SCMRevisionState baseline) throws IOException, InterruptedException {
    if (is1_346OrLater()) {
      SCMRevisionState baseline2;
      if (baseline != SCMRevisionState.NONE) {
        baseline2 = baseline;
      } else {
        baseline2 = calcRevisionsFromBuild(project.getLastBuild(), launcher, listener);
      } 
      return compareRemoteRevisionWith(project, launcher, workspace, listener, baseline2);
    } 
    return pollChanges(project, launcher, workspace, listener) ? PollingResult.SIGNIFICANT : PollingResult.NO_CHANGES;
  }
  
  private boolean is1_346OrLater() {
    for (Class<?> c = getClass(); c != SCM.class; c = c.getSuperclass()) {
      try {
        c.getDeclaredMethod("compareRemoteRevisionWith", new Class[] { AbstractProject.class, Launcher.class, FilePath.class, TaskListener.class, SCMRevisionState.class });
        return true;
      } catch (NoSuchMethodException e) {
        try {
          c.getDeclaredMethod("compareRemoteRevisionWith", new Class[] { Job.class, Launcher.class, FilePath.class, TaskListener.class, SCMRevisionState.class });
          return true;
        } catch (NoSuchMethodException noSuchMethodException) {}
      } 
    } 
    return false;
  }
  
  @NonNull
  public String getKey() { return getType(); }
  
  public void checkout(@NonNull Run<?, ?> build, @NonNull Launcher launcher, @NonNull FilePath workspace, @NonNull TaskListener listener, @CheckForNull File changelogFile, @CheckForNull SCMRevisionState baseline) throws IOException, InterruptedException {
    if (build instanceof AbstractBuild && listener instanceof BuildListener && 
      
      Util.isOverridden(SCM.class, 
        
        getClass(), "checkout", new Class[] { AbstractBuild.class, Launcher.class, FilePath.class, BuildListener.class, File.class })) {
      if (changelogFile == null) {
        changelogFile = File.createTempFile("changelog", ".xml");
        try {
          if (!checkout((AbstractBuild)build, launcher, workspace, (BuildListener)listener, changelogFile))
            throw new AbortException(); 
        } finally {
          Util.deleteFile(changelogFile);
        } 
      } else if (!checkout((AbstractBuild)build, launcher, workspace, (BuildListener)listener, changelogFile)) {
        throw new AbortException();
      } 
    } else {
      throw new AbstractMethodError("you must override the new overload of checkout");
    } 
  }
  
  @Deprecated
  public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener, @NonNull File changelogFile) throws IOException, InterruptedException {
    AbstractBuild<?, ?> prev = build.getPreviousBuild();
    checkout(build, launcher, workspace, listener, changelogFile, (prev != null) ? (SCMRevisionState)prev.getAction(SCMRevisionState.class) : null);
    return true;
  }
  
  public void postCheckout(@NonNull Run<?, ?> build, @NonNull Launcher launcher, @NonNull FilePath workspace, @NonNull TaskListener listener) throws IOException, InterruptedException {
    if (build instanceof AbstractBuild && listener instanceof BuildListener)
      postCheckout((AbstractBuild)build, launcher, workspace, (BuildListener)listener); 
  }
  
  @Deprecated
  public void postCheckout(AbstractBuild<?, ?> build, Launcher launcher, FilePath workspace, BuildListener listener) throws IOException, InterruptedException {
    if (Util.isOverridden(SCM.class, getClass(), "postCheckout", new Class[] { Run.class, Launcher.class, FilePath.class, TaskListener.class }))
      postCheckout(build, launcher, workspace, listener); 
  }
  
  public void buildEnvironment(@NonNull Run<?, ?> build, @NonNull Map<String, String> env) {
    if (build instanceof AbstractBuild)
      buildEnvVars((AbstractBuild)build, env); 
  }
  
  @Deprecated
  public void buildEnvVars(AbstractBuild<?, ?> build, Map<String, String> env) {
    if (Util.isOverridden(SCM.class, getClass(), "buildEnvironment", new Class[] { Run.class, Map.class }))
      buildEnvironment(build, env); 
  }
  
  public FilePath getModuleRoot(FilePath workspace, AbstractBuild build) { return getModuleRoot(workspace); }
  
  @Deprecated
  public FilePath getModuleRoot(FilePath workspace) {
    if (Util.isOverridden(SCM.class, getClass(), "getModuleRoot", new Class[] { FilePath.class, AbstractBuild.class }))
      return getModuleRoot(workspace, null); 
    return workspace;
  }
  
  public FilePath[] getModuleRoots(FilePath workspace, AbstractBuild build) {
    if (Util.isOverridden(SCM.class, getClass(), "getModuleRoots", new Class[] { FilePath.class }))
      return getModuleRoots(workspace); 
    return new FilePath[] { getModuleRoot(workspace, build) };
  }
  
  @Deprecated
  public FilePath[] getModuleRoots(FilePath workspace) {
    if (Util.isOverridden(SCM.class, getClass(), "getModuleRoots", new Class[] { FilePath.class, AbstractBuild.class }))
      return getModuleRoots(workspace, null); 
    return new FilePath[] { getModuleRoot(workspace) };
  }
  
  public SCMDescriptor<?> getDescriptor() { return (SCMDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  @Deprecated
  protected final boolean createEmptyChangeLog(File changelogFile, BuildListener listener, String rootTag) {
    try {
      createEmptyChangeLog(changelogFile, listener, rootTag);
      return true;
    } catch (IOException e) {
      Functions.printStackTrace(e, listener.error(e.getMessage()));
      return false;
    } 
  }
  
  protected final void createEmptyChangeLog(@NonNull File changelogFile, @NonNull TaskListener listener, @NonNull String rootTag) throws IOException {
    Writer w = Files.newBufferedWriter(Util.fileToPath(changelogFile), Charset.defaultCharset(), new java.nio.file.OpenOption[0]);
    try {
      w.write("<" + rootTag + "/>");
      if (w != null)
        w.close(); 
    } catch (Throwable throwable) {
      if (w != null)
        try {
          w.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  protected final String nullify(String s) {
    if (s == null)
      return null; 
    if (s.trim().isEmpty())
      return null; 
    return s;
  }
  
  public static final PermissionGroup PERMISSIONS = new PermissionGroup(SCM.class, Messages._SCM_Permissions_Title());
  
  public static final Permission TAG = new Permission(PERMISSIONS, "Tag", Messages._SCM_TagPermission_Description(), Permission.CREATE, PermissionScope.ITEM);
  
  public static DescriptorExtensionList<SCM, SCMDescriptor<?>> all() { return Jenkins.get().getDescriptorList(SCM.class); }
  
  public static List<SCMDescriptor<?>> _for(@CheckForNull Job project) {
    if (project == null)
      return all(); 
    Descriptor pd = Jenkins.get().getDescriptor(project.getClass());
    List<SCMDescriptor<?>> r = new ArrayList<SCMDescriptor<?>>();
    for (SCMDescriptor<?> scmDescriptor : all()) {
      if (!scmDescriptor.isApplicable(project))
        continue; 
      if (pd instanceof TopLevelItemDescriptor) {
        TopLevelItemDescriptor apd = (TopLevelItemDescriptor)pd;
        if (!apd.isApplicable(scmDescriptor))
          continue; 
      } 
      r.add(scmDescriptor);
    } 
    return r;
  }
  
  @Deprecated
  public static List<SCMDescriptor<?>> _for(AbstractProject project) { return _for(project); }
  
  @CheckForNull
  public RepositoryBrowser<?> guessBrowser() { return null; }
  
  public abstract ChangeLogParser createChangeLogParser();
}
