package hudson.cli;

import groovy.lang.Binding;
import hudson.Extension;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;
import jline.TerminalFactory;
import org.codehaus.groovy.tools.shell.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.kohsuke.args4j.Argument;

@Extension
public class GroovyshCommand extends CLICommand {
  public String getShortDescription() { return Messages.GroovyshCommand_ShortDescription(); }
  
  @Argument(metaVar = "ARGS")
  public List<String> args = new ArrayList();
  
  protected int run() {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    System.setProperty("jline.terminal", jline.UnsupportedTerminal.class.getName());
    TerminalFactory.reset();
    StringBuilder commandLine = new StringBuilder();
    for (String arg : this.args) {
      if (commandLine.length() > 0)
        commandLine.append(" "); 
      commandLine.append(arg);
    } 
    Groovysh shell = createShell(this.stdin, this.stdout, this.stderr);
    return shell.run(commandLine.toString());
  }
  
  protected Groovysh createShell(InputStream stdin, PrintStream stdout, PrintStream stderr) {
    Charset charset;
    Binding binding = new Binding();
    try {
      charset = getClientCharset();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } 
    binding.setProperty("out", new PrintWriter(new OutputStreamWriter(stdout, charset), true));
    binding.setProperty("hudson", Jenkins.get());
    binding.setProperty("jenkins", Jenkins.get());
    IO io = new IO(new BufferedInputStream(stdin), stdout, stderr);
    ClassLoader cl = (Jenkins.get()).pluginManager.uberClassLoader;
    Object object = new Object(this, null, null, cl);
    Groovysh shell = new Groovysh(cl, binding, io, object);
    shell.getImports().add("hudson.model.*");
    return shell;
  }
}
