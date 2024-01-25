package jenkins.mvn;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String DefaultGlobalSettingsProvider_DisplayName() { return holder.format("DefaultGlobalSettingsProvider.DisplayName", new Object[0]); }
  
  public static Localizable _DefaultGlobalSettingsProvider_DisplayName() { return new Localizable(holder, "DefaultGlobalSettingsProvider.DisplayName", new Object[0]); }
  
  public static String FilePathSettingsProvider_DisplayName() { return holder.format("FilePathSettingsProvider.DisplayName", new Object[0]); }
  
  public static Localizable _FilePathSettingsProvider_DisplayName() { return new Localizable(holder, "FilePathSettingsProvider.DisplayName", new Object[0]); }
  
  public static String DefaultSettingsProvider_DisplayName() { return holder.format("DefaultSettingsProvider.DisplayName", new Object[0]); }
  
  public static Localizable _DefaultSettingsProvider_DisplayName() { return new Localizable(holder, "DefaultSettingsProvider.DisplayName", new Object[0]); }
  
  public static String FilePathGlobalSettingsProvider_DisplayName() { return holder.format("FilePathGlobalSettingsProvider.DisplayName", new Object[0]); }
  
  public static Localizable _FilePathGlobalSettingsProvider_DisplayName() { return new Localizable(holder, "FilePathGlobalSettingsProvider.DisplayName", new Object[0]); }
}
