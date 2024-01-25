package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Retrier<V> extends Object {
  private static final Logger LOGGER = Logger.getLogger(Retrier.class.getName());
  
  private int attempts;
  
  private long delay;
  
  private Callable<V> callable;
  
  private BiPredicate<Integer, V> checkResult;
  
  private String action;
  
  private BiFunction<Integer, Exception, V> duringActionExceptionListener;
  
  private Class<?>[] duringActionExceptions;
  
  private Retrier(Builder<V> builder) {
    this.attempts = builder.attempts;
    this.delay = builder.delay;
    this.callable = builder.callable;
    this.checkResult = builder.checkResult;
    this.action = builder.action;
    this.duringActionExceptionListener = builder.duringActionExceptionListener;
    this.duringActionExceptions = builder.duringActionExceptions;
  }
  
  @CheckForNull
  public V start() throws Exception {
    V result = null;
    int currentAttempt = 0;
    boolean success = false;
    while (currentAttempt < this.attempts && !success) {
      currentAttempt++;
      try {
        if (LOGGER.isLoggable(Level.INFO))
          LOGGER.log(Level.INFO, Messages.Retrier_Attempt(Integer.valueOf(currentAttempt), this.action)); 
        result = (V)this.callable.call();
      } catch (Exception e) {
        if (this.duringActionExceptions == null || Stream.of(this.duringActionExceptions).noneMatch(exception -> exception.isAssignableFrom(e.getClass()))) {
          LOGGER.log(Level.WARNING, Messages.Retrier_ExceptionThrown(Integer.valueOf(currentAttempt), this.action), e);
          throw e;
        } 
        LOGGER.log(Level.INFO, Messages.Retrier_ExceptionFailed(Integer.valueOf(currentAttempt), this.action), e);
        if (this.duringActionExceptionListener != null) {
          LOGGER.log(Level.INFO, Messages.Retrier_CallingListener(e.getLocalizedMessage(), Integer.valueOf(currentAttempt), this.action));
          result = (V)this.duringActionExceptionListener.apply(Integer.valueOf(currentAttempt), e);
        } 
      } 
      success = this.checkResult.test(Integer.valueOf(currentAttempt), result);
      if (!success) {
        if (currentAttempt < this.attempts) {
          LOGGER.log(Level.WARNING, Messages.Retrier_AttemptFailed(Integer.valueOf(currentAttempt), this.action));
          LOGGER.log(Level.FINE, Messages.Retrier_Sleeping(Long.valueOf(this.delay), this.action));
          try {
            Thread.sleep(this.delay);
            continue;
          } catch (InterruptedException ie) {
            LOGGER.log(Level.FINE, Messages.Retrier_Interruption(this.action));
            Thread.currentThread().interrupt();
            currentAttempt = this.attempts;
            continue;
          } 
        } 
        LOGGER.log(Level.INFO, Messages.Retrier_NoSuccess(this.action, Integer.valueOf(this.attempts)));
        continue;
      } 
      LOGGER.log(Level.INFO, Messages.Retrier_Success(this.action, Integer.valueOf(currentAttempt)));
    } 
    return result;
  }
}
