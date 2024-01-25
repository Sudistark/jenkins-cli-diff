package hudson.tasks;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.tasks._maven.MavenConsoleAnnotator;
import hudson.util.ArgumentListBuilder;
import hudson.util.VariableResolver;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;
import jenkins.mvn.GlobalMavenConfig;
import jenkins.mvn.GlobalSettingsProvider;
import jenkins.mvn.SettingsProvider;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;

public class Maven extends Builder {
  public final String targets;
  
  public final String mavenName;
  
  public final String jvmOptions;
  
  public final String pom;
  
  public final String properties;
  
  public boolean usePrivateRepository;
  
  private SettingsProvider settings;
  
  private GlobalSettingsProvider globalSettings;
  
  @NonNull
  private Boolean injectBuildVariables;
  
  private static final String MAVEN_1_INSTALLATION_COMMON_FILE = "bin/maven";
  
  private static final String MAVEN_2_INSTALLATION_COMMON_FILE = "bin/mvn";
  
  private static final Pattern S_PATTERN = Pattern.compile("(^| )-s ");
  
  private static final Pattern GS_PATTERN = Pattern.compile("(^| )-gs ");
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static DescriptorImpl DESCRIPTOR;
  
  public Maven(String targets, String name) { this(targets, name, null, null, null, false, null, null); }
  
  public Maven(String targets, String name, String pom, String properties, String jvmOptions) { this(targets, name, pom, properties, jvmOptions, false, null, null); }
  
  public Maven(String targets, String name, String pom, String properties, String jvmOptions, boolean usePrivateRepository) { this(targets, name, pom, properties, jvmOptions, usePrivateRepository, null, null); }
  
  public Maven(String targets, String name, String pom, String properties, String jvmOptions, boolean usePrivateRepository, SettingsProvider settings, GlobalSettingsProvider globalSettings) { this(targets, name, pom, properties, jvmOptions, usePrivateRepository, settings, globalSettings, false); }
  
  @DataBoundConstructor
  public Maven(String targets, String name, String pom, String properties, String jvmOptions, boolean usePrivateRepository, SettingsProvider settings, GlobalSettingsProvider globalSettings, boolean injectBuildVariables) {
    this.targets = targets;
    this.mavenName = name;
    this.pom = Util.fixEmptyAndTrim(pom);
    this.properties = Util.fixEmptyAndTrim(properties);
    this.jvmOptions = Util.fixEmptyAndTrim(jvmOptions);
    this.usePrivateRepository = usePrivateRepository;
    this.settings = (settings != null) ? settings : GlobalMavenConfig.get().getSettingsProvider();
    this.globalSettings = (globalSettings != null) ? globalSettings : GlobalMavenConfig.get().getGlobalSettingsProvider();
    this.injectBuildVariables = Boolean.valueOf(injectBuildVariables);
  }
  
  public String getTargets() { return this.targets; }
  
  public SettingsProvider getSettings() { return (this.settings != null) ? this.settings : GlobalMavenConfig.get().getSettingsProvider(); }
  
  protected void setSettings(SettingsProvider settings) { this.settings = settings; }
  
  public GlobalSettingsProvider getGlobalSettings() { return (this.globalSettings != null) ? this.globalSettings : GlobalMavenConfig.get().getGlobalSettingsProvider(); }
  
  protected void setGlobalSettings(GlobalSettingsProvider globalSettings) { this.globalSettings = globalSettings; }
  
  public void setUsePrivateRepository(boolean usePrivateRepository) { this.usePrivateRepository = usePrivateRepository; }
  
  public boolean usesPrivateRepository() { return this.usePrivateRepository; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isInjectBuildVariables() { return this.injectBuildVariables.booleanValue(); }
  
  public MavenInstallation getMaven() {
    for (MavenInstallation i : getDescriptor().getInstallations()) {
      if (this.mavenName != null && this.mavenName.equals(i.getName()))
        return i; 
    } 
    return null;
  }
  
  @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"}, justification = "injectBuildVariables in readResolve is needed for data migration.")
  private Object readResolve() {
    if (this.injectBuildVariables == null)
      this.injectBuildVariables = Boolean.valueOf(true); 
    return this;
  }
  
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    VariableResolver<String> vr = build.getBuildVariableResolver();
    EnvVars env = build.getEnvironment(listener);
    String targets = Util.replaceMacro(this.targets, vr);
    targets = env.expand(targets);
    String pom = env.expand(this.pom);
    int startIndex = 0;
    do {
      int endIndex = targets.indexOf('|', startIndex);
      if (-1 == endIndex)
        endIndex = targets.length(); 
      String normalizedTarget = targets.substring(startIndex, endIndex).replaceAll("[\t\r\n]+", " ");
      ArgumentListBuilder args = new ArgumentListBuilder();
      MavenInstallation mi = getMaven();
      if (mi == null) {
        String execName = (String)build.getWorkspace().act(new DecideDefaultMavenCommand(normalizedTarget));
        args.add(execName);
      } else {
        mi = mi.forNode(Computer.currentComputer().getNode(), listener);
        mi = mi.forEnvironment(env);
        String exec = mi.getExecutable(launcher);
        if (exec == null) {
          listener.fatalError(Messages.Maven_NoExecutable(mi.getHome()));
          return false;
        } 
        args.add(exec);
      } 
      if (pom != null)
        args.add(new String[] { "-f", pom }); 
      if (!S_PATTERN.matcher(targets).find()) {
        String settingsPath = SettingsProvider.getSettingsRemotePath(getSettings(), build, listener);
        if (StringUtils.isNotBlank(settingsPath))
          args.add(new String[] { "-s", settingsPath }); 
      } 
      if (!GS_PATTERN.matcher(targets).find()) {
        String settingsPath = GlobalSettingsProvider.getSettingsRemotePath(getGlobalSettings(), build, listener);
        if (StringUtils.isNotBlank(settingsPath))
          args.add(new String[] { "-gs", settingsPath }); 
      } 
      Set<String> sensitiveVars = build.getSensitiveBuildVariables();
      if (isInjectBuildVariables())
        args.addKeyValuePairs("-D", build.getBuildVariables(), sensitiveVars); 
      VariableResolver.Union union = new VariableResolver.Union(new VariableResolver[] { new VariableResolver.ByMap(env), vr });
      args.addKeyValuePairsFromPropertyString("-D", this.properties, union, sensitiveVars);
      if (usesPrivateRepository())
        args.add("-Dmaven.repo.local=" + build.getWorkspace().child(".repository")); 
      args.addTokenized(normalizedTarget);
      wrapUpArguments(args, normalizedTarget, build, launcher, listener);
      buildEnvVars(env, mi);
      if (!launcher.isUnix())
        args = args.toWindowsCommand(); 
      try {
        MavenConsoleAnnotator mca = new MavenConsoleAnnotator(listener.getLogger(), build.getCharset());
        int r = launcher.launch().cmds(args).envs(env).stdout(mca).pwd(build.getModuleRoot()).join();
        if (0 != r)
          return false; 
      } catch (IOException e) {
        Util.displayIOException(e, listener);
        Functions.printStackTrace(e, listener.fatalError(Messages.Maven_ExecFailed()));
        return false;
      } 
      startIndex = endIndex + 1;
    } while (startIndex < targets.length());
    return true;
  }
  
  protected void wrapUpArguments(ArgumentListBuilder args, String normalizedTarget, AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {}
  
  protected void buildEnvVars(EnvVars env, MavenInstallation mi) throws IOException, InterruptedException {
    if (mi != null)
      mi.buildEnvVars(env); 
    env.put("MAVEN_TERMINATE_CMD", "on");
    String jvmOptions = env.expand(this.jvmOptions);
    if (jvmOptions != null)
      env.put("MAVEN_OPTS", jvmOptions.replaceAll("[\t\r\n]+", " ")); 
  }
  
  public DescriptorImpl getDescriptor() { return (DescriptorImpl)super.getDescriptor(); }
}
