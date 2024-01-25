package jenkins.security.seed;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String UserSeedProperty_DisplayName() { return holder.format("UserSeedProperty.DisplayName", new Object[0]); }
  
  public static Localizable _UserSeedProperty_DisplayName() { return new Localizable(holder, "UserSeedProperty.DisplayName", new Object[0]); }
}
