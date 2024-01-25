package jenkins.util;

import hudson.console.ConsoleNote;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public final class BuildListenerAdapter implements BuildListener {
  private final TaskListener delegate;
  
  public BuildListenerAdapter(TaskListener delegate) { this.delegate = delegate; }
  
  public PrintStream getLogger() { return this.delegate.getLogger(); }
  
  public void annotate(ConsoleNote ann) throws IOException { this.delegate.annotate(ann); }
  
  public void hyperlink(String url, String text) throws IOException { this.delegate.hyperlink(url, text); }
  
  public PrintWriter error(String msg) { return this.delegate.error(msg); }
  
  public PrintWriter error(String format, Object... args) { return this.delegate.error(format, args); }
  
  public PrintWriter fatalError(String msg) { return this.delegate.fatalError(msg); }
  
  public PrintWriter fatalError(String format, Object... args) { return this.delegate.fatalError(format, args); }
  
  public static BuildListener wrap(TaskListener l) {
    if (l instanceof BuildListener)
      return (BuildListener)l; 
    return new BuildListenerAdapter(l);
  }
}
