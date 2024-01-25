package jenkins.install;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String SetupWizard_DisplayName() { return holder.format("SetupWizard.DisplayName", new Object[0]); }
  
  public static Localizable _SetupWizard_DisplayName() { return new Localizable(holder, "SetupWizard.DisplayName", new Object[0]); }
  
  public static String SetupWizard_ConfigureInstance_RootUrl_Empty() { return holder.format("SetupWizard_ConfigureInstance_RootUrl_Empty", new Object[0]); }
  
  public static Localizable _SetupWizard_ConfigureInstance_RootUrl_Empty() { return new Localizable(holder, "SetupWizard_ConfigureInstance_RootUrl_Empty", new Object[0]); }
  
  public static String SetupWizard_ConfigureInstance_RootUrl_Invalid() { return holder.format("SetupWizard_ConfigureInstance_RootUrl_Invalid", new Object[0]); }
  
  public static Localizable _SetupWizard_ConfigureInstance_RootUrl_Invalid() { return new Localizable(holder, "SetupWizard_ConfigureInstance_RootUrl_Invalid", new Object[0]); }
  
  public static String SetupWizard_ConfigureInstance_ValidationErrors() { return holder.format("SetupWizard_ConfigureInstance_ValidationErrors", new Object[0]); }
  
  public static Localizable _SetupWizard_ConfigureInstance_ValidationErrors() { return new Localizable(holder, "SetupWizard_ConfigureInstance_ValidationErrors", new Object[0]); }
}
