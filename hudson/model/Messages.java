package hudson.model;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String View_ConfigurePermission_Description() { return holder.format("View.ConfigurePermission.Description", new Object[0]); }
  
  public static Localizable _View_ConfigurePermission_Description() { return new Localizable(holder, "View.ConfigurePermission.Description", new Object[0]); }
  
  public static String Job_NOfMFailed(Object arg0, Object arg1) { return holder.format("Job.NOfMFailed", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Job_NOfMFailed(Object arg0, Object arg1) { return new Localizable(holder, "Job.NOfMFailed", new Object[] { arg0, arg1 }); }
  
  public static String Permalink_LastBuild() { return holder.format("Permalink.LastBuild", new Object[0]); }
  
  public static Localizable _Permalink_LastBuild() { return new Localizable(holder, "Permalink.LastBuild", new Object[0]); }
  
  public static String ComputerSet_NoSuchSlave(Object arg0) { return holder.format("ComputerSet.NoSuchSlave", new Object[] { arg0 }); }
  
  public static Localizable _ComputerSet_NoSuchSlave(Object arg0) { return new Localizable(holder, "ComputerSet.NoSuchSlave", new Object[] { arg0 }); }
  
  public static String Hudson_RunScriptsPermission_Description() { return holder.format("Hudson.RunScriptsPermission.Description", new Object[0]); }
  
  public static Localizable _Hudson_RunScriptsPermission_Description() { return new Localizable(holder, "Hudson.RunScriptsPermission.Description", new Object[0]); }
  
  public static String Queue_AllNodesOffline(Object arg0) { return holder.format("Queue.AllNodesOffline", new Object[] { arg0 }); }
  
  public static Localizable _Queue_AllNodesOffline(Object arg0) { return new Localizable(holder, "Queue.AllNodesOffline", new Object[] { arg0 }); }
  
  public static String Cause_UpstreamCause_ShortDescription(Object arg0, Object arg1) { return holder.format("Cause.UpstreamCause.ShortDescription", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Cause_UpstreamCause_ShortDescription(Object arg0, Object arg1) { return new Localizable(holder, "Cause.UpstreamCause.ShortDescription", new Object[] { arg0, arg1 }); }
  
  public static String UpdateCenter_PluginCategory_security() { return holder.format("UpdateCenter.PluginCategory.security", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_security() { return new Localizable(holder, "UpdateCenter.PluginCategory.security", new Object[0]); }
  
  public static String Node_LabelMissing(Object arg0, Object arg1) { return holder.format("Node.LabelMissing", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Node_LabelMissing(Object arg0, Object arg1) { return new Localizable(holder, "Node.LabelMissing", new Object[] { arg0, arg1 }); }
  
  public static String Jenkins_NotAllowedName(Object arg0) { return holder.format("Jenkins.NotAllowedName", new Object[] { arg0 }); }
  
  public static Localizable _Jenkins_NotAllowedName(Object arg0) { return new Localizable(holder, "Jenkins.NotAllowedName", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_trigger() { return holder.format("UpdateCenter.PluginCategory.trigger", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_trigger() { return new Localizable(holder, "UpdateCenter.PluginCategory.trigger", new Object[0]); }
  
  public static String JDK_DisplayName() { return holder.format("JDK.DisplayName", new Object[0]); }
  
  public static Localizable _JDK_DisplayName() { return new Localizable(holder, "JDK.DisplayName", new Object[0]); }
  
  public static String Computer_DisconnectPermission_Description() { return holder.format("Computer.DisconnectPermission.Description", new Object[0]); }
  
  public static Localizable _Computer_DisconnectPermission_Description() { return new Localizable(holder, "Computer.DisconnectPermission.Description", new Object[0]); }
  
  public static String Hudson_NotAPlugin(Object arg0) { return holder.format("Hudson.NotAPlugin", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_NotAPlugin(Object arg0) { return new Localizable(holder, "Hudson.NotAPlugin", new Object[] { arg0 }); }
  
  public static String Run_Summary_NotBuilt() { return holder.format("Run.Summary.NotBuilt", new Object[0]); }
  
  public static Localizable _Run_Summary_NotBuilt() { return new Localizable(holder, "Run.Summary.NotBuilt", new Object[0]); }
  
  public static String Hudson_ViewName() { return holder.format("Hudson.ViewName", new Object[0]); }
  
  public static Localizable _Hudson_ViewName() { return new Localizable(holder, "Hudson.ViewName", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_listview_column() { return holder.format("UpdateCenter.PluginCategory.listview-column", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_listview_column() { return new Localizable(holder, "UpdateCenter.PluginCategory.listview-column", new Object[0]); }
  
  public static String Permalink_LastStableBuild() { return holder.format("Permalink.LastStableBuild", new Object[0]); }
  
  public static Localizable _Permalink_LastStableBuild() { return new Localizable(holder, "Permalink.LastStableBuild", new Object[0]); }
  
  public static String Queue_WaitingForNextAvailableExecutor() { return holder.format("Queue.WaitingForNextAvailableExecutor", new Object[0]); }
  
  public static Localizable _Queue_WaitingForNextAvailableExecutor() { return new Localizable(holder, "Queue.WaitingForNextAvailableExecutor", new Object[0]); }
  
  public static String AbstractBuild_KeptBecause(Object arg0) { return holder.format("AbstractBuild.KeptBecause", new Object[] { arg0 }); }
  
  public static Localizable _AbstractBuild_KeptBecause(Object arg0) { return new Localizable(holder, "AbstractBuild.KeptBecause", new Object[] { arg0 }); }
  
  public static String Label_ProvisionedFrom(Object arg0) { return holder.format("Label.ProvisionedFrom", new Object[] { arg0 }); }
  
  public static Localizable _Label_ProvisionedFrom(Object arg0) { return new Localizable(holder, "Label.ProvisionedFrom", new Object[] { arg0 }); }
  
  public static String Hudson_DisplayName() { return holder.format("Hudson.DisplayName", new Object[0]); }
  
  public static Localizable _Hudson_DisplayName() { return new Localizable(holder, "Hudson.DisplayName", new Object[0]); }
  
  public static String AbstractItem_Pronoun() { return holder.format("AbstractItem.Pronoun", new Object[0]); }
  
  public static Localizable _AbstractItem_Pronoun() { return new Localizable(holder, "AbstractItem.Pronoun", new Object[0]); }
  
  public static String Item_DELETE_description() { return holder.format("Item.DELETE.description", new Object[0]); }
  
  public static Localizable _Item_DELETE_description() { return new Localizable(holder, "Item.DELETE.description", new Object[0]); }
  
  public static String Queue_NodeOffline(Object arg0) { return holder.format("Queue.NodeOffline", new Object[] { arg0 }); }
  
  public static Localizable _Queue_NodeOffline(Object arg0) { return new Localizable(holder, "Queue.NodeOffline", new Object[] { arg0 }); }
  
  public static String LoadStatistics_Legends_DefinedExecutors() { return holder.format("LoadStatistics.Legends.DefinedExecutors", new Object[0]); }
  
  public static Localizable _LoadStatistics_Legends_DefinedExecutors() { return new Localizable(holder, "LoadStatistics.Legends.DefinedExecutors", new Object[0]); }
  
  public static String Run_ArtifactsPermission_Description() { return holder.format("Run.ArtifactsPermission.Description", new Object[0]); }
  
  public static Localizable _Run_ArtifactsPermission_Description() { return new Localizable(holder, "Run.ArtifactsPermission.Description", new Object[0]); }
  
  public static String CLI_safe_restart_shortDescription() { return holder.format("CLI.safe-restart.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_safe_restart_shortDescription() { return new Localizable(holder, "CLI.safe-restart.shortDescription", new Object[0]); }
  
  public static String CLI_restart_shortDescription() { return holder.format("CLI.restart.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_restart_shortDescription() { return new Localizable(holder, "CLI.restart.shortDescription", new Object[0]); }
  
  public static String FreeStyleProject_DisplayName() { return holder.format("FreeStyleProject.DisplayName", new Object[0]); }
  
  public static Localizable _FreeStyleProject_DisplayName() { return new Localizable(holder, "FreeStyleProject.DisplayName", new Object[0]); }
  
  public static String Computer_NoSuchSlaveExists(Object arg0, Object arg1) { return holder.format("Computer.NoSuchSlaveExists", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Computer_NoSuchSlaveExists(Object arg0, Object arg1) { return new Localizable(holder, "Computer.NoSuchSlaveExists", new Object[] { arg0, arg1 }); }
  
  public static String AbstractBuild_BuildingInWorkspace(Object arg0) { return holder.format("AbstractBuild.BuildingInWorkspace", new Object[] { arg0 }); }
  
  public static Localizable _AbstractBuild_BuildingInWorkspace(Object arg0) { return new Localizable(holder, "AbstractBuild.BuildingInWorkspace", new Object[] { arg0 }); }
  
  public static String FileParameterValue_IndexTitle() { return holder.format("FileParameterValue.IndexTitle", new Object[0]); }
  
  public static Localizable _FileParameterValue_IndexTitle() { return new Localizable(holder, "FileParameterValue.IndexTitle", new Object[0]); }
  
  public static String Queue_InProgress() { return holder.format("Queue.InProgress", new Object[0]); }
  
  public static Localizable _Queue_InProgress() { return new Localizable(holder, "Queue.InProgress", new Object[0]); }
  
  public static String Node_Mode_EXCLUSIVE() { return holder.format("Node.Mode.EXCLUSIVE", new Object[0]); }
  
  public static Localizable _Node_Mode_EXCLUSIVE() { return new Localizable(holder, "Node.Mode.EXCLUSIVE", new Object[0]); }
  
  public static String User_IllegalFullname(Object arg0) { return holder.format("User.IllegalFullname", new Object[] { arg0 }); }
  
  public static Localizable _User_IllegalFullname(Object arg0) { return new Localizable(holder, "User.IllegalFullname", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_misc() { return holder.format("UpdateCenter.PluginCategory.misc", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_misc() { return new Localizable(holder, "UpdateCenter.PluginCategory.misc", new Object[0]); }
  
  public static String Hudson_NotAPositiveNumber() { return holder.format("Hudson.NotAPositiveNumber", new Object[0]); }
  
  public static Localizable _Hudson_NotAPositiveNumber() { return new Localizable(holder, "Hudson.NotAPositiveNumber", new Object[0]); }
  
  public static String LoadStatistics_Legends_AvailableExecutors() { return holder.format("LoadStatistics.Legends.AvailableExecutors", new Object[0]); }
  
  public static Localizable _LoadStatistics_Legends_AvailableExecutors() { return new Localizable(holder, "LoadStatistics.Legends.AvailableExecutors", new Object[0]); }
  
  public static String Run_Summary_Stable() { return holder.format("Run.Summary.Stable", new Object[0]); }
  
  public static Localizable _Run_Summary_Stable() { return new Localizable(holder, "Run.Summary.Stable", new Object[0]); }
  
  public static String ProxyView_NoSuchViewExists(Object arg0) { return holder.format("ProxyView.NoSuchViewExists", new Object[] { arg0 }); }
  
  public static Localizable _ProxyView_NoSuchViewExists(Object arg0) { return new Localizable(holder, "ProxyView.NoSuchViewExists", new Object[] { arg0 }); }
  
  public static String Cause_RemoteCause_ShortDescriptionWithNote(Object arg0, Object arg1) { return holder.format("Cause.RemoteCause.ShortDescriptionWithNote", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Cause_RemoteCause_ShortDescriptionWithNote(Object arg0, Object arg1) { return new Localizable(holder, "Cause.RemoteCause.ShortDescriptionWithNote", new Object[] { arg0, arg1 }); }
  
  public static String Hudson_ViewAlreadyExists(Object arg0) { return holder.format("Hudson.ViewAlreadyExists", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_ViewAlreadyExists(Object arg0) { return new Localizable(holder, "Hudson.ViewAlreadyExists", new Object[] { arg0 }); }
  
  public static String BallColor_Pending() { return holder.format("BallColor.Pending", new Object[0]); }
  
  public static Localizable _BallColor_Pending() { return new Localizable(holder, "BallColor.Pending", new Object[0]); }
  
  public static String Queue_LabelHasNoNodes(Object arg0) { return holder.format("Queue.LabelHasNoNodes", new Object[] { arg0 }); }
  
  public static Localizable _Queue_LabelHasNoNodes(Object arg0) { return new Localizable(holder, "Queue.LabelHasNoNodes", new Object[] { arg0 }); }
  
  public static String UpdateCenter_CoreUpdateMonitor_DisplayName() { return holder.format("UpdateCenter.CoreUpdateMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _UpdateCenter_CoreUpdateMonitor_DisplayName() { return new Localizable(holder, "UpdateCenter.CoreUpdateMonitor.DisplayName", new Object[0]); }
  
  public static String Run__is_waiting_for_a_checkpoint_on_(Object arg0, Object arg1) { return holder.format("Run._is_waiting_for_a_checkpoint_on_", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Run__is_waiting_for_a_checkpoint_on_(Object arg0, Object arg1) { return new Localizable(holder, "Run._is_waiting_for_a_checkpoint_on_", new Object[] { arg0, arg1 }); }
  
  public static String Hudson_NodeDescription() { return holder.format("Hudson.NodeDescription", new Object[0]); }
  
  public static Localizable _Hudson_NodeDescription() { return new Localizable(holder, "Hudson.NodeDescription", new Object[0]); }
  
  public static String Computer_ExtendedReadPermission_Description() { return holder.format("Computer.ExtendedReadPermission.Description", new Object[0]); }
  
  public static Localizable _Computer_ExtendedReadPermission_Description() { return new Localizable(holder, "Computer.ExtendedReadPermission.Description", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_test() { return holder.format("UpdateCenter.PluginCategory.test", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_test() { return new Localizable(holder, "UpdateCenter.PluginCategory.test", new Object[0]); }
  
  public static String MyViewsProperty_DisplayName() { return holder.format("MyViewsProperty.DisplayName", new Object[0]); }
  
  public static Localizable _MyViewsProperty_DisplayName() { return new Localizable(holder, "MyViewsProperty.DisplayName", new Object[0]); }
  
  public static String Run_Summary_Aborted() { return holder.format("Run.Summary.Aborted", new Object[0]); }
  
  public static Localizable _Run_Summary_Aborted() { return new Localizable(holder, "Run.Summary.Aborted", new Object[0]); }
  
  public static String PasswordParameterDefinition_DisplayName() { return holder.format("PasswordParameterDefinition.DisplayName", new Object[0]); }
  
  public static Localizable _PasswordParameterDefinition_DisplayName() { return new Localizable(holder, "PasswordParameterDefinition.DisplayName", new Object[0]); }
  
  public static String Run_running_as_SYSTEM() { return holder.format("Run.running_as_SYSTEM", new Object[0]); }
  
  public static Localizable _Run_running_as_SYSTEM() { return new Localizable(holder, "Run.running_as_SYSTEM", new Object[0]); }
  
  public static String BuildAuthorizationToken_InvalidTokenProvided() { return holder.format("BuildAuthorizationToken.InvalidTokenProvided", new Object[0]); }
  
  public static Localizable _BuildAuthorizationToken_InvalidTokenProvided() { return new Localizable(holder, "BuildAuthorizationToken.InvalidTokenProvided", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_devsecops() { return holder.format("UpdateCenter.PluginCategory.devsecops", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_devsecops() { return new Localizable(holder, "UpdateCenter.PluginCategory.devsecops", new Object[0]); }
  
  public static String BooleanParameterDefinition_DisplayName() { return holder.format("BooleanParameterDefinition.DisplayName", new Object[0]); }
  
  public static Localizable _BooleanParameterDefinition_DisplayName() { return new Localizable(holder, "BooleanParameterDefinition.DisplayName", new Object[0]); }
  
  public static String Node_LackingBuildPermission(Object arg0, Object arg1) { return holder.format("Node.LackingBuildPermission", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Node_LackingBuildPermission(Object arg0, Object arg1) { return new Localizable(holder, "Node.LackingBuildPermission", new Object[] { arg0, arg1 }); }
  
  public static String LabelExpression_LabelLink(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) { return holder.format("LabelExpression.LabelLink", new Object[] { arg0, arg1, arg2, arg3, arg4 }); }
  
  public static Localizable _LabelExpression_LabelLink(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) { return new Localizable(holder, "LabelExpression.LabelLink", new Object[] { arg0, arg1, arg2, arg3, arg4 }); }
  
  public static String Run_NotStartedYet() { return holder.format("Run.NotStartedYet", new Object[0]); }
  
  public static Localizable _Run_NotStartedYet() { return new Localizable(holder, "Run.NotStartedYet", new Object[0]); }
  
  public static String LoadStatistics_Legends_TotalExecutors() { return holder.format("LoadStatistics.Legends.TotalExecutors", new Object[0]); }
  
  public static Localizable _LoadStatistics_Legends_TotalExecutors() { return new Localizable(holder, "LoadStatistics.Legends.TotalExecutors", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_post_build() { return holder.format("UpdateCenter.PluginCategory.post-build", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_post_build() { return new Localizable(holder, "UpdateCenter.PluginCategory.post-build", new Object[0]); }
  
  public static String Executor_NotAvailable() { return holder.format("Executor.NotAvailable", new Object[0]); }
  
  public static Localizable _Executor_NotAvailable() { return new Localizable(holder, "Executor.NotAvailable", new Object[0]); }
  
  public static String AbstractItem_BeingDeleted(Object arg0) { return holder.format("AbstractItem.BeingDeleted", new Object[] { arg0 }); }
  
  public static Localizable _AbstractItem_BeingDeleted(Object arg0) { return new Localizable(holder, "AbstractItem.BeingDeleted", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_page_decorator() { return holder.format("UpdateCenter.PluginCategory.page-decorator", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_page_decorator() { return new Localizable(holder, "UpdateCenter.PluginCategory.page-decorator", new Object[0]); }
  
  public static String Run_UnableToDelete(Object arg0, Object arg1) { return holder.format("Run.UnableToDelete", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Run_UnableToDelete(Object arg0, Object arg1) { return new Localizable(holder, "Run.UnableToDelete", new Object[] { arg0, arg1 }); }
  
  public static String ManageJenkinsAction_DisplayName() { return holder.format("ManageJenkinsAction.DisplayName", new Object[0]); }
  
  public static Localizable _ManageJenkinsAction_DisplayName() { return new Localizable(holder, "ManageJenkinsAction.DisplayName", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_buildwrapper() { return holder.format("UpdateCenter.PluginCategory.buildwrapper", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_buildwrapper() { return new Localizable(holder, "UpdateCenter.PluginCategory.buildwrapper", new Object[0]); }
  
  public static String Hudson_UnsafeChar(Object arg0) { return holder.format("Hudson.UnsafeChar", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_UnsafeChar(Object arg0) { return new Localizable(holder, "Hudson.UnsafeChar", new Object[] { arg0 }); }
  
  public static String Label_InvalidLabel() { return holder.format("Label.InvalidLabel", new Object[0]); }
  
  public static Localizable _Label_InvalidLabel() { return new Localizable(holder, "Label.InvalidLabel", new Object[0]); }
  
  public static String Run_BuildAborted() { return holder.format("Run.BuildAborted", new Object[0]); }
  
  public static Localizable _Run_BuildAborted() { return new Localizable(holder, "Run.BuildAborted", new Object[0]); }
  
  public static String Run_MarkedExplicitly() { return holder.format("Run.MarkedExplicitly", new Object[0]); }
  
  public static Localizable _Run_MarkedExplicitly() { return new Localizable(holder, "Run.MarkedExplicitly", new Object[0]); }
  
  public static String LabelExpression_InvalidBooleanExpression(Object arg0) { return holder.format("LabelExpression.InvalidBooleanExpression", new Object[] { arg0 }); }
  
  public static Localizable _LabelExpression_InvalidBooleanExpression(Object arg0) { return new Localizable(holder, "LabelExpression.InvalidBooleanExpression", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_view() { return holder.format("UpdateCenter.PluginCategory.view", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_view() { return new Localizable(holder, "UpdateCenter.PluginCategory.view", new Object[0]); }
  
  public static String Run_ArtifactsBrowserTitle(Object arg0, Object arg1) { return holder.format("Run.ArtifactsBrowserTitle", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Run_ArtifactsBrowserTitle(Object arg0, Object arg1) { return new Localizable(holder, "Run.ArtifactsBrowserTitle", new Object[] { arg0, arg1 }); }
  
  public static String Cause_LegacyCodeCause_ShortDescription() { return holder.format("Cause.LegacyCodeCause.ShortDescription", new Object[0]); }
  
  public static Localizable _Cause_LegacyCodeCause_ShortDescription() { return new Localizable(holder, "Cause.LegacyCodeCause.ShortDescription", new Object[0]); }
  
  public static String ChoiceParameterDefinition_MissingChoices() { return holder.format("ChoiceParameterDefinition.MissingChoices", new Object[0]); }
  
  public static Localizable _ChoiceParameterDefinition_MissingChoices() { return new Localizable(holder, "ChoiceParameterDefinition.MissingChoices", new Object[0]); }
  
  public static String ResultTrend_Failure() { return holder.format("ResultTrend.Failure", new Object[0]); }
  
  public static Localizable _ResultTrend_Failure() { return new Localizable(holder, "ResultTrend.Failure", new Object[0]); }
  
  public static String Run_InProgressDuration(Object arg0) { return holder.format("Run.InProgressDuration", new Object[] { arg0 }); }
  
  public static Localizable _Run_InProgressDuration(Object arg0) { return new Localizable(holder, "Run.InProgressDuration", new Object[] { arg0 }); }
  
  public static String Queue_WaitingForNextAvailableExecutorOn(Object arg0) { return holder.format("Queue.WaitingForNextAvailableExecutorOn", new Object[] { arg0 }); }
  
  public static Localizable _Queue_WaitingForNextAvailableExecutorOn(Object arg0) { return new Localizable(holder, "Queue.WaitingForNextAvailableExecutorOn", new Object[] { arg0 }); }
  
  public static String LoadStatistics_Legends_OnlineExecutors() { return holder.format("LoadStatistics.Legends.OnlineExecutors", new Object[0]); }
  
  public static Localizable _LoadStatistics_Legends_OnlineExecutors() { return new Localizable(holder, "LoadStatistics.Legends.OnlineExecutors", new Object[0]); }
  
  public static String LoadStatistics_Legends_IdleExecutors() { return holder.format("LoadStatistics.Legends.IdleExecutors", new Object[0]); }
  
  public static Localizable _LoadStatistics_Legends_IdleExecutors() { return new Localizable(holder, "LoadStatistics.Legends.IdleExecutors", new Object[0]); }
  
  public static String AbstractItem_NoSuchJobExistsWithoutSuggestion(Object arg0) { return holder.format("AbstractItem.NoSuchJobExistsWithoutSuggestion", new Object[] { arg0 }); }
  
  public static Localizable _AbstractItem_NoSuchJobExistsWithoutSuggestion(Object arg0) { return new Localizable(holder, "AbstractItem.NoSuchJobExistsWithoutSuggestion", new Object[] { arg0 }); }
  
  public static String UpdateCenter_DownloadButNotActivated() { return holder.format("UpdateCenter.DownloadButNotActivated", new Object[0]); }
  
  public static Localizable _UpdateCenter_DownloadButNotActivated() { return new Localizable(holder, "UpdateCenter.DownloadButNotActivated", new Object[0]); }
  
  public static String MyViewsProperty_ViewExistsCheck_NotExist(Object arg0) { return holder.format("MyViewsProperty.ViewExistsCheck.NotExist", new Object[] { arg0 }); }
  
  public static Localizable _MyViewsProperty_ViewExistsCheck_NotExist(Object arg0) { return new Localizable(holder, "MyViewsProperty.ViewExistsCheck.NotExist", new Object[] { arg0 }); }
  
  public static String Permalink_LastUnstableBuild() { return holder.format("Permalink.LastUnstableBuild", new Object[0]); }
  
  public static Localizable _Permalink_LastUnstableBuild() { return new Localizable(holder, "Permalink.LastUnstableBuild", new Object[0]); }
  
  public static String Hudson_JobAlreadyExists(Object arg0) { return holder.format("Hudson.JobAlreadyExists", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_JobAlreadyExists(Object arg0) { return new Localizable(holder, "Hudson.JobAlreadyExists", new Object[] { arg0 }); }
  
  public static String Permalink_LastFailedBuild() { return holder.format("Permalink.LastFailedBuild", new Object[0]); }
  
  public static Localizable _Permalink_LastFailedBuild() { return new Localizable(holder, "Permalink.LastFailedBuild", new Object[0]); }
  
  public static String Queue_ExceptionCanRun() { return holder.format("Queue.ExceptionCanRun", new Object[0]); }
  
  public static Localizable _Queue_ExceptionCanRun() { return new Localizable(holder, "Queue.ExceptionCanRun", new Object[0]); }
  
  public static String Run_running_as_(Object arg0) { return holder.format("Run.running_as_", new Object[] { arg0 }); }
  
  public static Localizable _Run_running_as_(Object arg0) { return new Localizable(holder, "Run.running_as_", new Object[] { arg0 }); }
  
  public static String Slave_WindowsSlave() { return holder.format("Slave.WindowsSlave", new Object[0]); }
  
  public static Localizable _Slave_WindowsSlave() { return new Localizable(holder, "Slave.WindowsSlave", new Object[0]); }
  
  public static String Run_Summary_BrokenSinceThisBuild() { return holder.format("Run.Summary.BrokenSinceThisBuild", new Object[0]); }
  
  public static Localizable _Run_Summary_BrokenSinceThisBuild() { return new Localizable(holder, "Run.Summary.BrokenSinceThisBuild", new Object[0]); }
  
  public static String Queue_executor_slot_already_in_use() { return holder.format("Queue.executor_slot_already_in_use", new Object[0]); }
  
  public static Localizable _Queue_executor_slot_already_in_use() { return new Localizable(holder, "Queue.executor_slot_already_in_use", new Object[0]); }
  
  public static String Job_NoRecentBuildFailed() { return holder.format("Job.NoRecentBuildFailed", new Object[0]); }
  
  public static Localizable _Job_NoRecentBuildFailed() { return new Localizable(holder, "Job.NoRecentBuildFailed", new Object[0]); }
  
  public static String Descriptor_From(Object arg0, Object arg1) { return holder.format("Descriptor.From", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Descriptor_From(Object arg0, Object arg1) { return new Localizable(holder, "Descriptor.From", new Object[] { arg0, arg1 }); }
  
  public static String ParameterAction_DisplayName() { return holder.format("ParameterAction.DisplayName", new Object[0]); }
  
  public static Localizable _ParameterAction_DisplayName() { return new Localizable(holder, "ParameterAction.DisplayName", new Object[0]); }
  
  public static String ManagementLink_Category_CONFIGURATION() { return holder.format("ManagementLink.Category.CONFIGURATION", new Object[0]); }
  
  public static Localizable _ManagementLink_Category_CONFIGURATION() { return new Localizable(holder, "ManagementLink.Category.CONFIGURATION", new Object[0]); }
  
  public static String Item_RENAME_description() { return holder.format("Item.RENAME.description", new Object[0]); }
  
  public static Localizable _Item_RENAME_description() { return new Localizable(holder, "Item.RENAME.description", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_parameter() { return holder.format("UpdateCenter.PluginCategory.parameter", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_parameter() { return new Localizable(holder, "UpdateCenter.PluginCategory.parameter", new Object[0]); }
  
  public static String AbstractProject_PollingABorted() { return holder.format("AbstractProject.PollingABorted", new Object[0]); }
  
  public static Localizable _AbstractProject_PollingABorted() { return new Localizable(holder, "AbstractProject.PollingABorted", new Object[0]); }
  
  public static String AbstractProject_ScmAborted() { return holder.format("AbstractProject.ScmAborted", new Object[0]); }
  
  public static Localizable _AbstractProject_ScmAborted() { return new Localizable(holder, "AbstractProject.ScmAborted", new Object[0]); }
  
  public static String Hudson_NoJavaInPath(Object arg0) { return holder.format("Hudson.NoJavaInPath", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_NoJavaInPath(Object arg0) { return new Localizable(holder, "Hudson.NoJavaInPath", new Object[] { arg0 }); }
  
  public static String Job_you_must_use_the_save_button_if_you_wish() { return holder.format("Job.you_must_use_the_save_button_if_you_wish", new Object[0]); }
  
  public static Localizable _Job_you_must_use_the_save_button_if_you_wish() { return new Localizable(holder, "Job.you_must_use_the_save_button_if_you_wish", new Object[0]); }
  
  public static String AbstractProject_UpstreamBuildInProgress(Object arg0) { return holder.format("AbstractProject.UpstreamBuildInProgress", new Object[] { arg0 }); }
  
  public static Localizable _AbstractProject_UpstreamBuildInProgress(Object arg0) { return new Localizable(holder, "AbstractProject.UpstreamBuildInProgress", new Object[] { arg0 }); }
  
  public static String AbstractItem_FailureToStopBuilds(Object arg0, Object arg1) { return holder.format("AbstractItem.FailureToStopBuilds", new Object[] { arg0, arg1 }); }
  
  public static Localizable _AbstractItem_FailureToStopBuilds(Object arg0, Object arg1) { return new Localizable(holder, "AbstractItem.FailureToStopBuilds", new Object[] { arg0, arg1 }); }
  
  public static String ProxyView_DisplayName() { return holder.format("ProxyView.DisplayName", new Object[0]); }
  
  public static Localizable _ProxyView_DisplayName() { return new Localizable(holder, "ProxyView.DisplayName", new Object[0]); }
  
  public static String Run_Summary_BackToNormal() { return holder.format("Run.Summary.BackToNormal", new Object[0]); }
  
  public static Localizable _Run_Summary_BackToNormal() { return new Localizable(holder, "Run.Summary.BackToNormal", new Object[0]); }
  
  public static String Permalink_LastUnsuccessfulBuild() { return holder.format("Permalink.LastUnsuccessfulBuild", new Object[0]); }
  
  public static Localizable _Permalink_LastUnsuccessfulBuild() { return new Localizable(holder, "Permalink.LastUnsuccessfulBuild", new Object[0]); }
  
  public static String RunParameterDefinition_DisplayName() { return holder.format("RunParameterDefinition.DisplayName", new Object[0]); }
  
  public static Localizable _RunParameterDefinition_DisplayName() { return new Localizable(holder, "RunParameterDefinition.DisplayName", new Object[0]); }
  
  public static String Node_Mode_NORMAL() { return holder.format("Node.Mode.NORMAL", new Object[0]); }
  
  public static Localizable _Node_Mode_NORMAL() { return new Localizable(holder, "Node.Mode.NORMAL", new Object[0]); }
  
  public static String ParametersDefinitionProperty_BuildButtonText() { return holder.format("ParametersDefinitionProperty.BuildButtonText", new Object[0]); }
  
  public static Localizable _ParametersDefinitionProperty_BuildButtonText() { return new Localizable(holder, "ParametersDefinitionProperty.BuildButtonText", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_devops() { return holder.format("UpdateCenter.PluginCategory.devops", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_devops() { return new Localizable(holder, "UpdateCenter.PluginCategory.devops", new Object[0]); }
  
  public static String ManagementLink_Category_STATUS() { return holder.format("ManagementLink.Category.STATUS", new Object[0]); }
  
  public static Localizable _ManagementLink_Category_STATUS() { return new Localizable(holder, "ManagementLink.Category.STATUS", new Object[0]); }
  
  public static String TimeZoneProperty_DisplayDefaultTimeZone() { return holder.format("TimeZoneProperty.DisplayDefaultTimeZone", new Object[0]); }
  
  public static Localizable _TimeZoneProperty_DisplayDefaultTimeZone() { return new Localizable(holder, "TimeZoneProperty.DisplayDefaultTimeZone", new Object[0]); }
  
  public static String Job_BuildStability(Object arg0) { return holder.format("Job.BuildStability", new Object[] { arg0 }); }
  
  public static Localizable _Job_BuildStability(Object arg0) { return new Localizable(holder, "Job.BuildStability", new Object[] { arg0 }); }
  
  public static String Hudson_NotANonNegativeNumber() { return holder.format("Hudson.NotANonNegativeNumber", new Object[0]); }
  
  public static Localizable _Hudson_NotANonNegativeNumber() { return new Localizable(holder, "Hudson.NotANonNegativeNumber", new Object[0]); }
  
  public static String Queue_FinishedWaiting() { return holder.format("Queue.FinishedWaiting", new Object[0]); }
  
  public static Localizable _Queue_FinishedWaiting() { return new Localizable(holder, "Queue.FinishedWaiting", new Object[0]); }
  
  public static String Label_GroupOf(Object arg0) { return holder.format("Label.GroupOf", new Object[] { arg0 }); }
  
  public static Localizable _Label_GroupOf(Object arg0) { return new Localizable(holder, "Label.GroupOf", new Object[] { arg0 }); }
  
  public static String ChoiceParameterDefinition_DisplayName() { return holder.format("ChoiceParameterDefinition.DisplayName", new Object[0]); }
  
  public static Localizable _ChoiceParameterDefinition_DisplayName() { return new Localizable(holder, "ChoiceParameterDefinition.DisplayName", new Object[0]); }
  
  public static String Api_NoXPathMatch(Object arg0) { return holder.format("Api.NoXPathMatch", new Object[] { arg0 }); }
  
  public static Localizable _Api_NoXPathMatch(Object arg0) { return new Localizable(holder, "Api.NoXPathMatch", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_scala() { return holder.format("UpdateCenter.PluginCategory.scala", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_scala() { return new Localizable(holder, "UpdateCenter.PluginCategory.scala", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_ui() { return holder.format("UpdateCenter.PluginCategory.ui", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_ui() { return new Localizable(holder, "UpdateCenter.PluginCategory.ui", new Object[0]); }
  
  public static String AbstractProject_CancelPermission_Description() { return holder.format("AbstractProject.CancelPermission.Description", new Object[0]); }
  
  public static Localizable _AbstractProject_CancelPermission_Description() { return new Localizable(holder, "AbstractProject.CancelPermission.Description", new Object[0]); }
  
  public static String Hudson_Computer_Caption() { return holder.format("Hudson.Computer.Caption", new Object[0]); }
  
  public static Localizable _Hudson_Computer_Caption() { return new Localizable(holder, "Hudson.Computer.Caption", new Object[0]); }
  
  public static String Hudson_MustBeAtMost(Object arg0) { return holder.format("Hudson.MustBeAtMost", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_MustBeAtMost(Object arg0) { return new Localizable(holder, "Hudson.MustBeAtMost", new Object[] { arg0 }); }
  
  public static String MyViewsProperty_GlobalAction_DisplayName() { return holder.format("MyViewsProperty.GlobalAction.DisplayName", new Object[0]); }
  
  public static Localizable _MyViewsProperty_GlobalAction_DisplayName() { return new Localizable(holder, "MyViewsProperty.GlobalAction.DisplayName", new Object[0]); }
  
  public static String View_DisplayNameNotUniqueWarning(Object arg0) { return holder.format("View.DisplayNameNotUniqueWarning", new Object[] { arg0 }); }
  
  public static Localizable _View_DisplayNameNotUniqueWarning(Object arg0) { return new Localizable(holder, "View.DisplayNameNotUniqueWarning", new Object[] { arg0 }); }
  
  public static String ManagementLink_Category_TOOLS() { return holder.format("ManagementLink.Category.TOOLS", new Object[0]); }
  
  public static Localizable _ManagementLink_Category_TOOLS() { return new Localizable(holder, "ManagementLink.Category.TOOLS", new Object[0]); }
  
  public static String Computer_CreatePermission_Description() { return holder.format("Computer.CreatePermission.Description", new Object[0]); }
  
  public static Localizable _Computer_CreatePermission_Description() { return new Localizable(holder, "Computer.CreatePermission.Description", new Object[0]); }
  
  public static String BallColor_InProgress() { return holder.format("BallColor.InProgress", new Object[0]); }
  
  public static Localizable _BallColor_InProgress() { return new Localizable(holder, "BallColor.InProgress", new Object[0]); }
  
  public static String Item_CREATE_description() { return holder.format("Item.CREATE.description", new Object[0]); }
  
  public static Localizable _Item_CREATE_description() { return new Localizable(holder, "Item.CREATE.description", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_maven() { return holder.format("UpdateCenter.PluginCategory.maven", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_maven() { return new Localizable(holder, "UpdateCenter.PluginCategory.maven", new Object[0]); }
  
  public static String LabelExpression_NoMatch_DidYouMean(Object arg0, Object arg1) { return holder.format("LabelExpression.NoMatch_DidYouMean", new Object[] { arg0, arg1 }); }
  
  public static Localizable _LabelExpression_NoMatch_DidYouMean(Object arg0, Object arg1) { return new Localizable(holder, "LabelExpression.NoMatch_DidYouMean", new Object[] { arg0, arg1 }); }
  
  public static String UpdateCenter_PluginCategory_python() { return holder.format("UpdateCenter.PluginCategory.python", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_python() { return new Localizable(holder, "UpdateCenter.PluginCategory.python", new Object[0]); }
  
  public static String StringParameterDefinition_DisplayName() { return holder.format("StringParameterDefinition.DisplayName", new Object[0]); }
  
  public static Localizable _StringParameterDefinition_DisplayName() { return new Localizable(holder, "StringParameterDefinition.DisplayName", new Object[0]); }
  
  public static String Hudson_NodeBeingRemoved() { return holder.format("Hudson.NodeBeingRemoved", new Object[0]); }
  
  public static Localizable _Hudson_NodeBeingRemoved() { return new Localizable(holder, "Hudson.NodeBeingRemoved", new Object[0]); }
  
  public static String Run_Summary_Unstable() { return holder.format("Run.Summary.Unstable", new Object[0]); }
  
  public static Localizable _Run_Summary_Unstable() { return new Localizable(holder, "Run.Summary.Unstable", new Object[0]); }
  
  public static String FreeStyleProject_Description() { return holder.format("FreeStyleProject.Description", new Object[0]); }
  
  public static Localizable _FreeStyleProject_Description() { return new Localizable(holder, "FreeStyleProject.Description", new Object[0]); }
  
  public static String Slave_Remote_Relative_Path_Warning() { return holder.format("Slave.Remote.Relative.Path.Warning", new Object[0]); }
  
  public static Localizable _Slave_Remote_Relative_Path_Warning() { return new Localizable(holder, "Slave.Remote.Relative.Path.Warning", new Object[0]); }
  
  public static String Computer_ConfigurePermission_Description() { return holder.format("Computer.ConfigurePermission.Description", new Object[0]); }
  
  public static Localizable _Computer_ConfigurePermission_Description() { return new Localizable(holder, "Computer.ConfigurePermission.Description", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_groovy_related() { return holder.format("UpdateCenter.PluginCategory.groovy-related", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_groovy_related() { return new Localizable(holder, "UpdateCenter.PluginCategory.groovy-related", new Object[0]); }
  
  public static String ManagementLink_Category_UNCATEGORIZED() { return holder.format("ManagementLink.Category.UNCATEGORIZED", new Object[0]); }
  
  public static Localizable _ManagementLink_Category_UNCATEGORIZED() { return new Localizable(holder, "ManagementLink.Category.UNCATEGORIZED", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_external() { return holder.format("UpdateCenter.PluginCategory.external", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_external() { return new Localizable(holder, "UpdateCenter.PluginCategory.external", new Object[0]); }
  
  public static String AbstractProject_Pronoun() { return holder.format("AbstractProject.Pronoun", new Object[0]); }
  
  public static Localizable _AbstractProject_Pronoun() { return new Localizable(holder, "AbstractProject.Pronoun", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_orchestration() { return holder.format("UpdateCenter.PluginCategory.orchestration", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_orchestration() { return new Localizable(holder, "UpdateCenter.PluginCategory.orchestration", new Object[0]); }
  
  public static String BallColor_NotBuilt() { return holder.format("BallColor.NotBuilt", new Object[0]); }
  
  public static Localizable _BallColor_NotBuilt() { return new Localizable(holder, "BallColor.NotBuilt", new Object[0]); }
  
  public static String HealthReport_EmptyString() { return holder.format("HealthReport.EmptyString", new Object[0]); }
  
  public static Localizable _HealthReport_EmptyString() { return new Localizable(holder, "HealthReport.EmptyString", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_cli() { return holder.format("UpdateCenter.PluginCategory.cli", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_cli() { return new Localizable(holder, "UpdateCenter.PluginCategory.cli", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_dotnet() { return holder.format("UpdateCenter.PluginCategory.dotnet", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_dotnet() { return new Localizable(holder, "UpdateCenter.PluginCategory.dotnet", new Object[0]); }
  
  public static String Node_BecauseNodeIsNotAcceptingTasks(Object arg0) { return holder.format("Node.BecauseNodeIsNotAcceptingTasks", new Object[] { arg0 }); }
  
  public static Localizable _Node_BecauseNodeIsNotAcceptingTasks(Object arg0) { return new Localizable(holder, "Node.BecauseNodeIsNotAcceptingTasks", new Object[] { arg0 }); }
  
  public static String AbstractProject_NoSCM() { return holder.format("AbstractProject.NoSCM", new Object[0]); }
  
  public static Localizable _AbstractProject_NoSCM() { return new Localizable(holder, "AbstractProject.NoSCM", new Object[0]); }
  
  public static String TimeZoneProperty_DisplayName() { return holder.format("TimeZoneProperty.DisplayName", new Object[0]); }
  
  public static Localizable _TimeZoneProperty_DisplayName() { return new Localizable(holder, "TimeZoneProperty.DisplayName", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_cloud() { return holder.format("UpdateCenter.PluginCategory.cloud", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_cloud() { return new Localizable(holder, "UpdateCenter.PluginCategory.cloud", new Object[0]); }
  
  public static String Item_CONFIGURE_description() { return holder.format("Item.CONFIGURE.description", new Object[0]); }
  
  public static Localizable _Item_CONFIGURE_description() { return new Localizable(holder, "Item.CONFIGURE.description", new Object[0]); }
  
  public static String Jenkins_Manage_Description() { return holder.format("Jenkins.Manage.Description", new Object[0]); }
  
  public static Localizable _Jenkins_Manage_Description() { return new Localizable(holder, "Jenkins.Manage.Description", new Object[0]); }
  
  public static String Node_BecauseNodeIsReserved(Object arg0) { return holder.format("Node.BecauseNodeIsReserved", new Object[] { arg0 }); }
  
  public static Localizable _Node_BecauseNodeIsReserved(Object arg0) { return new Localizable(holder, "Node.BecauseNodeIsReserved", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_user() { return holder.format("UpdateCenter.PluginCategory.user", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_user() { return new Localizable(holder, "UpdateCenter.PluginCategory.user", new Object[0]); }
  
  public static String LoadStatistics_Legends_QueueLength() { return holder.format("LoadStatistics.Legends.QueueLength", new Object[0]); }
  
  public static Localizable _LoadStatistics_Legends_QueueLength() { return new Localizable(holder, "LoadStatistics.Legends.QueueLength", new Object[0]); }
  
  public static String Cause_RemoteCause_ShortDescription(Object arg0) { return holder.format("Cause.RemoteCause.ShortDescription", new Object[] { arg0 }); }
  
  public static Localizable _Cause_RemoteCause_ShortDescription(Object arg0) { return new Localizable(holder, "Cause.RemoteCause.ShortDescription", new Object[] { arg0 }); }
  
  public static String Hudson_NotANumber() { return holder.format("Hudson.NotANumber", new Object[0]); }
  
  public static Localizable _Hudson_NotANumber() { return new Localizable(holder, "Hudson.NotANumber", new Object[0]); }
  
  public static String View_Permissions_Title() { return holder.format("View.Permissions.Title", new Object[0]); }
  
  public static Localizable _View_Permissions_Title() { return new Localizable(holder, "View.Permissions.Title", new Object[0]); }
  
  public static String AbstractItem_TaskNoun() { return holder.format("AbstractItem.TaskNoun", new Object[0]); }
  
  public static Localizable _AbstractItem_TaskNoun() { return new Localizable(holder, "AbstractItem.TaskNoun", new Object[0]); }
  
  public static String AbstractProject_WorkspaceOffline() { return holder.format("AbstractProject.WorkspaceOffline", new Object[0]); }
  
  public static Localizable _AbstractProject_WorkspaceOffline() { return new Localizable(holder, "AbstractProject.WorkspaceOffline", new Object[0]); }
  
  public static String MyViewsProperty_ViewExistsCheck_AlreadyExists(Object arg0) { return holder.format("MyViewsProperty.ViewExistsCheck.AlreadyExists", new Object[] { arg0 }); }
  
  public static Localizable _MyViewsProperty_ViewExistsCheck_AlreadyExists(Object arg0) { return new Localizable(holder, "MyViewsProperty.ViewExistsCheck.AlreadyExists", new Object[] { arg0 }); }
  
  public static String View_ReadPermission_Description() { return holder.format("View.ReadPermission.Description", new Object[0]); }
  
  public static Localizable _View_ReadPermission_Description() { return new Localizable(holder, "View.ReadPermission.Description", new Object[0]); }
  
  public static String Queue_Unknown() { return holder.format("Queue.Unknown", new Object[0]); }
  
  public static Localizable _Queue_Unknown() { return new Localizable(holder, "Queue.Unknown", new Object[0]); }
  
  public static String Run_UpdatePermission_Description() { return holder.format("Run.UpdatePermission.Description", new Object[0]); }
  
  public static Localizable _Run_UpdatePermission_Description() { return new Localizable(holder, "Run.UpdatePermission.Description", new Object[0]); }
  
  public static String AbstractItem_NewNameInUse(Object arg0) { return holder.format("AbstractItem.NewNameInUse", new Object[] { arg0 }); }
  
  public static Localizable _AbstractItem_NewNameInUse(Object arg0) { return new Localizable(holder, "AbstractItem.NewNameInUse", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_deployment() { return holder.format("UpdateCenter.PluginCategory.deployment", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_deployment() { return new Localizable(holder, "UpdateCenter.PluginCategory.deployment", new Object[0]); }
  
  public static String CLI_clear_queue_shortDescription() { return holder.format("CLI.clear-queue.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_clear_queue_shortDescription() { return new Localizable(holder, "CLI.clear-queue.shortDescription", new Object[0]); }
  
  public static String AbstractProject_NewBuildForWorkspace() { return holder.format("AbstractProject.NewBuildForWorkspace", new Object[0]); }
  
  public static Localizable _AbstractProject_NewBuildForWorkspace() { return new Localizable(holder, "AbstractProject.NewBuildForWorkspace", new Object[0]); }
  
  public static String Slave_Remote_Director_Mandatory() { return holder.format("Slave.Remote.Director.Mandatory", new Object[0]); }
  
  public static Localizable _Slave_Remote_Director_Mandatory() { return new Localizable(holder, "Slave.Remote.Director.Mandatory", new Object[0]); }
  
  public static String Item_READ_description() { return holder.format("Item.READ.description", new Object[0]); }
  
  public static Localizable _Item_READ_description() { return new Localizable(holder, "Item.READ.description", new Object[0]); }
  
  public static String MultiStageTimeSeries_EMPTY_STRING() { return holder.format("MultiStageTimeSeries.EMPTY_STRING", new Object[0]); }
  
  public static Localizable _MultiStageTimeSeries_EMPTY_STRING() { return new Localizable(holder, "MultiStageTimeSeries.EMPTY_STRING", new Object[0]); }
  
  public static String Queue_BlockedBy(Object arg0) { return holder.format("Queue.BlockedBy", new Object[] { arg0 }); }
  
  public static Localizable _Queue_BlockedBy(Object arg0) { return new Localizable(holder, "Queue.BlockedBy", new Object[] { arg0 }); }
  
  public static String Queue_node_has_been_removed_from_configuration(Object arg0) { return holder.format("Queue.node_has_been_removed_from_configuration", new Object[] { arg0 }); }
  
  public static Localizable _Queue_node_has_been_removed_from_configuration(Object arg0) { return new Localizable(holder, "Queue.node_has_been_removed_from_configuration", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_android() { return holder.format("UpdateCenter.PluginCategory.android", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_android() { return new Localizable(holder, "UpdateCenter.PluginCategory.android", new Object[0]); }
  
  public static String Hudson_USER_CONTENT_README() { return holder.format("Hudson.USER_CONTENT_README", new Object[0]); }
  
  public static Localizable _Hudson_USER_CONTENT_README() { return new Localizable(holder, "Hudson.USER_CONTENT_README", new Object[0]); }
  
  public static String UpdateCenter_Status_CheckingJavaNet() { return holder.format("UpdateCenter.Status.CheckingJavaNet", new Object[0]); }
  
  public static Localizable _UpdateCenter_Status_CheckingJavaNet() { return new Localizable(holder, "UpdateCenter.Status.CheckingJavaNet", new Object[0]); }
  
  public static String AbstractProject_AwaitingWorkspaceToComeOnline(Object arg0) { return holder.format("AbstractProject.AwaitingWorkspaceToComeOnline", new Object[] { arg0 }); }
  
  public static Localizable _AbstractProject_AwaitingWorkspaceToComeOnline(Object arg0) { return new Localizable(holder, "AbstractProject.AwaitingWorkspaceToComeOnline", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_builder() { return holder.format("UpdateCenter.PluginCategory.builder", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_builder() { return new Localizable(holder, "UpdateCenter.PluginCategory.builder", new Object[0]); }
  
  public static String Api_MultipleMatch(Object arg0, Object arg1) { return holder.format("Api.MultipleMatch", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Api_MultipleMatch(Object arg0, Object arg1) { return new Localizable(holder, "Api.MultipleMatch", new Object[] { arg0, arg1 }); }
  
  public static String ResultTrend_Unstable() { return holder.format("ResultTrend.Unstable", new Object[0]); }
  
  public static Localizable _ResultTrend_Unstable() { return new Localizable(holder, "ResultTrend.Unstable", new Object[0]); }
  
  public static String TimeZoneProperty_current_time_in_(Object arg0, Object arg1) { return holder.format("TimeZoneProperty.current_time_in_", new Object[] { arg0, arg1 }); }
  
  public static Localizable _TimeZoneProperty_current_time_in_(Object arg0, Object arg1) { return new Localizable(holder, "TimeZoneProperty.current_time_in_", new Object[] { arg0, arg1 }); }
  
  public static String ComputerSet_SpecifySlaveToCopy() { return holder.format("ComputerSet.SpecifySlaveToCopy", new Object[0]); }
  
  public static Localizable _ComputerSet_SpecifySlaveToCopy() { return new Localizable(holder, "ComputerSet.SpecifySlaveToCopy", new Object[0]); }
  
  public static String Queue_ExceptionCanTake() { return holder.format("Queue.ExceptionCanTake", new Object[0]); }
  
  public static Localizable _Queue_ExceptionCanTake() { return new Localizable(holder, "Queue.ExceptionCanTake", new Object[0]); }
  
  public static String Hudson_NotUsesUTF8ToDecodeURL() { return holder.format("Hudson.NotUsesUTF8ToDecodeURL", new Object[0]); }
  
  public static Localizable _Hudson_NotUsesUTF8ToDecodeURL() { return new Localizable(holder, "Hudson.NotUsesUTF8ToDecodeURL", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_ruby() { return holder.format("UpdateCenter.PluginCategory.ruby", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_ruby() { return new Localizable(holder, "UpdateCenter.PluginCategory.ruby", new Object[0]); }
  
  public static String Computer_BadChannel() { return holder.format("Computer.BadChannel", new Object[0]); }
  
  public static Localizable _Computer_BadChannel() { return new Localizable(holder, "Computer.BadChannel", new Object[0]); }
  
  public static String AbstractProject_WorkspacePermission_Description() { return holder.format("AbstractProject.WorkspacePermission.Description", new Object[0]); }
  
  public static Localizable _AbstractProject_WorkspacePermission_Description() { return new Localizable(holder, "AbstractProject.WorkspacePermission.Description", new Object[0]); }
  
  public static String ResultTrend_StillUnstable() { return holder.format("ResultTrend.StillUnstable", new Object[0]); }
  
  public static Localizable _ResultTrend_StillUnstable() { return new Localizable(holder, "ResultTrend.StillUnstable", new Object[0]); }
  
  public static String FileParameterDefinition_DisplayName() { return holder.format("FileParameterDefinition.DisplayName", new Object[0]); }
  
  public static Localizable _FileParameterDefinition_DisplayName() { return new Localizable(holder, "FileParameterDefinition.DisplayName", new Object[0]); }
  
  public static String ListView_DisplayName() { return holder.format("ListView.DisplayName", new Object[0]); }
  
  public static Localizable _ListView_DisplayName() { return new Localizable(holder, "ListView.DisplayName", new Object[0]); }
  
  public static String Hudson_NoSuchDirectory(Object arg0) { return holder.format("Hudson.NoSuchDirectory", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_NoSuchDirectory(Object arg0) { return new Localizable(holder, "Hudson.NoSuchDirectory", new Object[] { arg0 }); }
  
  public static String Build_post_build_steps_failed() { return holder.format("Build.post_build_steps_failed", new Object[0]); }
  
  public static Localizable _Build_post_build_steps_failed() { return new Localizable(holder, "Build.post_build_steps_failed", new Object[0]); }
  
  public static String TextParameterDefinition_DisplayName() { return holder.format("TextParameterDefinition.DisplayName", new Object[0]); }
  
  public static Localizable _TextParameterDefinition_DisplayName() { return new Localizable(holder, "TextParameterDefinition.DisplayName", new Object[0]); }
  
  public static String TopLevelItemDescriptor_NotApplicableIn(Object arg0, Object arg1) { return holder.format("TopLevelItemDescriptor.NotApplicableIn", new Object[] { arg0, arg1 }); }
  
  public static Localizable _TopLevelItemDescriptor_NotApplicableIn(Object arg0, Object arg1) { return new Localizable(holder, "TopLevelItemDescriptor.NotApplicableIn", new Object[] { arg0, arg1 }); }
  
  public static String AbstractProject_ExtendedReadPermission_Description() { return holder.format("AbstractProject.ExtendedReadPermission.Description", new Object[0]); }
  
  public static Localizable _AbstractProject_ExtendedReadPermission_Description() { return new Localizable(holder, "AbstractProject.ExtendedReadPermission.Description", new Object[0]); }
  
  public static String Computer_ConnectPermission_Description() { return holder.format("Computer.ConnectPermission.Description", new Object[0]); }
  
  public static Localizable _Computer_ConnectPermission_Description() { return new Localizable(holder, "Computer.ConnectPermission.Description", new Object[0]); }
  
  public static String View_CreatePermission_Description() { return holder.format("View.CreatePermission.Description", new Object[0]); }
  
  public static Localizable _View_CreatePermission_Description() { return new Localizable(holder, "View.CreatePermission.Description", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_scm_related() { return holder.format("UpdateCenter.PluginCategory.scm-related", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_scm_related() { return new Localizable(holder, "UpdateCenter.PluginCategory.scm-related", new Object[0]); }
  
  public static String Queue_init() { return holder.format("Queue.init", new Object[0]); }
  
  public static Localizable _Queue_init() { return new Localizable(holder, "Queue.init", new Object[0]); }
  
  public static String Hudson_ReadPermission_Description() { return holder.format("Hudson.ReadPermission.Description", new Object[0]); }
  
  public static Localizable _Hudson_ReadPermission_Description() { return new Localizable(holder, "Hudson.ReadPermission.Description", new Object[0]); }
  
  public static String Api_WrapperParamInvalid() { return holder.format("Api.WrapperParamInvalid", new Object[0]); }
  
  public static Localizable _Api_WrapperParamInvalid() { return new Localizable(holder, "Api.WrapperParamInvalid", new Object[0]); }
  
  public static String TimeZoneProperty_current_time_on_server_in_in_proposed_di(Object arg0, Object arg1, Object arg2) { return holder.format("TimeZoneProperty.current_time_on_server_in_in_proposed_di", new Object[] { arg0, arg1, arg2 }); }
  
  public static Localizable _TimeZoneProperty_current_time_on_server_in_in_proposed_di(Object arg0, Object arg1, Object arg2) { return new Localizable(holder, "TimeZoneProperty.current_time_on_server_in_in_proposed_di", new Object[] { arg0, arg1, arg2 }); }
  
  public static String Computer_BuildPermission_Description() { return holder.format("Computer.BuildPermission.Description", new Object[0]); }
  
  public static Localizable _Computer_BuildPermission_Description() { return new Localizable(holder, "Computer.BuildPermission.Description", new Object[0]); }
  
  public static String ManagementLink_Category_MISC() { return holder.format("ManagementLink.Category.MISC", new Object[0]); }
  
  public static Localizable _ManagementLink_Category_MISC() { return new Localizable(holder, "ManagementLink.Category.MISC", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_unrecognized(Object arg0) { return holder.format("UpdateCenter.PluginCategory.unrecognized", new Object[] { arg0 }); }
  
  public static Localizable _UpdateCenter_PluginCategory_unrecognized(Object arg0) { return new Localizable(holder, "UpdateCenter.PluginCategory.unrecognized", new Object[] { arg0 }); }
  
  public static String AbstractBuild_BuildingOnMaster() { return holder.format("AbstractBuild.BuildingOnMaster", new Object[0]); }
  
  public static Localizable _AbstractBuild_BuildingOnMaster() { return new Localizable(holder, "AbstractBuild.BuildingOnMaster", new Object[0]); }
  
  public static String Item_Permissions_Title() { return holder.format("Item.Permissions.Title", new Object[0]); }
  
  public static Localizable _Item_Permissions_Title() { return new Localizable(holder, "Item.Permissions.Title", new Object[0]); }
  
  public static String UpdateCenter_Status_ConnectionFailed(Object arg0, Object arg1) { return holder.format("UpdateCenter.Status.ConnectionFailed", new Object[] { arg0, arg1 }); }
  
  public static Localizable _UpdateCenter_Status_ConnectionFailed(Object arg0, Object arg1) { return new Localizable(holder, "UpdateCenter.Status.ConnectionFailed", new Object[] { arg0, arg1 }); }
  
  public static String ComputerSet_DisplayName() { return holder.format("ComputerSet.DisplayName", new Object[0]); }
  
  public static Localizable _ComputerSet_DisplayName() { return new Localizable(holder, "ComputerSet.DisplayName", new Object[0]); }
  
  public static String Hudson_MustBeAtLeast(Object arg0) { return holder.format("Hudson.MustBeAtLeast", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_MustBeAtLeast(Object arg0) { return new Localizable(holder, "Hudson.MustBeAtLeast", new Object[] { arg0 }); }
  
  public static String CLI_online_node_shortDescription() { return holder.format("CLI.online-node.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_online_node_shortDescription() { return new Localizable(holder, "CLI.online-node.shortDescription", new Object[0]); }
  
  public static String Hudson_NotJDKDir(Object arg0) { return holder.format("Hudson.NotJDKDir", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_NotJDKDir(Object arg0) { return new Localizable(holder, "Hudson.NotJDKDir", new Object[] { arg0 }); }
  
  public static String AbstractProject_BuildPermission_Description() { return holder.format("AbstractProject.BuildPermission.Description", new Object[0]); }
  
  public static Localizable _AbstractProject_BuildPermission_Description() { return new Localizable(holder, "AbstractProject.BuildPermission.Description", new Object[0]); }
  
  public static String ResultTrend_Aborted() { return holder.format("ResultTrend.Aborted", new Object[0]); }
  
  public static Localizable _ResultTrend_Aborted() { return new Localizable(holder, "ResultTrend.Aborted", new Object[0]); }
  
  public static String UpdateCenter_DisplayName() { return holder.format("UpdateCenter.DisplayName", new Object[0]); }
  
  public static Localizable _UpdateCenter_DisplayName() { return new Localizable(holder, "UpdateCenter.DisplayName", new Object[0]); }
  
  public static String MyView_DisplayName() { return holder.format("MyView.DisplayName", new Object[0]); }
  
  public static Localizable _MyView_DisplayName() { return new Localizable(holder, "MyView.DisplayName", new Object[0]); }
  
  public static String UpdateCenter_Status_Success() { return holder.format("UpdateCenter.Status.Success", new Object[0]); }
  
  public static Localizable _UpdateCenter_Status_Success() { return new Localizable(holder, "UpdateCenter.Status.Success", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_cluster() { return holder.format("UpdateCenter.PluginCategory.cluster", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_cluster() { return new Localizable(holder, "UpdateCenter.PluginCategory.cluster", new Object[0]); }
  
  public static String LoadStatistics_Legends_ConnectingExecutors() { return holder.format("LoadStatistics.Legends.ConnectingExecutors", new Object[0]); }
  
  public static Localizable _LoadStatistics_Legends_ConnectingExecutors() { return new Localizable(holder, "LoadStatistics.Legends.ConnectingExecutors", new Object[0]); }
  
  public static String Jenkins_SystemRead_Description() { return holder.format("Jenkins.SystemRead.Description", new Object[0]); }
  
  public static Localizable _Jenkins_SystemRead_Description() { return new Localizable(holder, "Jenkins.SystemRead.Description", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_theme() { return holder.format("UpdateCenter.PluginCategory.theme", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_theme() { return new Localizable(holder, "UpdateCenter.PluginCategory.theme", new Object[0]); }
  
  public static String Run_Summary_BrokenSince(Object arg0) { return holder.format("Run.Summary.BrokenSince", new Object[] { arg0 }); }
  
  public static Localizable _Run_Summary_BrokenSince(Object arg0) { return new Localizable(holder, "Run.Summary.BrokenSince", new Object[] { arg0 }); }
  
  public static String AbstractProject_DownstreamBuildInProgress(Object arg0) { return holder.format("AbstractProject.DownstreamBuildInProgress", new Object[] { arg0 }); }
  
  public static Localizable _AbstractProject_DownstreamBuildInProgress(Object arg0) { return new Localizable(holder, "AbstractProject.DownstreamBuildInProgress", new Object[] { arg0 }); }
  
  public static String View_MissingMode() { return holder.format("View.MissingMode", new Object[0]); }
  
  public static Localizable _View_MissingMode() { return new Localizable(holder, "View.MissingMode", new Object[0]); }
  
  public static String Hudson_TrailingDot() { return holder.format("Hudson.TrailingDot", new Object[0]); }
  
  public static Localizable _Hudson_TrailingDot() { return new Localizable(holder, "Hudson.TrailingDot", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_must_be_labeled() { return holder.format("UpdateCenter.PluginCategory.must-be-labeled", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_must_be_labeled() { return new Localizable(holder, "UpdateCenter.PluginCategory.must-be-labeled", new Object[0]); }
  
  public static String ResultTrend_Success() { return holder.format("ResultTrend.Success", new Object[0]); }
  
  public static Localizable _ResultTrend_Success() { return new Localizable(holder, "ResultTrend.Success", new Object[0]); }
  
  public static String Hudson_NoName() { return holder.format("Hudson.NoName", new Object[0]); }
  
  public static Localizable _Hudson_NoName() { return new Localizable(holder, "Hudson.NoName", new Object[0]); }
  
  public static String ParametersDefinitionProperty_DisplayName() { return holder.format("ParametersDefinitionProperty.DisplayName", new Object[0]); }
  
  public static Localizable _ParametersDefinitionProperty_DisplayName() { return new Localizable(holder, "ParametersDefinitionProperty.DisplayName", new Object[0]); }
  
  public static String AbstractItem_NewNameUnchanged() { return holder.format("AbstractItem.NewNameUnchanged", new Object[0]); }
  
  public static Localizable _AbstractItem_NewNameUnchanged() { return new Localizable(holder, "AbstractItem.NewNameUnchanged", new Object[0]); }
  
  public static String CLI_keep_build_shortDescription() { return holder.format("CLI.keep-build.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_keep_build_shortDescription() { return new Localizable(holder, "CLI.keep-build.shortDescription", new Object[0]); }
  
  public static String BallColor_Failed() { return holder.format("BallColor.Failed", new Object[0]); }
  
  public static Localizable _BallColor_Failed() { return new Localizable(holder, "BallColor.Failed", new Object[0]); }
  
  public static String Queue_InQuietPeriod(Object arg0) { return holder.format("Queue.InQuietPeriod", new Object[] { arg0 }); }
  
  public static Localizable _Queue_InQuietPeriod(Object arg0) { return new Localizable(holder, "Queue.InQuietPeriod", new Object[] { arg0 }); }
  
  public static String ManagementLink_Category_SECURITY() { return holder.format("ManagementLink.Category.SECURITY", new Object[0]); }
  
  public static Localizable _ManagementLink_Category_SECURITY() { return new Localizable(holder, "ManagementLink.Category.SECURITY", new Object[0]); }
  
  public static String AbstractProject_PollingVetoed(Object arg0) { return holder.format("AbstractProject.PollingVetoed", new Object[] { arg0 }); }
  
  public static Localizable _AbstractProject_PollingVetoed(Object arg0) { return new Localizable(holder, "AbstractProject.PollingVetoed", new Object[] { arg0 }); }
  
  public static String Slave_UnixSlave() { return holder.format("Slave.UnixSlave", new Object[0]); }
  
  public static Localizable _Slave_UnixSlave() { return new Localizable(holder, "Slave.UnixSlave", new Object[0]); }
  
  public static String ItemGroupMixIn_may_not_copy_as_it_contains_secrets_and_(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) { return holder.format("ItemGroupMixIn.may_not_copy_as_it_contains_secrets_and_", new Object[] { arg0, arg1, arg2, arg3, arg4 }); }
  
  public static Localizable _ItemGroupMixIn_may_not_copy_as_it_contains_secrets_and_(Object arg0, Object arg1, Object arg2, Object arg3, Object arg4) { return new Localizable(holder, "ItemGroupMixIn.may_not_copy_as_it_contains_secrets_and_", new Object[] { arg0, arg1, arg2, arg3, arg4 }); }
  
  public static String UpdateCenter_PluginCategory_notifier() { return holder.format("UpdateCenter.PluginCategory.notifier", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_notifier() { return new Localizable(holder, "UpdateCenter.PluginCategory.notifier", new Object[0]); }
  
  public static String ComputerSet_SlaveAlreadyExists(Object arg0) { return holder.format("ComputerSet.SlaveAlreadyExists", new Object[] { arg0 }); }
  
  public static Localizable _ComputerSet_SlaveAlreadyExists(Object arg0) { return new Localizable(holder, "ComputerSet.SlaveAlreadyExists", new Object[] { arg0 }); }
  
  public static String UpdateCenter_PluginCategory_agent() { return holder.format("UpdateCenter.PluginCategory.agent", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_agent() { return new Localizable(holder, "UpdateCenter.PluginCategory.agent", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_runcondition() { return holder.format("UpdateCenter.PluginCategory.runcondition", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_runcondition() { return new Localizable(holder, "UpdateCenter.PluginCategory.runcondition", new Object[0]); }
  
  public static String BallColor_Aborted() { return holder.format("BallColor.Aborted", new Object[0]); }
  
  public static Localizable _BallColor_Aborted() { return new Localizable(holder, "BallColor.Aborted", new Object[0]); }
  
  public static String Hudson_ControlCodeNotAllowed(Object arg0) { return holder.format("Hudson.ControlCodeNotAllowed", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_ControlCodeNotAllowed(Object arg0) { return new Localizable(holder, "Hudson.ControlCodeNotAllowed", new Object[] { arg0 }); }
  
  public static String ResultTrend_NowUnstable() { return holder.format("ResultTrend.NowUnstable", new Object[0]); }
  
  public static Localizable _ResultTrend_NowUnstable() { return new Localizable(holder, "ResultTrend.NowUnstable", new Object[0]); }
  
  public static String Slave_InvalidConfig_NoName() { return holder.format("Slave.InvalidConfig.NoName", new Object[0]); }
  
  public static Localizable _Slave_InvalidConfig_NoName() { return new Localizable(holder, "Slave.InvalidConfig.NoName", new Object[0]); }
  
  public static String ManagementLink_Category_TROUBLESHOOTING() { return holder.format("ManagementLink.Category.TROUBLESHOOTING", new Object[0]); }
  
  public static Localizable _ManagementLink_Category_TROUBLESHOOTING() { return new Localizable(holder, "ManagementLink.Category.TROUBLESHOOTING", new Object[0]); }
  
  public static String LabelExpression_ObsoleteMasterLabel() { return holder.format("LabelExpression_ObsoleteMasterLabel", new Object[0]); }
  
  public static Localizable _LabelExpression_ObsoleteMasterLabel() { return new Localizable(holder, "LabelExpression_ObsoleteMasterLabel", new Object[0]); }
  
  public static String Job_minutes() { return holder.format("Job.minutes", new Object[0]); }
  
  public static Localizable _Job_minutes() { return new Localizable(holder, "Job.minutes", new Object[0]); }
  
  public static String ResultTrend_StillFailing() { return holder.format("ResultTrend.StillFailing", new Object[0]); }
  
  public static Localizable _ResultTrend_StillFailing() { return new Localizable(holder, "ResultTrend.StillFailing", new Object[0]); }
  
  public static String Slave_Terminated(Object arg0) { return holder.format("Slave.Terminated", new Object[] { arg0 }); }
  
  public static Localizable _Slave_Terminated(Object arg0) { return new Localizable(holder, "Slave.Terminated", new Object[] { arg0 }); }
  
  public static String AbstractProject_DiscoverPermission_Description() { return holder.format("AbstractProject.DiscoverPermission.Description", new Object[0]); }
  
  public static Localizable _AbstractProject_DiscoverPermission_Description() { return new Localizable(holder, "AbstractProject.DiscoverPermission.Description", new Object[0]); }
  
  public static String User_IllegalUsername(Object arg0) { return holder.format("User.IllegalUsername", new Object[] { arg0 }); }
  
  public static Localizable _User_IllegalUsername(Object arg0) { return new Localizable(holder, "User.IllegalUsername", new Object[] { arg0 }); }
  
  public static String Hudson_Permissions_Title() { return holder.format("Hudson.Permissions.Title", new Object[0]); }
  
  public static Localizable _Hudson_Permissions_Title() { return new Localizable(holder, "Hudson.Permissions.Title", new Object[0]); }
  
  public static String Cause_UserCause_ShortDescription(Object arg0) { return holder.format("Cause.UserCause.ShortDescription", new Object[] { arg0 }); }
  
  public static Localizable _Cause_UserCause_ShortDescription(Object arg0) { return new Localizable(holder, "Cause.UserCause.ShortDescription", new Object[] { arg0 }); }
  
  public static String UpdateCenter_init() { return holder.format("UpdateCenter.init", new Object[0]); }
  
  public static Localizable _UpdateCenter_init() { return new Localizable(holder, "UpdateCenter.init", new Object[0]); }
  
  public static String AbstractProject_Aborted() { return holder.format("AbstractProject.Aborted", new Object[0]); }
  
  public static Localizable _AbstractProject_Aborted() { return new Localizable(holder, "AbstractProject.Aborted", new Object[0]); }
  
  public static String AbstractProject_Disabled() { return holder.format("AbstractProject.Disabled", new Object[0]); }
  
  public static Localizable _AbstractProject_Disabled() { return new Localizable(holder, "AbstractProject.Disabled", new Object[0]); }
  
  public static String Computer_NoSuchSlaveExistsWithoutAdvice(Object arg0) { return holder.format("Computer.NoSuchSlaveExistsWithoutAdvice", new Object[] { arg0 }); }
  
  public static Localizable _Computer_NoSuchSlaveExistsWithoutAdvice(Object arg0) { return new Localizable(holder, "Computer.NoSuchSlaveExistsWithoutAdvice", new Object[] { arg0 }); }
  
  public static String Job_Pronoun() { return holder.format("Job.Pronoun", new Object[0]); }
  
  public static Localizable _Job_Pronoun() { return new Localizable(holder, "Job.Pronoun", new Object[0]); }
  
  public static String UpdateCenter_Status_CheckingInternet() { return holder.format("UpdateCenter.Status.CheckingInternet", new Object[0]); }
  
  public static Localizable _UpdateCenter_Status_CheckingInternet() { return new Localizable(holder, "UpdateCenter.Status.CheckingInternet", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_upload() { return holder.format("UpdateCenter.PluginCategory.upload", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_upload() { return new Localizable(holder, "UpdateCenter.PluginCategory.upload", new Object[0]); }
  
  public static String Jenkins_CheckDisplayName_DisplayNameNotUniqueWarning(Object arg0) { return holder.format("Jenkins.CheckDisplayName.DisplayNameNotUniqueWarning", new Object[] { arg0 }); }
  
  public static Localizable _Jenkins_CheckDisplayName_DisplayNameNotUniqueWarning(Object arg0) { return new Localizable(holder, "Jenkins.CheckDisplayName.DisplayNameNotUniqueWarning", new Object[] { arg0 }); }
  
  public static String AbstractBuild_BuildingRemotely(Object arg0) { return holder.format("AbstractBuild.BuildingRemotely", new Object[] { arg0 }); }
  
  public static Localizable _AbstractBuild_BuildingRemotely(Object arg0) { return new Localizable(holder, "AbstractBuild.BuildingRemotely", new Object[] { arg0 }); }
  
  public static String Computer_DeletePermission_Description() { return holder.format("Computer.DeletePermission.Description", new Object[0]); }
  
  public static Localizable _Computer_DeletePermission_Description() { return new Localizable(holder, "Computer.DeletePermission.Description", new Object[0]); }
  
  public static String ResultTrend_NotBuilt() { return holder.format("ResultTrend.NotBuilt", new Object[0]); }
  
  public static Localizable _ResultTrend_NotBuilt() { return new Localizable(holder, "ResultTrend.NotBuilt", new Object[0]); }
  
  public static String AbstractItem_NoSuchJobExists(Object arg0, Object arg1) { return holder.format("AbstractItem.NoSuchJobExists", new Object[] { arg0, arg1 }); }
  
  public static Localizable _AbstractItem_NoSuchJobExists(Object arg0, Object arg1) { return new Localizable(holder, "AbstractItem.NoSuchJobExists", new Object[] { arg0, arg1 }); }
  
  public static String Permalink_LastCompletedBuild() { return holder.format("Permalink.LastCompletedBuild", new Object[0]); }
  
  public static Localizable _Permalink_LastCompletedBuild() { return new Localizable(holder, "Permalink.LastCompletedBuild", new Object[0]); }
  
  public static String Permalink_LastSuccessfulBuild() { return holder.format("Permalink.LastSuccessfulBuild", new Object[0]); }
  
  public static Localizable _Permalink_LastSuccessfulBuild() { return new Localizable(holder, "Permalink.LastSuccessfulBuild", new Object[0]); }
  
  public static String AbstractBuild_Building() { return holder.format("AbstractBuild_Building", new Object[0]); }
  
  public static Localizable _AbstractBuild_Building() { return new Localizable(holder, "AbstractBuild_Building", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_report() { return holder.format("UpdateCenter.PluginCategory.report", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_report() { return new Localizable(holder, "UpdateCenter.PluginCategory.report", new Object[0]); }
  
  public static String LoadStatistics_Legends_BusyExecutors() { return holder.format("LoadStatistics.Legends.BusyExecutors", new Object[0]); }
  
  public static Localizable _LoadStatistics_Legends_BusyExecutors() { return new Localizable(holder, "LoadStatistics.Legends.BusyExecutors", new Object[0]); }
  
  public static String AbstractProject_WorkspaceTitle(Object arg0) { return holder.format("AbstractProject.WorkspaceTitle", new Object[] { arg0 }); }
  
  public static Localizable _AbstractProject_WorkspaceTitle(Object arg0) { return new Localizable(holder, "AbstractProject.WorkspaceTitle", new Object[] { arg0 }); }
  
  public static String LabelExpression_NoMatch() { return holder.format("LabelExpression.NoMatch", new Object[0]); }
  
  public static Localizable _LabelExpression_NoMatch() { return new Localizable(holder, "LabelExpression.NoMatch", new Object[0]); }
  
  public static String BallColor_Disabled() { return holder.format("BallColor.Disabled", new Object[0]); }
  
  public static Localizable _BallColor_Disabled() { return new Localizable(holder, "BallColor.Disabled", new Object[0]); }
  
  public static String BallColor_Success() { return holder.format("BallColor.Success", new Object[0]); }
  
  public static Localizable _BallColor_Success() { return new Localizable(holder, "BallColor.Success", new Object[0]); }
  
  public static String Run_Permissions_Title() { return holder.format("Run.Permissions.Title", new Object[0]); }
  
  public static Localizable _Run_Permissions_Title() { return new Localizable(holder, "Run.Permissions.Title", new Object[0]); }
  
  public static String AbstractProject_NoBuilds() { return holder.format("AbstractProject.NoBuilds", new Object[0]); }
  
  public static Localizable _AbstractProject_NoBuilds() { return new Localizable(holder, "AbstractProject.NoBuilds", new Object[0]); }
  
  public static String Cause_UserIdCause_ShortDescription(Object arg0) { return holder.format("Cause.UserIdCause.ShortDescription", new Object[] { arg0 }); }
  
  public static Localizable _Cause_UserIdCause_ShortDescription(Object arg0) { return new Localizable(holder, "Cause.UserIdCause.ShortDescription", new Object[] { arg0 }); }
  
  public static String BallColor_Unstable() { return holder.format("BallColor.Unstable", new Object[0]); }
  
  public static Localizable _BallColor_Unstable() { return new Localizable(holder, "BallColor.Unstable", new Object[0]); }
  
  public static String Computer_Permissions_Title() { return holder.format("Computer.Permissions.Title", new Object[0]); }
  
  public static Localizable _Computer_Permissions_Title() { return new Localizable(holder, "Computer.Permissions.Title", new Object[0]); }
  
  public static String View_DeletePermission_Description() { return holder.format("View.DeletePermission.Description", new Object[0]); }
  
  public static Localizable _View_DeletePermission_Description() { return new Localizable(holder, "View.DeletePermission.Description", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_api_plugin() { return holder.format("UpdateCenter.PluginCategory.api-plugin", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_api_plugin() { return new Localizable(holder, "UpdateCenter.PluginCategory.api-plugin", new Object[0]); }
  
  public static String Run_DeletePermission_Description() { return holder.format("Run.DeletePermission.Description", new Object[0]); }
  
  public static Localizable _Run_DeletePermission_Description() { return new Localizable(holder, "Run.DeletePermission.Description", new Object[0]); }
  
  public static String AbstractProject_AwaitingBuildForWorkspace() { return holder.format("AbstractProject.AwaitingBuildForWorkspace", new Object[0]); }
  
  public static Localizable _AbstractProject_AwaitingBuildForWorkspace() { return new Localizable(holder, "AbstractProject.AwaitingBuildForWorkspace", new Object[0]); }
  
  public static String Hudson_NotANegativeNumber() { return holder.format("Hudson.NotANegativeNumber", new Object[0]); }
  
  public static Localizable _Hudson_NotANegativeNumber() { return new Localizable(holder, "Hudson.NotANegativeNumber", new Object[0]); }
  
  public static String AbstractProject_NoWorkspace() { return holder.format("AbstractProject.NoWorkspace", new Object[0]); }
  
  public static Localizable _AbstractProject_NoWorkspace() { return new Localizable(holder, "AbstractProject.NoWorkspace", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_ios() { return holder.format("UpdateCenter.PluginCategory.ios", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_ios() { return new Localizable(holder, "UpdateCenter.PluginCategory.ios", new Object[0]); }
  
  public static String Job_AllRecentBuildFailed() { return holder.format("Job.AllRecentBuildFailed", new Object[0]); }
  
  public static Localizable _Job_AllRecentBuildFailed() { return new Localizable(holder, "Job.AllRecentBuildFailed", new Object[0]); }
  
  public static String ResultTrend_Fixed() { return holder.format("ResultTrend.Fixed", new Object[0]); }
  
  public static Localizable _ResultTrend_Fixed() { return new Localizable(holder, "ResultTrend.Fixed", new Object[0]); }
  
  public static String AbstractProject_WipeOutPermission_Description() { return holder.format("AbstractProject.WipeOutPermission.Description", new Object[0]); }
  
  public static Localizable _AbstractProject_WipeOutPermission_Description() { return new Localizable(holder, "AbstractProject.WipeOutPermission.Description", new Object[0]); }
  
  public static String Computer_Caption(Object arg0) { return holder.format("Computer.Caption", new Object[] { arg0 }); }
  
  public static Localizable _Computer_Caption(Object arg0) { return new Localizable(holder, "Computer.Caption", new Object[] { arg0 }); }
  
  public static String AbstractProject_CustomWorkspaceEmpty() { return holder.format("AbstractProject.CustomWorkspaceEmpty", new Object[0]); }
  
  public static Localizable _AbstractProject_CustomWorkspaceEmpty() { return new Localizable(holder, "AbstractProject.CustomWorkspaceEmpty", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_database() { return holder.format("UpdateCenter.PluginCategory.database", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_database() { return new Localizable(holder, "UpdateCenter.PluginCategory.database", new Object[0]); }
  
  public static String UpdateCenter_Status_UnknownHostException(Object arg0, Object arg1) { return holder.format("UpdateCenter.Status.UnknownHostException", new Object[] { arg0, arg1 }); }
  
  public static Localizable _UpdateCenter_Status_UnknownHostException(Object arg0, Object arg1) { return new Localizable(holder, "UpdateCenter.Status.UnknownHostException", new Object[] { arg0, arg1 }); }
  
  public static String Jenkins_IsRestarting() { return holder.format("Jenkins.IsRestarting", new Object[0]); }
  
  public static Localizable _Jenkins_IsRestarting() { return new Localizable(holder, "Jenkins.IsRestarting", new Object[0]); }
  
  public static String Queue_HudsonIsAboutToShutDown() { return holder.format("Queue.HudsonIsAboutToShutDown", new Object[0]); }
  
  public static Localizable _Queue_HudsonIsAboutToShutDown() { return new Localizable(holder, "Queue.HudsonIsAboutToShutDown", new Object[0]); }
  
  public static String Slave_InvalidConfig_Executors(Object arg0) { return holder.format("Slave.InvalidConfig.Executors", new Object[] { arg0 }); }
  
  public static Localizable _Slave_InvalidConfig_Executors(Object arg0) { return new Localizable(holder, "Slave.InvalidConfig.Executors", new Object[] { arg0 }); }
  
  public static String Slave_Network_Mounted_File_System_Warning() { return holder.format("Slave.Network.Mounted.File.System.Warning", new Object[0]); }
  
  public static Localizable _Slave_Network_Mounted_File_System_Warning() { return new Localizable(holder, "Slave.Network.Mounted.File.System.Warning", new Object[0]); }
  
  public static String AbstractProject_WorkspaceTitleOnComputer(Object arg0, Object arg1) { return holder.format("AbstractProject.WorkspaceTitleOnComputer", new Object[] { arg0, arg1 }); }
  
  public static Localizable _AbstractProject_WorkspaceTitleOnComputer(Object arg0, Object arg1) { return new Localizable(holder, "AbstractProject.WorkspaceTitleOnComputer", new Object[] { arg0, arg1 }); }
  
  public static String Hudson_BadPortNumber(Object arg0) { return holder.format("Hudson.BadPortNumber", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_BadPortNumber(Object arg0) { return new Localizable(holder, "Hudson.BadPortNumber", new Object[] { arg0 }); }
  
  public static String Cause_UpstreamCause_CausedBy() { return holder.format("Cause.UpstreamCause.CausedBy", new Object[0]); }
  
  public static Localizable _Cause_UpstreamCause_CausedBy() { return new Localizable(holder, "Cause.UpstreamCause.CausedBy", new Object[0]); }
  
  public static String UpdateCenter_n_a() { return holder.format("UpdateCenter.n_a", new Object[0]); }
  
  public static Localizable _UpdateCenter_n_a() { return new Localizable(holder, "UpdateCenter.n_a", new Object[0]); }
  
  public static String Hudson_Computer_DisplayName() { return holder.format("Hudson.Computer.DisplayName", new Object[0]); }
  
  public static Localizable _Hudson_Computer_DisplayName() { return new Localizable(holder, "Hudson.Computer.DisplayName", new Object[0]); }
  
  public static String UpdateCenter_PluginCategory_scm() { return holder.format("UpdateCenter.PluginCategory.scm", new Object[0]); }
  
  public static Localizable _UpdateCenter_PluginCategory_scm() { return new Localizable(holder, "UpdateCenter.PluginCategory.scm", new Object[0]); }
  
  public static String Hudson_AdministerPermission_Description() { return holder.format("Hudson.AdministerPermission.Description", new Object[0]); }
  
  public static Localizable _Hudson_AdministerPermission_Description() { return new Localizable(holder, "Hudson.AdministerPermission.Description", new Object[0]); }
  
  public static String Run_Summary_BrokenForALongTime() { return holder.format("Run.Summary.BrokenForALongTime", new Object[0]); }
  
  public static Localizable _Run_Summary_BrokenForALongTime() { return new Localizable(holder, "Run.Summary.BrokenForALongTime", new Object[0]); }
  
  public static String Run_Summary_Unknown() { return holder.format("Run.Summary.Unknown", new Object[0]); }
  
  public static Localizable _Run_Summary_Unknown() { return new Localizable(holder, "Run.Summary.Unknown", new Object[0]); }
  
  public static String Job_NoRenameWhileBuilding() { return holder.format("Job.NoRenameWhileBuilding", new Object[0]); }
  
  public static Localizable _Job_NoRenameWhileBuilding() { return new Localizable(holder, "Job.NoRenameWhileBuilding", new Object[0]); }
  
  public static String Jenkins_CheckDisplayName_NameNotUniqueWarning(Object arg0) { return holder.format("Jenkins.CheckDisplayName.NameNotUniqueWarning", new Object[] { arg0 }); }
  
  public static Localizable _Jenkins_CheckDisplayName_NameNotUniqueWarning(Object arg0) { return new Localizable(holder, "Jenkins.CheckDisplayName.NameNotUniqueWarning", new Object[] { arg0 }); }
}
