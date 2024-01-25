package hudson.util;

import hudson.console.PlainTextConsoleOutputStream;
import hudson.model.TaskListener;
import java.io.Closeable;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogTaskListener extends AbstractTaskListener implements TaskListener, Closeable {
  private final TaskListener delegate;
  
  private static final long serialVersionUID = 1L;
  
  public LogTaskListener(Logger logger, Level level) { this.delegate = new StreamTaskListener(new PlainTextConsoleOutputStream(new LogOutputStream(logger, level, (new Throwable()).getStackTrace()[1]))); }
  
  public PrintStream getLogger() { return this.delegate.getLogger(); }
  
  public void close() { this.delegate.getLogger().close(); }
}
