package jenkins.model;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String Hudson_ViewAlreadyExists(Object arg0) { return holder.format("Hudson.ViewAlreadyExists", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_ViewAlreadyExists(Object arg0) { return new Localizable(holder, "Hudson.ViewAlreadyExists", new Object[] { arg0 }); }
  
  public static String BuiltInNodeMigration_DisplayName() { return holder.format("BuiltInNodeMigration.DisplayName", new Object[0]); }
  
  public static Localizable _BuiltInNodeMigration_DisplayName() { return new Localizable(holder, "BuiltInNodeMigration.DisplayName", new Object[0]); }
  
  public static String Hudson_NodeDescription() { return holder.format("Hudson.NodeDescription", new Object[0]); }
  
  public static Localizable _Hudson_NodeDescription() { return new Localizable(holder, "Hudson.NodeDescription", new Object[0]); }
  
  public static String IdStrategy_CaseSensitiveEmailAddress_DisplayName() { return holder.format("IdStrategy.CaseSensitiveEmailAddress.DisplayName", new Object[0]); }
  
  public static Localizable _IdStrategy_CaseSensitiveEmailAddress_DisplayName() { return new Localizable(holder, "IdStrategy.CaseSensitiveEmailAddress.DisplayName", new Object[0]); }
  
  public static String CLI_disable_job_shortDescription() { return holder.format("CLI.disable-job.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_disable_job_shortDescription() { return new Localizable(holder, "CLI.disable-job.shortDescription", new Object[0]); }
  
  public static String Hudson_Computer_Caption() { return holder.format("Hudson.Computer.Caption", new Object[0]); }
  
  public static Localizable _Hudson_Computer_Caption() { return new Localizable(holder, "Hudson.Computer.Caption", new Object[0]); }
  
  public static String PatternProjectNamingStrategy_NamePatternInvalidSyntax() { return holder.format("PatternProjectNamingStrategy.NamePatternInvalidSyntax", new Object[0]); }
  
  public static Localizable _PatternProjectNamingStrategy_NamePatternInvalidSyntax() { return new Localizable(holder, "PatternProjectNamingStrategy.NamePatternInvalidSyntax", new Object[0]); }
  
  public static String DefaultProjectNamingStrategy_DisplayName() { return holder.format("DefaultProjectNamingStrategy.DisplayName", new Object[0]); }
  
  public static Localizable _DefaultProjectNamingStrategy_DisplayName() { return new Localizable(holder, "DefaultProjectNamingStrategy.DisplayName", new Object[0]); }
  
  public static String Hudson_ControlCodeNotAllowed(Object arg0) { return holder.format("Hudson.ControlCodeNotAllowed", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_ControlCodeNotAllowed(Object arg0) { return new Localizable(holder, "Hudson.ControlCodeNotAllowed", new Object[] { arg0 }); }
  
  public static String JenkinsLocationConfiguration_does_not_look_like_an_email_address() { return holder.format("JenkinsLocationConfiguration.does_not_look_like_an_email_address", new Object[0]); }
  
  public static Localizable _JenkinsLocationConfiguration_does_not_look_like_an_email_address() { return new Localizable(holder, "JenkinsLocationConfiguration.does_not_look_like_an_email_address", new Object[0]); }
  
  public static String IdStrategy_CaseInsensitive_DisplayName() { return holder.format("IdStrategy.CaseInsensitive.DisplayName", new Object[0]); }
  
  public static Localizable _IdStrategy_CaseInsensitive_DisplayName() { return new Localizable(holder, "IdStrategy.CaseInsensitive.DisplayName", new Object[0]); }
  
  public static String Hudson_ViewName() { return holder.format("Hudson.ViewName", new Object[0]); }
  
  public static Localizable _Hudson_ViewName() { return new Localizable(holder, "Hudson.ViewName", new Object[0]); }
  
  public static String Hudson_NoJavaInPath(Object arg0) { return holder.format("Hudson.NoJavaInPath", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_NoJavaInPath(Object arg0) { return new Localizable(holder, "Hudson.NoJavaInPath", new Object[] { arg0 }); }
  
  public static String CLI_shutdown_shortDescription() { return holder.format("CLI.shutdown.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_shutdown_shortDescription() { return new Localizable(holder, "CLI.shutdown.shortDescription", new Object[0]); }
  
  public static String ParameterizedJobMixIn_build_with_parameters() { return holder.format("ParameterizedJobMixIn.build_with_parameters", new Object[0]); }
  
  public static Localizable _ParameterizedJobMixIn_build_with_parameters() { return new Localizable(holder, "ParameterizedJobMixIn.build_with_parameters", new Object[0]); }
  
  public static String GlobalCloudConfiguration_DisplayName() { return holder.format("GlobalCloudConfiguration.DisplayName", new Object[0]); }
  
  public static Localizable _GlobalCloudConfiguration_DisplayName() { return new Localizable(holder, "GlobalCloudConfiguration.DisplayName", new Object[0]); }
  
  public static String ParameterizedJobMixIn_build_now() { return holder.format("ParameterizedJobMixIn.build_now", new Object[0]); }
  
  public static Localizable _ParameterizedJobMixIn_build_now() { return new Localizable(holder, "ParameterizedJobMixIn.build_now", new Object[0]); }
  
  public static String Hudson_DisplayName() { return holder.format("Hudson.DisplayName", new Object[0]); }
  
  public static Localizable _Hudson_DisplayName() { return new Localizable(holder, "Hudson.DisplayName", new Object[0]); }
  
  public static String JobGlobalBuildDiscarderStrategy_displayName() { return holder.format("JobGlobalBuildDiscarderStrategy.displayName", new Object[0]); }
  
  public static Localizable _JobGlobalBuildDiscarderStrategy_displayName() { return new Localizable(holder, "JobGlobalBuildDiscarderStrategy.displayName", new Object[0]); }
  
  public static String Mailer_Localhost_Error() { return holder.format("Mailer.Localhost.Error", new Object[0]); }
  
  public static Localizable _Mailer_Localhost_Error() { return new Localizable(holder, "Mailer.Localhost.Error", new Object[0]); }
  
  public static String Hudson_Computer_IncorrectNumberOfExecutors() { return holder.format("Hudson.Computer.IncorrectNumberOfExecutors", new Object[0]); }
  
  public static Localizable _Hudson_Computer_IncorrectNumberOfExecutors() { return new Localizable(holder, "Hudson.Computer.IncorrectNumberOfExecutors", new Object[0]); }
  
  public static String Hudson_UnsafeChar(Object arg0) { return holder.format("Hudson.UnsafeChar", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_UnsafeChar(Object arg0) { return new Localizable(holder, "Hudson.UnsafeChar", new Object[] { arg0 }); }
  
  public static String CLI_safe_shutdown_shortDescription() { return holder.format("CLI.safe-shutdown.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_safe_shutdown_shortDescription() { return new Localizable(holder, "CLI.safe-shutdown.shortDescription", new Object[0]); }
  
  public static String SimpleGlobalBuildDiscarderStrategy_displayName() { return holder.format("SimpleGlobalBuildDiscarderStrategy.displayName", new Object[0]); }
  
  public static Localizable _SimpleGlobalBuildDiscarderStrategy_displayName() { return new Localizable(holder, "SimpleGlobalBuildDiscarderStrategy.displayName", new Object[0]); }
  
  public static String BlockedBecauseOfBuildInProgress_ETA(Object arg0) { return holder.format("BlockedBecauseOfBuildInProgress.ETA", new Object[] { arg0 }); }
  
  public static Localizable _BlockedBecauseOfBuildInProgress_ETA(Object arg0) { return new Localizable(holder, "BlockedBecauseOfBuildInProgress.ETA", new Object[] { arg0 }); }
  
  public static String Hudson_NodeBeingRemoved() { return holder.format("Hudson.NodeBeingRemoved", new Object[0]); }
  
  public static Localizable _Hudson_NodeBeingRemoved() { return new Localizable(holder, "Hudson.NodeBeingRemoved", new Object[0]); }
  
  public static String CLI_safe_restart_shortDescription() { return holder.format("CLI.safe-restart.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_safe_restart_shortDescription() { return new Localizable(holder, "CLI.safe-restart.shortDescription", new Object[0]); }
  
  public static String Hudson_JobNameConventionNotApplyed(Object arg0, Object arg1) { return holder.format("Hudson.JobNameConventionNotApplyed", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Hudson_JobNameConventionNotApplyed(Object arg0, Object arg1) { return new Localizable(holder, "Hudson.JobNameConventionNotApplyed", new Object[] { arg0, arg1 }); }
  
  public static String CLI_restart_shortDescription() { return holder.format("CLI.restart.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_restart_shortDescription() { return new Localizable(holder, "CLI.restart.shortDescription", new Object[0]); }
  
  public static String CLI_enable_job_shortDescription() { return holder.format("CLI.enable-job.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_enable_job_shortDescription() { return new Localizable(holder, "CLI.enable-job.shortDescription", new Object[0]); }
  
  public static String Mailer_Address_Not_Configured() { return holder.format("Mailer.Address.Not.Configured", new Object[0]); }
  
  public static Localizable _Mailer_Address_Not_Configured() { return new Localizable(holder, "Mailer.Address.Not.Configured", new Object[0]); }
  
  public static String CauseOfInterruption_ShortDescription(Object arg0) { return holder.format("CauseOfInterruption.ShortDescription", new Object[] { arg0 }); }
  
  public static Localizable _CauseOfInterruption_ShortDescription(Object arg0) { return new Localizable(holder, "CauseOfInterruption.ShortDescription", new Object[] { arg0 }); }
  
  public static String Hudson_BadPortNumber(Object arg0) { return holder.format("Hudson.BadPortNumber", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_BadPortNumber(Object arg0) { return new Localizable(holder, "Hudson.BadPortNumber", new Object[] { arg0 }); }
  
  public static String Hudson_NotUsesUTF8ToDecodeURL() { return holder.format("Hudson.NotUsesUTF8ToDecodeURL", new Object[0]); }
  
  public static Localizable _Hudson_NotUsesUTF8ToDecodeURL() { return new Localizable(holder, "Hudson.NotUsesUTF8ToDecodeURL", new Object[0]); }
  
  public static String BlockedBecauseOfBuildInProgress_shortDescription(Object arg0, Object arg1) { return holder.format("BlockedBecauseOfBuildInProgress.shortDescription", new Object[] { arg0, arg1 }); }
  
  public static Localizable _BlockedBecauseOfBuildInProgress_shortDescription(Object arg0, Object arg1) { return new Localizable(holder, "BlockedBecauseOfBuildInProgress.shortDescription", new Object[] { arg0, arg1 }); }
  
  public static String Hudson_Computer_DisplayName() { return holder.format("Hudson.Computer.DisplayName", new Object[0]); }
  
  public static Localizable _Hudson_Computer_DisplayName() { return new Localizable(holder, "Hudson.Computer.DisplayName", new Object[0]); }
  
  public static String EnforceSlaveAgentPortAdministrativeMonitor_displayName() { return holder.format("EnforceSlaveAgentPortAdministrativeMonitor.displayName", new Object[0]); }
  
  public static Localizable _EnforceSlaveAgentPortAdministrativeMonitor_displayName() { return new Localizable(holder, "EnforceSlaveAgentPortAdministrativeMonitor.displayName", new Object[0]); }
  
  public static String Mailer_NotHttp_Error() { return holder.format("Mailer.NotHttp.Error", new Object[0]); }
  
  public static Localizable _Mailer_NotHttp_Error() { return new Localizable(holder, "Mailer.NotHttp.Error", new Object[0]); }
  
  public static String IdStrategy_CaseSensitive_DisplayName() { return holder.format("IdStrategy.CaseSensitive.DisplayName", new Object[0]); }
  
  public static Localizable _IdStrategy_CaseSensitive_DisplayName() { return new Localizable(holder, "IdStrategy.CaseSensitive.DisplayName", new Object[0]); }
  
  public static String Hudson_NoName() { return holder.format("Hudson.NoName", new Object[0]); }
  
  public static Localizable _Hudson_NoName() { return new Localizable(holder, "Hudson.NoName", new Object[0]); }
  
  public static String PatternProjectNamingStrategy_DisplayName() { return holder.format("PatternProjectNamingStrategy.DisplayName", new Object[0]); }
  
  public static Localizable _PatternProjectNamingStrategy_DisplayName() { return new Localizable(holder, "PatternProjectNamingStrategy.DisplayName", new Object[0]); }
  
  public static String Hudson_JobAlreadyExists(Object arg0) { return holder.format("Hudson.JobAlreadyExists", new Object[] { arg0 }); }
  
  public static Localizable _Hudson_JobAlreadyExists(Object arg0) { return new Localizable(holder, "Hudson.JobAlreadyExists", new Object[] { arg0 }); }
  
  public static String CLI_keep_build_shortDescription() { return holder.format("CLI.keep-build.shortDescription", new Object[0]); }
  
  public static Localizable _CLI_keep_build_shortDescription() { return new Localizable(holder, "CLI.keep-build.shortDescription", new Object[0]); }
  
  public static String BuildDiscarderProperty_displayName() { return holder.format("BuildDiscarderProperty.displayName", new Object[0]); }
  
  public static Localizable _BuildDiscarderProperty_displayName() { return new Localizable(holder, "BuildDiscarderProperty.displayName", new Object[0]); }
  
  public static String PatternProjectNamingStrategy_NamePatternRequired() { return holder.format("PatternProjectNamingStrategy.NamePatternRequired", new Object[0]); }
  
  public static Localizable _PatternProjectNamingStrategy_NamePatternRequired() { return new Localizable(holder, "PatternProjectNamingStrategy.NamePatternRequired", new Object[0]); }
}
