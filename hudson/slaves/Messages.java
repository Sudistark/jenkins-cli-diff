package hudson.slaves;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String NodeProvisioner_EmptyString() { return holder.format("NodeProvisioner.EmptyString", new Object[0]); }
  
  public static Localizable _NodeProvisioner_EmptyString() { return new Localizable(holder, "NodeProvisioner.EmptyString", new Object[0]); }
  
  public static String OfflineCause_LaunchFailed() { return holder.format("OfflineCause.LaunchFailed", new Object[0]); }
  
  public static Localizable _OfflineCause_LaunchFailed() { return new Localizable(holder, "OfflineCause.LaunchFailed", new Object[0]); }
  
  public static String RetentionStrategy_Always_displayName() { return holder.format("RetentionStrategy.Always.displayName", new Object[0]); }
  
  public static Localizable _RetentionStrategy_Always_displayName() { return new Localizable(holder, "RetentionStrategy.Always.displayName", new Object[0]); }
  
  public static String OfflineCause_connection_was_broken_simple() { return holder.format("OfflineCause.connection_was_broken_simple", new Object[0]); }
  
  public static Localizable _OfflineCause_connection_was_broken_simple() { return new Localizable(holder, "OfflineCause.connection_was_broken_simple", new Object[0]); }
  
  public static String OfflineCause_connection_was_broken_(Object arg0) { return holder.format("OfflineCause.connection_was_broken_", new Object[] { arg0 }); }
  
  public static Localizable _OfflineCause_connection_was_broken_(Object arg0) { return new Localizable(holder, "OfflineCause.connection_was_broken_", new Object[] { arg0 }); }
  
  public static String SimpleScheduledRetentionStrategy_FinishedUpTime() { return holder.format("SimpleScheduledRetentionStrategy.FinishedUpTime", new Object[0]); }
  
  public static Localizable _SimpleScheduledRetentionStrategy_FinishedUpTime() { return new Localizable(holder, "SimpleScheduledRetentionStrategy.FinishedUpTime", new Object[0]); }
  
  public static String ComputerLauncher_NoJavaFound(Object arg0) { return holder.format("ComputerLauncher.NoJavaFound", new Object[] { arg0 }); }
  
  public static Localizable _ComputerLauncher_NoJavaFound(Object arg0) { return new Localizable(holder, "ComputerLauncher.NoJavaFound", new Object[] { arg0 }); }
  
  public static String JNLPLauncher_TunnelingNotSupported() { return holder.format("JNLPLauncher.TunnelingNotSupported", new Object[0]); }
  
  public static Localizable _JNLPLauncher_TunnelingNotSupported() { return new Localizable(holder, "JNLPLauncher.TunnelingNotSupported", new Object[0]); }
  
  public static String JNLPLauncher_displayName() { return holder.format("JNLPLauncher.displayName", new Object[0]); }
  
  public static Localizable _JNLPLauncher_displayName() { return new Localizable(holder, "JNLPLauncher.displayName", new Object[0]); }
  
  public static String SimpleScheduledRetentionStrategy_displayName() { return holder.format("SimpleScheduledRetentionStrategy.displayName", new Object[0]); }
  
  public static Localizable _SimpleScheduledRetentionStrategy_displayName() { return new Localizable(holder, "SimpleScheduledRetentionStrategy.displayName", new Object[0]); }
  
  public static String ComputerLauncher_JavaVersionResult(Object arg0, Object arg1) { return holder.format("ComputerLauncher.JavaVersionResult", new Object[] { arg0, arg1 }); }
  
  public static Localizable _ComputerLauncher_JavaVersionResult(Object arg0, Object arg1) { return new Localizable(holder, "ComputerLauncher.JavaVersionResult", new Object[] { arg0, arg1 }); }
  
  public static String Cloud_RequiredName() { return holder.format("Cloud.RequiredName", new Object[0]); }
  
  public static Localizable _Cloud_RequiredName() { return new Localizable(holder, "Cloud.RequiredName", new Object[0]); }
  
  public static String NodeDescriptor_CheckName_Mandatory() { return holder.format("NodeDescriptor.CheckName.Mandatory", new Object[0]); }
  
  public static Localizable _NodeDescriptor_CheckName_Mandatory() { return new Localizable(holder, "NodeDescriptor.CheckName.Mandatory", new Object[0]); }
  
  public static String JNLPLauncher_WebsocketNotEnabled() { return holder.format("JNLPLauncher.WebsocketNotEnabled", new Object[0]); }
  
  public static Localizable _JNLPLauncher_WebsocketNotEnabled() { return new Localizable(holder, "JNLPLauncher.WebsocketNotEnabled", new Object[0]); }
  
  public static String ConnectionActivityMonitor_OfflineCause() { return holder.format("ConnectionActivityMonitor.OfflineCause", new Object[0]); }
  
  public static Localizable _ConnectionActivityMonitor_OfflineCause() { return new Localizable(holder, "ConnectionActivityMonitor.OfflineCause", new Object[0]); }
  
  public static String RetentionStrategy_Demand_OfflineIdle() { return holder.format("RetentionStrategy.Demand.OfflineIdle", new Object[0]); }
  
  public static Localizable _RetentionStrategy_Demand_OfflineIdle() { return new Localizable(holder, "RetentionStrategy.Demand.OfflineIdle", new Object[0]); }
  
  public static String ComputerLauncher_UnknownJavaVersion(Object arg0) { return holder.format("ComputerLauncher.UnknownJavaVersion", new Object[] { arg0 }); }
  
  public static Localizable _ComputerLauncher_UnknownJavaVersion(Object arg0) { return new Localizable(holder, "ComputerLauncher.UnknownJavaVersion", new Object[] { arg0 }); }
  
  public static String ComputerLauncher_unexpectedError() { return holder.format("ComputerLauncher.unexpectedError", new Object[0]); }
  
  public static Localizable _ComputerLauncher_unexpectedError() { return new Localizable(holder, "ComputerLauncher.unexpectedError", new Object[0]); }
  
  public static String RetentionStrategy_Demand_displayName() { return holder.format("RetentionStrategy.Demand.displayName", new Object[0]); }
  
  public static Localizable _RetentionStrategy_Demand_displayName() { return new Localizable(holder, "RetentionStrategy.Demand.displayName", new Object[0]); }
  
  public static String DumbSlave_displayName() { return holder.format("DumbSlave.displayName", new Object[0]); }
  
  public static Localizable _DumbSlave_displayName() { return new Localizable(holder, "DumbSlave.displayName", new Object[0]); }
  
  public static String ComputerLauncher_abortedLaunch() { return holder.format("ComputerLauncher.abortedLaunch", new Object[0]); }
  
  public static Localizable _ComputerLauncher_abortedLaunch() { return new Localizable(holder, "ComputerLauncher.abortedLaunch", new Object[0]); }
  
  public static String JNLPLauncher_TCPPortDisabled() { return holder.format("JNLPLauncher.TCPPortDisabled", new Object[0]); }
  
  public static Localizable _JNLPLauncher_TCPPortDisabled() { return new Localizable(holder, "JNLPLauncher.TCPPortDisabled", new Object[0]); }
  
  public static String EnvironmentVariablesNodeProperty_displayName() { return holder.format("EnvironmentVariablesNodeProperty.displayName", new Object[0]); }
  
  public static Localizable _EnvironmentVariablesNodeProperty_displayName() { return new Localizable(holder, "EnvironmentVariablesNodeProperty.displayName", new Object[0]); }
  
  public static String Cloud_ProvisionPermission_Description() { return holder.format("Cloud.ProvisionPermission.Description", new Object[0]); }
  
  public static Localizable _Cloud_ProvisionPermission_Description() { return new Localizable(holder, "Cloud.ProvisionPermission.Description", new Object[0]); }
  
  public static String SlaveComputer_DisconnectedBy(Object arg0, Object arg1) { return holder.format("SlaveComputer.DisconnectedBy", new Object[] { arg0, arg1 }); }
  
  public static Localizable _SlaveComputer_DisconnectedBy(Object arg0, Object arg1) { return new Localizable(holder, "SlaveComputer.DisconnectedBy", new Object[] { arg0, arg1 }); }
  
  public static String JNLPLauncher_InstanceIdentityRequired() { return holder.format("JNLPLauncher.InstanceIdentityRequired", new Object[0]); }
  
  public static Localizable _JNLPLauncher_InstanceIdentityRequired() { return new Localizable(holder, "JNLPLauncher.InstanceIdentityRequired", new Object[0]); }
}
