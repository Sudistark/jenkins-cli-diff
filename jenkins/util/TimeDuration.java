package jenkins.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.concurrent.TimeUnit;

public class TimeDuration {
  private final long millis;
  
  public TimeDuration(long millis) { this.millis = millis; }
  
  @Deprecated
  public int getTime() { return (int)this.millis; }
  
  public long getTimeInMillis() { return this.millis; }
  
  public int getTimeInSeconds() { return (int)(this.millis / 1000L); }
  
  public long as(TimeUnit t) { return t.convert(this.millis, TimeUnit.MILLISECONDS); }
  
  @CheckForNull
  public static TimeDuration fromString(@CheckForNull String delay) {
    if (delay == null)
      return null; 
    long unitMultiplier = 1L;
    delay = delay.trim();
    try {
      if (delay.endsWith("sec") || delay.endsWith("secs")) {
        delay = delay.substring(0, delay.lastIndexOf("sec"));
        delay = delay.trim();
        unitMultiplier = 1000L;
      } 
      return new TimeDuration(Long.parseLong(delay.trim()) * unitMultiplier);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid time duration value: " + delay, e);
    } 
  }
}
