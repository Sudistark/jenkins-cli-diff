package hudson.cli;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String CliProtocol2_displayName() { return holder.format("CliProtocol2.displayName", new Object[0]); }
  
  public static Localizable _CliProtocol2_displayName() { return new Localizable(holder, "CliProtocol2.displayName", new Object[0]); }
  
  public static String DisablePluginCommand_ShortDescription() { return holder.format("DisablePluginCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _DisablePluginCommand_ShortDescription() { return new Localizable(holder, "DisablePluginCommand.ShortDescription", new Object[0]); }
  
  public static String WaitNodeOnlineCommand_ShortDescription() { return holder.format("WaitNodeOnlineCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _WaitNodeOnlineCommand_ShortDescription() { return new Localizable(holder, "WaitNodeOnlineCommand.ShortDescription", new Object[0]); }
  
  public static String InstallPluginCommand_InstallingFromUpdateCenter(Object arg0) { return holder.format("InstallPluginCommand.InstallingFromUpdateCenter", new Object[] { arg0 }); }
  
  public static Localizable _InstallPluginCommand_InstallingFromUpdateCenter(Object arg0) { return new Localizable(holder, "InstallPluginCommand.InstallingFromUpdateCenter", new Object[] { arg0 }); }
  
  public static String VersionCommand_ShortDescription() { return holder.format("VersionCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _VersionCommand_ShortDescription() { return new Localizable(holder, "VersionCommand.ShortDescription", new Object[0]); }
  
  public static String BuildCommand_CLICause_ShortDescription(Object arg0) { return holder.format("BuildCommand.CLICause.ShortDescription", new Object[] { arg0 }); }
  
  public static Localizable _BuildCommand_CLICause_ShortDescription(Object arg0) { return new Localizable(holder, "BuildCommand.CLICause.ShortDescription", new Object[] { arg0 }); }
  
  public static String ListJobsCommand_ShortDescription() { return holder.format("ListJobsCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _ListJobsCommand_ShortDescription() { return new Localizable(holder, "ListJobsCommand.ShortDescription", new Object[0]); }
  
  public static String SafeRestartCommand_ShortDescription() { return holder.format("SafeRestartCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _SafeRestartCommand_ShortDescription() { return new Localizable(holder, "SafeRestartCommand.ShortDescription", new Object[0]); }
  
  public static String CopyJobCommand_ShortDescription() { return holder.format("CopyJobCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _CopyJobCommand_ShortDescription() { return new Localizable(holder, "CopyJobCommand.ShortDescription", new Object[0]); }
  
  public static String ReloadConfigurationCommand_ShortDescription() { return holder.format("ReloadConfigurationCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _ReloadConfigurationCommand_ShortDescription() { return new Localizable(holder, "ReloadConfigurationCommand.ShortDescription", new Object[0]); }
  
  public static String OnlineNodeCommand_ShortDescription() { return holder.format("OnlineNodeCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _OnlineNodeCommand_ShortDescription() { return new Localizable(holder, "OnlineNodeCommand.ShortDescription", new Object[0]); }
  
  public static String EnablePluginCommand_MissingDependencies(Object arg0, Object arg1) { return holder.format("EnablePluginCommand.MissingDependencies", new Object[] { arg0, arg1 }); }
  
  public static Localizable _EnablePluginCommand_MissingDependencies(Object arg0, Object arg1) { return new Localizable(holder, "EnablePluginCommand.MissingDependencies", new Object[] { arg0, arg1 }); }
  
  public static String SetBuildDescriptionCommand_ShortDescription() { return holder.format("SetBuildDescriptionCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _SetBuildDescriptionCommand_ShortDescription() { return new Localizable(holder, "SetBuildDescriptionCommand.ShortDescription", new Object[0]); }
  
  public static String QuietDownCommand_ShortDescription() { return holder.format("QuietDownCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _QuietDownCommand_ShortDescription() { return new Localizable(holder, "QuietDownCommand.ShortDescription", new Object[0]); }
  
  public static String ListPluginsCommand_ShortDescription() { return holder.format("ListPluginsCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _ListPluginsCommand_ShortDescription() { return new Localizable(holder, "ListPluginsCommand.ShortDescription", new Object[0]); }
  
  public static String DisconnectNodeCommand_ShortDescription() { return holder.format("DisconnectNodeCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _DisconnectNodeCommand_ShortDescription() { return new Localizable(holder, "DisconnectNodeCommand.ShortDescription", new Object[0]); }
  
  public static String OfflineNodeCommand_ShortDescription() { return holder.format("OfflineNodeCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _OfflineNodeCommand_ShortDescription() { return new Localizable(holder, "OfflineNodeCommand.ShortDescription", new Object[0]); }
  
  public static String DisablePluginCommand_PrintUsageSummary() { return holder.format("DisablePluginCommand.PrintUsageSummary", new Object[0]); }
  
  public static Localizable _DisablePluginCommand_PrintUsageSummary() { return new Localizable(holder, "DisablePluginCommand.PrintUsageSummary", new Object[0]); }
  
  public static String BuildCommand_CLICause_CannotBuildDisabled(Object arg0) { return holder.format("BuildCommand.CLICause.CannotBuildDisabled", new Object[] { arg0 }); }
  
  public static Localizable _BuildCommand_CLICause_CannotBuildDisabled(Object arg0) { return new Localizable(holder, "BuildCommand.CLICause.CannotBuildDisabled", new Object[] { arg0 }); }
  
  public static String WaitNodeOfflineCommand_ShortDescription() { return holder.format("WaitNodeOfflineCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _WaitNodeOfflineCommand_ShortDescription() { return new Localizable(holder, "WaitNodeOfflineCommand.ShortDescription", new Object[0]); }
  
  public static String InstallPluginCommand_InstallingPluginFromStdin() { return holder.format("InstallPluginCommand.InstallingPluginFromStdin", new Object[0]); }
  
  public static Localizable _InstallPluginCommand_InstallingPluginFromStdin() { return new Localizable(holder, "InstallPluginCommand.InstallingPluginFromStdin", new Object[0]); }
  
  public static String InstallPluginCommand_ShortDescription() { return holder.format("InstallPluginCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _InstallPluginCommand_ShortDescription() { return new Localizable(holder, "InstallPluginCommand.ShortDescription", new Object[0]); }
  
  public static String AddJobToViewCommand_ShortDescription() { return holder.format("AddJobToViewCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _AddJobToViewCommand_ShortDescription() { return new Localizable(holder, "AddJobToViewCommand.ShortDescription", new Object[0]); }
  
  public static String DeleteNodeCommand_ShortDescription() { return holder.format("DeleteNodeCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _DeleteNodeCommand_ShortDescription() { return new Localizable(holder, "DeleteNodeCommand.ShortDescription", new Object[0]); }
  
  public static String BuildCommand_ShortDescription() { return holder.format("BuildCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _BuildCommand_ShortDescription() { return new Localizable(holder, "BuildCommand.ShortDescription", new Object[0]); }
  
  public static String DeleteViewCommand_ShortDescription() { return holder.format("DeleteViewCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _DeleteViewCommand_ShortDescription() { return new Localizable(holder, "DeleteViewCommand.ShortDescription", new Object[0]); }
  
  public static String InstallPluginCommand_DidYouMean(Object arg0, Object arg1) { return holder.format("InstallPluginCommand.DidYouMean", new Object[] { arg0, arg1 }); }
  
  public static Localizable _InstallPluginCommand_DidYouMean(Object arg0, Object arg1) { return new Localizable(holder, "InstallPluginCommand.DidYouMean", new Object[] { arg0, arg1 }); }
  
  public static String InstallPluginCommand_InstallingPluginFromLocalFile(Object arg0) { return holder.format("InstallPluginCommand.InstallingPluginFromLocalFile", new Object[] { arg0 }); }
  
  public static Localizable _InstallPluginCommand_InstallingPluginFromLocalFile(Object arg0) { return new Localizable(holder, "InstallPluginCommand.InstallingPluginFromLocalFile", new Object[] { arg0 }); }
  
  public static String SetBuildDisplayNameCommand_ShortDescription() { return holder.format("SetBuildDisplayNameCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _SetBuildDisplayNameCommand_ShortDescription() { return new Localizable(holder, "SetBuildDisplayNameCommand.ShortDescription", new Object[0]); }
  
  public static String WhoAmICommand_ShortDescription() { return holder.format("WhoAmICommand.ShortDescription", new Object[0]); }
  
  public static Localizable _WhoAmICommand_ShortDescription() { return new Localizable(holder, "WhoAmICommand.ShortDescription", new Object[0]); }
  
  public static String GroovyCommand_ShortDescription() { return holder.format("GroovyCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _GroovyCommand_ShortDescription() { return new Localizable(holder, "GroovyCommand.ShortDescription", new Object[0]); }
  
  public static String DeleteJobCommand_ShortDescription() { return holder.format("DeleteJobCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _DeleteJobCommand_ShortDescription() { return new Localizable(holder, "DeleteJobCommand.ShortDescription", new Object[0]); }
  
  public static String CliProtocol_displayName() { return holder.format("CliProtocol.displayName", new Object[0]); }
  
  public static Localizable _CliProtocol_displayName() { return new Localizable(holder, "CliProtocol.displayName", new Object[0]); }
  
  public static String DisablePluginCommand_NoSuchStrategy(Object arg0, Object arg1) { return holder.format("DisablePluginCommand.NoSuchStrategy", new Object[] { arg0, arg1 }); }
  
  public static Localizable _DisablePluginCommand_NoSuchStrategy(Object arg0, Object arg1) { return new Localizable(holder, "DisablePluginCommand.NoSuchStrategy", new Object[] { arg0, arg1 }); }
  
  public static String RemoveJobFromViewCommand_ShortDescription() { return holder.format("RemoveJobFromViewCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _RemoveJobFromViewCommand_ShortDescription() { return new Localizable(holder, "RemoveJobFromViewCommand.ShortDescription", new Object[0]); }
  
  public static String SessionIdCommand_ShortDescription() { return holder.format("SessionIdCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _SessionIdCommand_ShortDescription() { return new Localizable(holder, "SessionIdCommand.ShortDescription", new Object[0]); }
  
  public static String ConnectNodeCommand_ShortDescription() { return holder.format("ConnectNodeCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _ConnectNodeCommand_ShortDescription() { return new Localizable(holder, "ConnectNodeCommand.ShortDescription", new Object[0]); }
  
  public static String InstallPluginCommand_NotAValidSourceName(Object arg0) { return holder.format("InstallPluginCommand.NotAValidSourceName", new Object[] { arg0 }); }
  
  public static Localizable _InstallPluginCommand_NotAValidSourceName(Object arg0) { return new Localizable(holder, "InstallPluginCommand.NotAValidSourceName", new Object[] { arg0 }); }
  
  public static String GetJobCommand_ShortDescription() { return holder.format("GetJobCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _GetJobCommand_ShortDescription() { return new Localizable(holder, "GetJobCommand.ShortDescription", new Object[0]); }
  
  public static String DisablePluginCommand_StatusMessage(Object arg0, Object arg1, Object arg2) { return holder.format("DisablePluginCommand.StatusMessage", new Object[] { arg0, arg1, arg2 }); }
  
  public static Localizable _DisablePluginCommand_StatusMessage(Object arg0, Object arg1, Object arg2) { return new Localizable(holder, "DisablePluginCommand.StatusMessage", new Object[] { arg0, arg1, arg2 }); }
  
  public static String EnablePluginCommand_ShortDescription() { return holder.format("EnablePluginCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _EnablePluginCommand_ShortDescription() { return new Localizable(holder, "EnablePluginCommand.ShortDescription", new Object[0]); }
  
  public static String CreateNodeCommand_ShortDescription() { return holder.format("CreateNodeCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _CreateNodeCommand_ShortDescription() { return new Localizable(holder, "CreateNodeCommand.ShortDescription", new Object[0]); }
  
  public static String InstallPluginCommand_NoUpdateDataRetrieved(Object arg0) { return holder.format("InstallPluginCommand.NoUpdateDataRetrieved", new Object[] { arg0 }); }
  
  public static Localizable _InstallPluginCommand_NoUpdateDataRetrieved(Object arg0) { return new Localizable(holder, "InstallPluginCommand.NoUpdateDataRetrieved", new Object[] { arg0 }); }
  
  public static String BuildCommand_CLICause_CannotBuildUnknownReasons(Object arg0) { return holder.format("BuildCommand.CLICause.CannotBuildUnknownReasons", new Object[] { arg0 }); }
  
  public static Localizable _BuildCommand_CLICause_CannotBuildUnknownReasons(Object arg0) { return new Localizable(holder, "BuildCommand.CLICause.CannotBuildUnknownReasons", new Object[] { arg0 }); }
  
  public static String CreateViewCommand_ShortDescription() { return holder.format("CreateViewCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _CreateViewCommand_ShortDescription() { return new Localizable(holder, "CreateViewCommand.ShortDescription", new Object[0]); }
  
  public static String InstallPluginCommand_InstallingPluginFromUrl(Object arg0) { return holder.format("InstallPluginCommand.InstallingPluginFromUrl", new Object[] { arg0 }); }
  
  public static Localizable _InstallPluginCommand_InstallingPluginFromUrl(Object arg0) { return new Localizable(holder, "InstallPluginCommand.InstallingPluginFromUrl", new Object[] { arg0 }); }
  
  public static String InstallPluginCommand_NoUpdateCenterDefined() { return holder.format("InstallPluginCommand.NoUpdateCenterDefined", new Object[0]); }
  
  public static Localizable _InstallPluginCommand_NoUpdateCenterDefined() { return new Localizable(holder, "InstallPluginCommand.NoUpdateCenterDefined", new Object[0]); }
  
  public static String ConsoleCommand_ShortDescription() { return holder.format("ConsoleCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _ConsoleCommand_ShortDescription() { return new Localizable(holder, "ConsoleCommand.ShortDescription", new Object[0]); }
  
  public static String GroovyshCommand_ShortDescription() { return holder.format("GroovyshCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _GroovyshCommand_ShortDescription() { return new Localizable(holder, "GroovyshCommand.ShortDescription", new Object[0]); }
  
  public static String UpdateViewCommand_ShortDescription() { return holder.format("UpdateViewCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _UpdateViewCommand_ShortDescription() { return new Localizable(holder, "UpdateViewCommand.ShortDescription", new Object[0]); }
  
  public static String HelpCommand_ShortDescription() { return holder.format("HelpCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _HelpCommand_ShortDescription() { return new Localizable(holder, "HelpCommand.ShortDescription", new Object[0]); }
  
  public static String ClearQueueCommand_ShortDescription() { return holder.format("ClearQueueCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _ClearQueueCommand_ShortDescription() { return new Localizable(holder, "ClearQueueCommand.ShortDescription", new Object[0]); }
  
  public static String UpdateNodeCommand_ShortDescription() { return holder.format("UpdateNodeCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _UpdateNodeCommand_ShortDescription() { return new Localizable(holder, "UpdateNodeCommand.ShortDescription", new Object[0]); }
  
  public static String CancelQuietDownCommand_ShortDescription() { return holder.format("CancelQuietDownCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _CancelQuietDownCommand_ShortDescription() { return new Localizable(holder, "CancelQuietDownCommand.ShortDescription", new Object[0]); }
  
  public static String UpdateJobCommand_ShortDescription() { return holder.format("UpdateJobCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _UpdateJobCommand_ShortDescription() { return new Localizable(holder, "UpdateJobCommand.ShortDescription", new Object[0]); }
  
  public static String EnablePluginCommand_NoSuchPlugin(Object arg0) { return holder.format("EnablePluginCommand.NoSuchPlugin", new Object[] { arg0 }); }
  
  public static Localizable _EnablePluginCommand_NoSuchPlugin(Object arg0) { return new Localizable(holder, "EnablePluginCommand.NoSuchPlugin", new Object[] { arg0 }); }
  
  public static String BuildCommand_CLICause_CannotBuildConfigNotSaved(Object arg0) { return holder.format("BuildCommand.CLICause.CannotBuildConfigNotSaved", new Object[] { arg0 }); }
  
  public static Localizable _BuildCommand_CLICause_CannotBuildConfigNotSaved(Object arg0) { return new Localizable(holder, "BuildCommand.CLICause.CannotBuildConfigNotSaved", new Object[] { arg0 }); }
  
  public static String ReloadJobCommand_ShortDescription() { return holder.format("ReloadJobCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _ReloadJobCommand_ShortDescription() { return new Localizable(holder, "ReloadJobCommand.ShortDescription", new Object[0]); }
  
  public static String CreateJobCommand_ShortDescription() { return holder.format("CreateJobCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _CreateJobCommand_ShortDescription() { return new Localizable(holder, "CreateJobCommand.ShortDescription", new Object[0]); }
  
  public static String DeleteBuildsCommand_ShortDescription() { return holder.format("DeleteBuildsCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _DeleteBuildsCommand_ShortDescription() { return new Localizable(holder, "DeleteBuildsCommand.ShortDescription", new Object[0]); }
  
  public static String ListChangesCommand_ShortDescription() { return holder.format("ListChangesCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _ListChangesCommand_ShortDescription() { return new Localizable(holder, "ListChangesCommand.ShortDescription", new Object[0]); }
  
  public static String GetNodeCommand_ShortDescription() { return holder.format("GetNodeCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _GetNodeCommand_ShortDescription() { return new Localizable(holder, "GetNodeCommand.ShortDescription", new Object[0]); }
  
  public static String GetViewCommand_ShortDescription() { return holder.format("GetViewCommand.ShortDescription", new Object[0]); }
  
  public static Localizable _GetViewCommand_ShortDescription() { return new Localizable(holder, "GetViewCommand.ShortDescription", new Object[0]); }
}
