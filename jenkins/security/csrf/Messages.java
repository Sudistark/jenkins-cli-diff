package jenkins.security.csrf;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String CSRFAdministrativeMonitor_displayName() { return holder.format("CSRFAdministrativeMonitor.displayName", new Object[0]); }
  
  public static Localizable _CSRFAdministrativeMonitor_displayName() { return new Localizable(holder, "CSRFAdministrativeMonitor.displayName", new Object[0]); }
}
