package hudson.scheduler;

import antlr.ANTLRException;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

public final class CronTabList {
  private final Vector<CronTab> tabs;
  
  public CronTabList(Collection<CronTab> tabs) { this.tabs = new Vector(tabs); }
  
  public boolean check(Calendar cal) {
    for (CronTab tab : this.tabs) {
      if (tab.check(cal))
        return true; 
    } 
    return false;
  }
  
  public String checkSanity() {
    for (CronTab tab : this.tabs) {
      String s = tab.checkSanity();
      if (s != null)
        return s; 
    } 
    return null;
  }
  
  @CheckForNull
  public static String getValidTimezone(String timezone) {
    String[] validIDs = TimeZone.getAvailableIDs();
    for (String str : validIDs) {
      if (str != null && str.equals(timezone))
        return timezone; 
    } 
    return null;
  }
  
  public static CronTabList create(@NonNull String format) { return create(format, null); }
  
  public static CronTabList create(@NonNull String format, Hash hash) {
    Vector<CronTab> r = new Vector<CronTab>();
    int lineNumber = 0;
    String timezone = null;
    for (String line : format.split("\\r?\\n")) {
      lineNumber++;
      line = line.trim();
      if (lineNumber == 1 && line.startsWith("TZ=")) {
        String timezoneString = line.replace("TZ=", "");
        timezone = getValidTimezone(timezoneString);
        if (timezone != null) {
          LOGGER.log(Level.CONFIG, "CRON with timezone {0}", timezone);
        } else {
          throw new ANTLRException("Invalid or unsupported timezone '" + timezoneString + "'");
        } 
      } else if (!line.isEmpty() && !line.startsWith("#")) {
        try {
          r.add(new CronTab(line, lineNumber, hash, timezone));
        } catch (IllegalArgumentException e) {
          throw new ANTLRException(Messages.CronTabList_InvalidInput(line, e.getMessage()), e);
        } 
      } 
    } 
    return new CronTabList(r);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @CheckForNull
  public Calendar previous() {
    Calendar nearest = null;
    for (CronTab tab : this.tabs) {
      Calendar scheduled = tab.floor((tab.getTimeZone() == null) ? Calendar.getInstance() : Calendar.getInstance(tab.getTimeZone()));
      if (nearest == null || nearest.before(scheduled))
        nearest = scheduled; 
    } 
    return nearest;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @CheckForNull
  public Calendar next() {
    Calendar nearest = null;
    for (CronTab tab : this.tabs) {
      Calendar scheduled = tab.ceil((tab.getTimeZone() == null) ? Calendar.getInstance() : Calendar.getInstance(tab.getTimeZone()));
      if (nearest == null || nearest.after(scheduled))
        nearest = scheduled; 
    } 
    return nearest;
  }
  
  private static final Logger LOGGER = Logger.getLogger(CronTabList.class.getName());
}
