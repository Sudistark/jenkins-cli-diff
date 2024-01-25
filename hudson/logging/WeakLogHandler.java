package hudson.logging;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class WeakLogHandler extends Handler {
  private final WeakReference<Handler> target;
  
  private final Logger logger;
  
  public WeakLogHandler(Handler target, Logger logger) {
    this.logger = logger;
    logger.addHandler(this);
    this.target = new WeakReference(target);
  }
  
  public void publish(LogRecord record) {
    Handler t = resolve();
    if (t != null)
      t.publish(record); 
  }
  
  public void flush() {
    Handler t = resolve();
    if (t != null)
      t.flush(); 
  }
  
  public void close() {
    Handler t = resolve();
    if (t != null)
      t.close(); 
  }
  
  private Handler resolve() {
    Handler r = (Handler)this.target.get();
    if (r == null)
      this.logger.removeHandler(this); 
    return r;
  }
  
  public void setFormatter(Formatter newFormatter) throws SecurityException {
    super.setFormatter(newFormatter);
    Handler t = resolve();
    if (t != null)
      t.setFormatter(newFormatter); 
  }
  
  public void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
    super.setEncoding(encoding);
    Handler t = resolve();
    if (t != null)
      t.setEncoding(encoding); 
  }
  
  public void setFilter(Filter newFilter) throws SecurityException {
    super.setFilter(newFilter);
    Handler t = resolve();
    if (t != null)
      t.setFilter(newFilter); 
  }
  
  public void setErrorManager(ErrorManager em) {
    super.setErrorManager(em);
    Handler t = resolve();
    if (t != null)
      t.setErrorManager(em); 
  }
  
  public void setLevel(Level newLevel) throws SecurityException {
    super.setLevel(newLevel);
    Handler t = resolve();
    if (t != null)
      t.setLevel(newLevel); 
  }
  
  public boolean isLoggable(LogRecord record) {
    Handler t = resolve();
    if (t != null)
      return t.isLoggable(record); 
    return super.isLoggable(record);
  }
}
