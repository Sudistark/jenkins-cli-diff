package jenkins.monitor;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String JavaLevelAdminMonitor_DisplayName() { return holder.format("JavaLevelAdminMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _JavaLevelAdminMonitor_DisplayName() { return new Localizable(holder, "JavaLevelAdminMonitor.DisplayName", new Object[0]); }
}
