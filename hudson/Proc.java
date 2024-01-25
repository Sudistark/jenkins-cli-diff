package hudson;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.TaskListener;
import hudson.util.ClassLoaderSanityThreadFactory;
import hudson.util.DaemonThreadFactory;
import hudson.util.ExceptionCatchingThreadFactory;
import hudson.util.NamingThreadFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class Proc {
  private static final ExecutorService executor = Executors.newCachedThreadPool(new ExceptionCatchingThreadFactory(new NamingThreadFactory(new ClassLoaderSanityThreadFactory(new DaemonThreadFactory()), "Proc.executor")));
  
  public abstract boolean isAlive() throws IOException, InterruptedException;
  
  public abstract void kill();
  
  public abstract int join() throws IOException, InterruptedException;
  
  @CheckForNull
  public abstract InputStream getStdout();
  
  @CheckForNull
  public abstract InputStream getStderr();
  
  @CheckForNull
  public abstract OutputStream getStdin();
  
  public final int joinWithTimeout(long timeout, TimeUnit unit, TaskListener listener) throws IOException, InterruptedException {
    latch = new CountDownLatch(1);
    try {
      executor.submit(new Object(this, latch, timeout, unit, listener));
      return join();
    } finally {
      latch.countDown();
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(Proc.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for debugging")
  public static boolean SHOW_PID = true;
}
