package hudson.cli;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;

@Extension
public class GroovyCommand extends CLICommand {
  @Argument(metaVar = "SCRIPT", usage = "Script to be executed. Only '=' (to represent stdin) is supported.")
  public String script;
  
  public String getShortDescription() { return Messages.GroovyCommand_ShortDescription(); }
  
  @Argument(metaVar = "ARGUMENTS", index = 1, usage = "Command line arguments to pass into script.")
  public List<String> remaining = new ArrayList();
  
  protected int run() throws Exception {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    Binding binding = new Binding();
    binding.setProperty("out", new PrintWriter(new OutputStreamWriter(this.stdout, getClientCharset()), true));
    binding.setProperty("stdin", this.stdin);
    binding.setProperty("stdout", this.stdout);
    binding.setProperty("stderr", this.stderr);
    GroovyShell groovy = new GroovyShell((Jenkins.get().getPluginManager()).uberClassLoader, binding);
    groovy.run(loadScript(), "RemoteClass", (String[])this.remaining.toArray(new String[0]));
    return 0;
  }
  
  private String loadScript() {
    if (this.script == null)
      throw new CmdLineException(null, "No script is specified"); 
    if (this.script.equals("="))
      return IOUtils.toString(this.stdin); 
    checkChannel();
    return null;
  }
}
