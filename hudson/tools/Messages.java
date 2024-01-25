package hudson.tools;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String ZipExtractionInstaller_malformed_url() { return holder.format("ZipExtractionInstaller.malformed_url", new Object[0]); }
  
  public static Localizable _ZipExtractionInstaller_malformed_url() { return new Localizable(holder, "ZipExtractionInstaller.malformed_url", new Object[0]); }
  
  public static String ToolLocationNodeProperty_displayName() { return holder.format("ToolLocationNodeProperty.displayName", new Object[0]); }
  
  public static Localizable _ToolLocationNodeProperty_displayName() { return new Localizable(holder, "ToolLocationNodeProperty.displayName", new Object[0]); }
  
  public static String CommandInstaller_DescriptorImpl_displayName() { return holder.format("CommandInstaller.DescriptorImpl.displayName", new Object[0]); }
  
  public static Localizable _CommandInstaller_DescriptorImpl_displayName() { return new Localizable(holder, "CommandInstaller.DescriptorImpl.displayName", new Object[0]); }
  
  public static String CannotBeInstalled(Object arg0, Object arg1, Object arg2) { return holder.format("CannotBeInstalled", new Object[] { arg0, arg1, arg2 }); }
  
  public static Localizable _CannotBeInstalled(Object arg0, Object arg1, Object arg2) { return new Localizable(holder, "CannotBeInstalled", new Object[] { arg0, arg1, arg2 }); }
  
  public static String CommandInstaller_no_command() { return holder.format("CommandInstaller.no_command", new Object[0]); }
  
  public static Localizable _CommandInstaller_no_command() { return new Localizable(holder, "CommandInstaller.no_command", new Object[0]); }
  
  public static String ZipExtractionInstaller_DescriptorImpl_displayName() { return holder.format("ZipExtractionInstaller.DescriptorImpl.displayName", new Object[0]); }
  
  public static Localizable _ZipExtractionInstaller_DescriptorImpl_displayName() { return new Localizable(holder, "ZipExtractionInstaller.DescriptorImpl.displayName", new Object[0]); }
  
  public static String InstallSourceProperty_DescriptorImpl_displayName() { return holder.format("InstallSourceProperty.DescriptorImpl.displayName", new Object[0]); }
  
  public static Localizable _InstallSourceProperty_DescriptorImpl_displayName() { return new Localizable(holder, "InstallSourceProperty.DescriptorImpl.displayName", new Object[0]); }
  
  public static String CommandInstaller_no_toolHome() { return holder.format("CommandInstaller.no_toolHome", new Object[0]); }
  
  public static Localizable _CommandInstaller_no_toolHome() { return new Localizable(holder, "CommandInstaller.no_toolHome", new Object[0]); }
  
  public static String BatchCommandInstaller_DescriptorImpl_displayName() { return holder.format("BatchCommandInstaller.DescriptorImpl.displayName", new Object[0]); }
  
  public static Localizable _BatchCommandInstaller_DescriptorImpl_displayName() { return new Localizable(holder, "BatchCommandInstaller.DescriptorImpl.displayName", new Object[0]); }
  
  public static String ZipExtractionInstaller_bad_connection() { return holder.format("ZipExtractionInstaller.bad_connection", new Object[0]); }
  
  public static Localizable _ZipExtractionInstaller_bad_connection() { return new Localizable(holder, "ZipExtractionInstaller.bad_connection", new Object[0]); }
  
  public static String ZipExtractionInstaller_could_not_connect() { return holder.format("ZipExtractionInstaller.could_not_connect", new Object[0]); }
  
  public static Localizable _ZipExtractionInstaller_could_not_connect() { return new Localizable(holder, "ZipExtractionInstaller.could_not_connect", new Object[0]); }
  
  public static String ToolDescriptor_NotADirectory(Object arg0) { return holder.format("ToolDescriptor.NotADirectory", new Object[] { arg0 }); }
  
  public static Localizable _ToolDescriptor_NotADirectory(Object arg0) { return new Localizable(holder, "ToolDescriptor.NotADirectory", new Object[] { arg0 }); }
}
