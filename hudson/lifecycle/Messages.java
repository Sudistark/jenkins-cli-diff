package hudson.lifecycle;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String WindowsSlaveInstaller_DotNetRequired() { return holder.format("WindowsSlaveInstaller.DotNetRequired", new Object[0]); }
  
  public static Localizable _WindowsSlaveInstaller_DotNetRequired() { return new Localizable(holder, "WindowsSlaveInstaller.DotNetRequired", new Object[0]); }
  
  public static String WindowsSlaveInstaller_InstallationSuccessful() { return holder.format("WindowsSlaveInstaller.InstallationSuccessful", new Object[0]); }
  
  public static Localizable _WindowsSlaveInstaller_InstallationSuccessful() { return new Localizable(holder, "WindowsSlaveInstaller.InstallationSuccessful", new Object[0]); }
  
  public static String WindowsInstallerLink_DisplayName() { return holder.format("WindowsInstallerLink.DisplayName", new Object[0]); }
  
  public static Localizable _WindowsInstallerLink_DisplayName() { return new Localizable(holder, "WindowsInstallerLink.DisplayName", new Object[0]); }
  
  public static String WindowsInstallerLink_Description() { return holder.format("WindowsInstallerLink.Description", new Object[0]); }
  
  public static Localizable _WindowsInstallerLink_Description() { return new Localizable(holder, "WindowsInstallerLink.Description", new Object[0]); }
  
  public static String WindowsSlaveInstaller_ConfirmInstallation() { return holder.format("WindowsSlaveInstaller.ConfirmInstallation", new Object[0]); }
  
  public static Localizable _WindowsSlaveInstaller_ConfirmInstallation() { return new Localizable(holder, "WindowsSlaveInstaller.ConfirmInstallation", new Object[0]); }
  
  public static String WindowsSlaveInstaller_RootFsDoesntExist(Object arg0) { return holder.format("WindowsSlaveInstaller.RootFsDoesntExist", new Object[] { arg0 }); }
  
  public static Localizable _WindowsSlaveInstaller_RootFsDoesntExist(Object arg0) { return new Localizable(holder, "WindowsSlaveInstaller.RootFsDoesntExist", new Object[] { arg0 }); }
}
