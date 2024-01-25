package hudson.console;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Computer;
import hudson.model.Run;
import java.io.IOException;
import java.io.OutputStream;

public abstract class ConsoleLogFilter implements ExtensionPoint {
  @Deprecated
  public OutputStream decorateLogger(AbstractBuild build, OutputStream logger) throws IOException, InterruptedException {
    if (Util.isOverridden(ConsoleLogFilter.class, getClass(), "decorateLogger", new Class[] { Run.class, OutputStream.class }))
      return decorateLogger(build, logger); 
    throw new AbstractMethodError("Plugin class '" + getClass().getName() + "' does not override either overload of the decorateLogger method.");
  }
  
  public OutputStream decorateLogger(Run build, OutputStream logger) throws IOException, InterruptedException {
    if (build instanceof AbstractBuild)
      return decorateLogger((AbstractBuild)build, logger); 
    return logger;
  }
  
  public OutputStream decorateLogger(@NonNull Computer computer, OutputStream logger) throws IOException, InterruptedException { return logger; }
  
  public static ExtensionList<ConsoleLogFilter> all() { return ExtensionList.lookup(ConsoleLogFilter.class); }
}
