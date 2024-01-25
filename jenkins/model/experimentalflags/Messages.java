package jenkins.model.experimentalflags;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String UserExperimentalFlagsProperty_DisplayName() { return holder.format("UserExperimentalFlagsProperty.DisplayName", new Object[0]); }
  
  public static Localizable _UserExperimentalFlagsProperty_DisplayName() { return new Localizable(holder, "UserExperimentalFlagsProperty.DisplayName", new Object[0]); }
}
