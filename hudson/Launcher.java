package hudson;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.tasks.filters.EnvVarsFilterLocalRule;
import jenkins.tasks.filters.EnvVarsFilterRuleWrapper;
import jenkins.tasks.filters.EnvVarsFilterableBuilder;
import org.kohsuke.accmod.Restricted;

public abstract class Launcher {
  @NonNull
  protected final TaskListener listener;
  
  @CheckForNull
  protected final VirtualChannel channel;
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  protected EnvVarsFilterRuleWrapper envVarsFilterRuleWrapper;
  
  protected Launcher(@NonNull TaskListener listener, @CheckForNull VirtualChannel channel) {
    this.listener = listener;
    this.channel = channel;
  }
  
  protected Launcher(@NonNull Launcher launcher) { this(launcher.listener, launcher.channel); }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  public void prepareFilterRules(@CheckForNull Run<?, ?> run, @NonNull EnvVarsFilterableBuilder builder) {
    List<EnvVarsFilterLocalRule> specificRuleList = builder.buildEnvVarsFilterRules();
    EnvVarsFilterRuleWrapper ruleWrapper = EnvVarsFilterRuleWrapper.createRuleWrapper(run, builder, this, specificRuleList);
    setEnvVarsFilterRuleWrapper(ruleWrapper);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  protected void setEnvVarsFilterRuleWrapper(EnvVarsFilterRuleWrapper envVarsFilterRuleWrapper) { this.envVarsFilterRuleWrapper = envVarsFilterRuleWrapper; }
  
  @CheckForNull
  public VirtualChannel getChannel() { return this.channel; }
  
  @NonNull
  public TaskListener getListener() { return this.listener; }
  
  @Deprecated
  @CheckForNull
  public Computer getComputer() {
    for (Computer c : Jenkins.get().getComputers()) {
      if (c.getChannel() == this.channel)
        return c; 
    } 
    return null;
  }
  
  @NonNull
  public final ProcStarter launch() { return new ProcStarter(this); }
  
  @Deprecated
  public final Proc launch(String cmd, Map<String, String> env, OutputStream out, FilePath workDir) throws IOException { return launch(cmd, Util.mapToEnv(env), out, workDir); }
  
  @Deprecated
  public final Proc launch(String[] cmd, Map<String, String> env, OutputStream out, FilePath workDir) throws IOException { return launch(cmd, Util.mapToEnv(env), out, workDir); }
  
  @Deprecated
  public final Proc launch(String[] cmd, Map<String, String> env, InputStream in, OutputStream out) throws IOException { return launch(cmd, Util.mapToEnv(env), in, out); }
  
  @Deprecated
  public final Proc launch(String[] cmd, boolean[] mask, Map<String, String> env, OutputStream out, FilePath workDir) throws IOException { return launch(cmd, mask, Util.mapToEnv(env), out, workDir); }
  
  @Deprecated
  public final Proc launch(String[] cmd, boolean[] mask, Map<String, String> env, InputStream in, OutputStream out) throws IOException { return launch(cmd, mask, Util.mapToEnv(env), in, out); }
  
  @Deprecated
  public final Proc launch(String cmd, String[] env, OutputStream out, FilePath workDir) throws IOException { return launch(Util.tokenize(cmd), env, out, workDir); }
  
  @Deprecated
  public final Proc launch(String[] cmd, String[] env, OutputStream out, FilePath workDir) throws IOException { return launch(cmd, env, null, out, workDir); }
  
  @Deprecated
  public final Proc launch(String[] cmd, String[] env, InputStream in, OutputStream out) throws IOException { return launch(cmd, env, in, out, null); }
  
  @Deprecated
  public final Proc launch(String[] cmd, boolean[] mask, String[] env, OutputStream out, FilePath workDir) throws IOException { return launch(cmd, mask, env, null, out, workDir); }
  
  @Deprecated
  public final Proc launch(String[] cmd, boolean[] mask, String[] env, InputStream in, OutputStream out) throws IOException { return launch(cmd, mask, env, in, out, null); }
  
  @Deprecated
  public Proc launch(String[] cmd, String[] env, InputStream in, OutputStream out, FilePath workDir) throws IOException { return launch(launch().cmds(cmd).envs(env).stdin(in).stdout(out).pwd(workDir)); }
  
  @Deprecated
  public Proc launch(String[] cmd, boolean[] mask, String[] env, InputStream in, OutputStream out, FilePath workDir) throws IOException { return launch(launch().cmds(cmd).masks(mask).envs(env).stdin(in).stdout(out).pwd(workDir)); }
  
  public abstract Proc launch(@NonNull ProcStarter paramProcStarter) throws IOException;
  
  public abstract Channel launchChannel(@NonNull String[] paramArrayOfString, @NonNull OutputStream paramOutputStream, @CheckForNull FilePath paramFilePath, @NonNull Map<String, String> paramMap) throws IOException, InterruptedException;
  
  public boolean isUnix() { return (File.pathSeparatorChar == ':'); }
  
  public abstract void kill(Map<String, String> paramMap) throws IOException, InterruptedException;
  
  protected final void printCommandLine(@NonNull String[] cmd, @CheckForNull FilePath workDir) {
    StringBuilder buf = new StringBuilder();
    if (workDir != null) {
      buf.append('[');
      if (showFullPath) {
        buf.append(workDir.getRemote());
      } else {
        buf.append(workDir.getRemote().replaceFirst("^.+[/\\\\]", ""));
      } 
      buf.append("] ");
    } 
    buf.append('$');
    for (String c : cmd) {
      buf.append(' ');
      if (c.indexOf(' ') >= 0) {
        if (c.indexOf('"') >= 0) {
          buf.append('\'').append(c).append('\'');
        } else {
          buf.append('"').append(c).append('"');
        } 
      } else {
        buf.append(c);
      } 
    } 
    this.listener.getLogger().println(buf);
    this.listener.getLogger().flush();
  }
  
  protected final void maskedPrintCommandLine(@NonNull List<String> cmd, @CheckForNull boolean[] mask, @CheckForNull FilePath workDir) {
    if (mask == null) {
      printCommandLine((String[])cmd.toArray(new String[0]), workDir);
      return;
    } 
    assert mask.length == cmd.size();
    String[] masked = new String[cmd.size()];
    for (int i = 0; i < cmd.size(); i++) {
      if (mask[i]) {
        masked[i] = "********";
      } else {
        masked[i] = (String)cmd.get(i);
      } 
    } 
    printCommandLine(masked, workDir);
  }
  
  protected final void maskedPrintCommandLine(@NonNull String[] cmd, @NonNull boolean[] mask, @CheckForNull FilePath workDir) { maskedPrintCommandLine(Arrays.asList(cmd), mask, workDir); }
  
  @NonNull
  public final Launcher decorateFor(@NonNull Node node) {
    Launcher l = this;
    for (LauncherDecorator d : LauncherDecorator.all())
      l = d.decorate(l, node); 
    return l;
  }
  
  @NonNull
  public final Launcher decorateByPrefix(String... prefix) {
    Launcher outer = this;
    return new Object(this, outer, outer, prefix);
  }
  
  @NonNull
  public final Launcher decorateByEnv(@NonNull EnvVars _env) {
    EnvVars env = new EnvVars(_env);
    Launcher outer = this;
    return new Object(this, outer, outer, env);
  }
  
  private static EnvVars inherit(@CheckForNull String[] env) {
    EnvVars m = new EnvVars();
    if (env != null)
      for (String e : env) {
        int index = e.indexOf('=');
        m.put(e.substring(0, index), e.substring(index + 1));
      }  
    return inherit(m);
  }
  
  private static EnvVars inherit(@NonNull Map<String, String> overrides) {
    EnvVars m = new EnvVars(EnvVars.masterEnvVars);
    m.overrideExpandingAll(overrides);
    return m;
  }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for debugging")
  public static boolean showFullPath = false;
  
  private static final InputStream NULL_INPUT_STREAM = InputStream.nullInputStream();
  
  private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());
}
