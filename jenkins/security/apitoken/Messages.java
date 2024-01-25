package jenkins.security.apitoken;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String ApiTokenPropertyEnabledNewLegacyAdministrativeMonitor_displayName() { return holder.format("ApiTokenPropertyEnabledNewLegacyAdministrativeMonitor.displayName", new Object[0]); }
  
  public static Localizable _ApiTokenPropertyEnabledNewLegacyAdministrativeMonitor_displayName() { return new Localizable(holder, "ApiTokenPropertyEnabledNewLegacyAdministrativeMonitor.displayName", new Object[0]); }
  
  public static String LegacyApiTokenAdministrativeMonitor_displayName() { return holder.format("LegacyApiTokenAdministrativeMonitor.displayName", new Object[0]); }
  
  public static Localizable _LegacyApiTokenAdministrativeMonitor_displayName() { return new Localizable(holder, "LegacyApiTokenAdministrativeMonitor.displayName", new Object[0]); }
  
  public static String ApiTokenPropertyDisabledDefaultAdministrativeMonitor_displayName() { return holder.format("ApiTokenPropertyDisabledDefaultAdministrativeMonitor.displayName", new Object[0]); }
  
  public static Localizable _ApiTokenPropertyDisabledDefaultAdministrativeMonitor_displayName() { return new Localizable(holder, "ApiTokenPropertyDisabledDefaultAdministrativeMonitor.displayName", new Object[0]); }
}
