package hudson.search;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String UserSearchProperty_DisplayName() { return holder.format("UserSearchProperty.DisplayName", new Object[0]); }
  
  public static Localizable _UserSearchProperty_DisplayName() { return new Localizable(holder, "UserSearchProperty.DisplayName", new Object[0]); }
}
