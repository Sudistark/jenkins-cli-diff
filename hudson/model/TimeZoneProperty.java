package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class TimeZoneProperty extends UserProperty {
  @CheckForNull
  private String timeZoneName;
  
  private static final Logger LOGGER = Logger.getLogger(TimeZoneProperty.class.getName());
  
  @DataBoundConstructor
  public TimeZoneProperty(@CheckForNull String timeZoneName) { this.timeZoneName = timeZoneName; }
  
  private TimeZoneProperty() { this(null); }
  
  public void setTimeZoneName(@CheckForNull String timeZoneName) { this.timeZoneName = timeZoneName; }
  
  @CheckForNull
  public String getTimeZoneName() { return this.timeZoneName; }
  
  @CheckForNull
  public static String forCurrentUser() {
    current = User.current();
    if (current == null)
      return null; 
    return forUser(current);
  }
  
  @CheckForNull
  private static String forUser(User user) {
    TimeZoneProperty tzp = (TimeZoneProperty)user.getProperty(TimeZoneProperty.class);
    if (tzp.timeZoneName == null || tzp.timeZoneName.isEmpty())
      return null; 
    TimeZone tz = TimeZone.getTimeZone(tzp.timeZoneName);
    if (!tz.getID().equals(tzp.timeZoneName)) {
      LOGGER.log(Level.WARNING, "Invalid user time zone {0} for {1}", new Object[] { tzp.timeZoneName, user.getId() });
      return null;
    } 
    return tz.getID();
  }
}
