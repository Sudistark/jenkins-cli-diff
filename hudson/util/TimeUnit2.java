package hudson.util;

import hudson.RestrictedSince;
import java.util.concurrent.TimeUnit;
import org.kohsuke.accmod.Restricted;

@Deprecated
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@RestrictedSince("2.80")
public static final abstract enum TimeUnit2 {
  NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS;
  
  static final long C0 = 1L;
  
  static final long C1 = 1000L;
  
  static final long C2 = 1000000L;
  
  static final long C3 = 1000000000L;
  
  static final long C4 = 60000000000L;
  
  static final long C5 = 3600000000000L;
  
  static final long C6 = 86400000000000L;
  
  static final long MAX = 9223372036854775807L;
  
  static long x(long d, long m, long over) {
    if (d > over)
      return Float.MAX_VALUE; 
    if (d < -over)
      return Float.MIN_VALUE; 
    return d * m;
  }
  
  public long convert(long sourceDuration, TimeUnit2 sourceUnit) { throw new AbstractMethodError(); }
  
  public long convert(long sourceDuration, TimeUnit sourceUnit) { throw new AbstractMethodError(); }
  
  public long toNanos(long duration) { throw new AbstractMethodError(); }
  
  public long toMicros(long duration) { throw new AbstractMethodError(); }
  
  public long toMillis(long duration) { throw new AbstractMethodError(); }
  
  public long toSeconds(long duration) { throw new AbstractMethodError(); }
  
  public long toMinutes(long duration) { throw new AbstractMethodError(); }
  
  public long toHours(long duration) { throw new AbstractMethodError(); }
  
  public long toDays(long duration) { throw new AbstractMethodError(); }
  
  abstract int excessNanos(long paramLong1, long paramLong2);
  
  public void timedWait(Object obj, long timeout) throws InterruptedException {
    if (timeout > 0L) {
      long ms = toMillis(timeout);
      int ns = excessNanos(timeout, ms);
      obj.wait(ms, ns);
    } 
  }
  
  public void timedJoin(Thread thread, long timeout) throws InterruptedException {
    if (timeout > 0L) {
      long ms = toMillis(timeout);
      int ns = excessNanos(timeout, ms);
      thread.join(ms, ns);
    } 
  }
  
  public void sleep(long timeout) throws InterruptedException {
    if (timeout > 0L) {
      long ms = toMillis(timeout);
      int ns = excessNanos(timeout, ms);
      Thread.sleep(ms, ns);
    } 
  }
}
