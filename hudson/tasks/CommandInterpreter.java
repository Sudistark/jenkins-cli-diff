package hudson.tasks;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Proc;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.tasks.filters.EnvVarsFilterException;
import jenkins.tasks.filters.EnvVarsFilterLocalRule;
import jenkins.tasks.filters.EnvVarsFilterableBuilder;
import org.kohsuke.accmod.Restricted;

public abstract class CommandInterpreter extends Builder implements EnvVarsFilterableBuilder {
  protected final String command;
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  protected List<EnvVarsFilterLocalRule> configuredLocalRules;
  
  protected CommandInterpreter(String command) {
    this.configuredLocalRules = new ArrayList();
    this.command = command;
  }
  
  public final String getCommand() { return this.command; }
  
  @NonNull
  public List<EnvVarsFilterLocalRule> buildEnvVarsFilterRules() { return (this.configuredLocalRules == null) ? Collections.emptyList() : new ArrayList(this.configuredLocalRules); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public List<EnvVarsFilterLocalRule> getConfiguredLocalRules() { return (this.configuredLocalRules == null) ? Collections.emptyList() : this.configuredLocalRules; }
  
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException { return perform(build, launcher, listener); }
  
  protected boolean isErrorlevelForUnstableBuild(int exitCode) { return false; }
  
  public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener) throws InterruptedException {
    FilePath ws = build.getWorkspace();
    if (ws == null) {
      Node node = build.getBuiltOn();
      if (node == null)
        throw new NullPointerException("no such build node: " + build.getBuiltOnStr()); 
      throw new NullPointerException("no workspace from node " + node + " which is computer " + node.toComputer() + " and has channel " + node.getChannel());
    } 
    script = null;
    r = -1;
    try {
      try {
        script = createScriptFile(ws);
      } catch (IOException e) {
        Util.displayIOException(e, listener);
        Functions.printStackTrace(e, listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
        return false;
      } 
      try {
        EnvVars envVars = build.getEnvironment(listener);
        for (Map.Entry<String, String> e : build.getBuildVariables().entrySet())
          envVars.put((String)e.getKey(), (String)e.getValue()); 
        launcher.prepareFilterRules(build, this);
        Launcher.ProcStarter procStarter = launcher.launch();
        procStarter.cmds(buildCommandLine(script))
          .envs(envVars)
          .stdout(listener)
          .pwd(ws);
        try {
          Proc proc = procStarter.start();
          r = join(proc);
        } catch (EnvVarsFilterException se) {
          LOGGER.log(Level.FINE, "Environment variable filtering failed", se);
          return false;
        } 
        if (isErrorlevelForUnstableBuild(r)) {
          build.setResult(Result.UNSTABLE);
          r = 0;
        } 
      } catch (IOException e) {
        Util.displayIOException(e, listener);
        Functions.printStackTrace(e, listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
      } 
      return (r == 0);
    } finally {
      try {
        if (script != null)
          script.delete(); 
      } catch (IOException e) {
        if (r == -1 && e.getCause() instanceof hudson.remoting.ChannelClosedException) {
          LOGGER.log(Level.FINE, "Script deletion failed", e);
        } else {
          Util.displayIOException(e, listener);
          Functions.printStackTrace(e, listener.fatalError(Messages.CommandInterpreter_UnableToDelete(script)));
        } 
      } catch (Exception e) {
        Functions.printStackTrace(e, listener.fatalError(Messages.CommandInterpreter_UnableToDelete(script)));
      } 
    } 
  }
  
  protected int join(Proc p) throws IOException, InterruptedException { return p.join(); }
  
  public FilePath createScriptFile(@NonNull FilePath dir) throws IOException, InterruptedException { return dir.createTextTempFile("jenkins", getFileExtension(), getContents(), false); }
  
  private static final Logger LOGGER = Logger.getLogger(CommandInterpreter.class.getName());
  
  public abstract String[] buildCommandLine(FilePath paramFilePath);
  
  protected abstract String getContents();
  
  protected abstract String getFileExtension();
}
