package jenkins.appearance;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String AppearanceCategory_Description() { return holder.format("AppearanceCategory.Description", new Object[0]); }
  
  public static Localizable _AppearanceCategory_Description() { return new Localizable(holder, "AppearanceCategory.Description", new Object[0]); }
  
  public static String AppearanceGlobalConfiguration_Description() { return holder.format("AppearanceGlobalConfiguration.Description", new Object[0]); }
  
  public static Localizable _AppearanceGlobalConfiguration_Description() { return new Localizable(holder, "AppearanceGlobalConfiguration.Description", new Object[0]); }
  
  public static String AppearanceCategory_DisplayName() { return holder.format("AppearanceCategory.DisplayName", new Object[0]); }
  
  public static Localizable _AppearanceCategory_DisplayName() { return new Localizable(holder, "AppearanceCategory.DisplayName", new Object[0]); }
  
  public static String AppearanceGlobalConfiguration_DisplayName() { return holder.format("AppearanceGlobalConfiguration.DisplayName", new Object[0]); }
  
  public static Localizable _AppearanceGlobalConfiguration_DisplayName() { return new Localizable(holder, "AppearanceGlobalConfiguration.DisplayName", new Object[0]); }
}
