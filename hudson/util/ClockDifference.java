package hudson.util;

import hudson.Util;
import hudson.model.Node;
import java.io.IOException;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public final class ClockDifference {
  @Exported
  public final long diff;
  
  public ClockDifference(long value) { this.diff = value; }
  
  public boolean isDangerous() { return (Math.abs(this.diff) > 5000L); }
  
  public long abs() { return Math.abs(this.diff); }
  
  public String toString() {
    if (-1000L < this.diff && this.diff < 1000L)
      return Messages.ClockDifference_InSync(); 
    long abs = Math.abs(this.diff);
    String s = Util.getTimeSpanString(abs);
    if (this.diff < 0L) {
      s = Messages.ClockDifference_Ahead(s);
    } else {
      s = Messages.ClockDifference_Behind(s);
    } 
    return s;
  }
  
  public String toHtml() {
    String s = toString();
    if (isDangerous())
      s = Util.wrapToErrorSpan(s); 
    return s;
  }
  
  public static String toHtml(Node d) {
    try {
      if (d == null)
        return FAILED_HTML; 
      return d.getClockDifference().toHtml();
    } catch (IOException|InterruptedException e) {
      return FAILED_HTML;
    } 
  }
  
  public static String toHtml(ClockDifference d) {
    if (d == null)
      return FAILED_HTML; 
    return d.toHtml();
  }
  
  public static final ClockDifference ZERO = new ClockDifference(0L);
  
  private static final String FAILED_HTML = "<span class='error'>" + Messages.ClockDifference_Failed() + "</span>";
}
