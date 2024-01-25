package hudson.tasks;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String ArtifactArchiver_ARCHIVING_ARTIFACTS() { return holder.format("ArtifactArchiver.ARCHIVING_ARTIFACTS", new Object[0]); }
  
  public static Localizable _ArtifactArchiver_ARCHIVING_ARTIFACTS() { return new Localizable(holder, "ArtifactArchiver.ARCHIVING_ARTIFACTS", new Object[0]); }
  
  public static String Shell_invalid_exit_code_zero() { return holder.format("Shell.invalid_exit_code_zero", new Object[0]); }
  
  public static Localizable _Shell_invalid_exit_code_zero() { return new Localizable(holder, "Shell.invalid_exit_code_zero", new Object[0]); }
  
  public static String JavadocArchiver_DisplayName_Javadoc() { return holder.format("JavadocArchiver.DisplayName.Javadoc", new Object[0]); }
  
  public static Localizable _JavadocArchiver_DisplayName_Javadoc() { return new Localizable(holder, "JavadocArchiver.DisplayName.Javadoc", new Object[0]); }
  
  public static String Maven_DisplayName() { return holder.format("Maven.DisplayName", new Object[0]); }
  
  public static Localizable _Maven_DisplayName() { return new Localizable(holder, "Maven.DisplayName", new Object[0]); }
  
  public static String JavadocArchiver_NoMatchFound(Object arg0, Object arg1) { return holder.format("JavadocArchiver.NoMatchFound", new Object[] { arg0, arg1 }); }
  
  public static Localizable _JavadocArchiver_NoMatchFound(Object arg0, Object arg1) { return new Localizable(holder, "JavadocArchiver.NoMatchFound", new Object[] { arg0, arg1 }); }
  
  public static String Shell_invalid_exit_code_range(Object arg0) { return holder.format("Shell.invalid_exit_code_range", new Object[] { arg0 }); }
  
  public static Localizable _Shell_invalid_exit_code_range(Object arg0) { return new Localizable(holder, "Shell.invalid_exit_code_range", new Object[] { arg0 }); }
  
  public static String BuildTrigger_you_have_no_permission_to_build_(Object arg0) { return holder.format("BuildTrigger.you_have_no_permission_to_build_", new Object[] { arg0 }); }
  
  public static Localizable _BuildTrigger_you_have_no_permission_to_build_(Object arg0) { return new Localizable(holder, "BuildTrigger.you_have_no_permission_to_build_", new Object[] { arg0 }); }
  
  public static String Maven_NoExecutable(Object arg0) { return holder.format("Maven.NoExecutable", new Object[] { arg0 }); }
  
  public static Localizable _Maven_NoExecutable(Object arg0) { return new Localizable(holder, "Maven.NoExecutable", new Object[] { arg0 }); }
  
  public static String Fingerprinter_FailedFor(Object arg0) { return holder.format("Fingerprinter.FailedFor", new Object[] { arg0 }); }
  
  public static Localizable _Fingerprinter_FailedFor(Object arg0) { return new Localizable(holder, "Fingerprinter.FailedFor", new Object[] { arg0 }); }
  
  public static String Maven_NotMavenDirectory(Object arg0) { return holder.format("Maven.NotMavenDirectory", new Object[] { arg0 }); }
  
  public static Localizable _Maven_NotMavenDirectory(Object arg0) { return new Localizable(holder, "Maven.NotMavenDirectory", new Object[] { arg0 }); }
  
  public static String BatchFile_DisplayName() { return holder.format("BatchFile.DisplayName", new Object[0]); }
  
  public static Localizable _BatchFile_DisplayName() { return new Localizable(holder, "BatchFile.DisplayName", new Object[0]); }
  
  public static String BatchFile_invalid_exit_code_zero() { return holder.format("BatchFile.invalid_exit_code_zero", new Object[0]); }
  
  public static Localizable _BatchFile_invalid_exit_code_zero() { return new Localizable(holder, "BatchFile.invalid_exit_code_zero", new Object[0]); }
  
  public static String Ant_GlobalConfigNeeded() { return holder.format("Ant.GlobalConfigNeeded", new Object[0]); }
  
  public static Localizable _Ant_GlobalConfigNeeded() { return new Localizable(holder, "Ant.GlobalConfigNeeded", new Object[0]); }
  
  public static String BuildTrigger_Disabled(Object arg0) { return holder.format("BuildTrigger.Disabled", new Object[] { arg0 }); }
  
  public static Localizable _BuildTrigger_Disabled(Object arg0) { return new Localizable(holder, "BuildTrigger.Disabled", new Object[] { arg0 }); }
  
  public static String BuildTrigger_ok_ancestor_is_null() { return holder.format("BuildTrigger.ok_ancestor_is_null", new Object[0]); }
  
  public static Localizable _BuildTrigger_ok_ancestor_is_null() { return new Localizable(holder, "BuildTrigger.ok_ancestor_is_null", new Object[0]); }
  
  public static String Maven_ExecFailed() { return holder.format("Maven.ExecFailed", new Object[0]); }
  
  public static Localizable _Maven_ExecFailed() { return new Localizable(holder, "Maven.ExecFailed", new Object[0]); }
  
  public static String BuildTrigger_NoProjectSpecified() { return holder.format("BuildTrigger.NoProjectSpecified", new Object[0]); }
  
  public static Localizable _BuildTrigger_NoProjectSpecified() { return new Localizable(holder, "BuildTrigger.NoProjectSpecified", new Object[0]); }
  
  public static String TestJavadocArchiver_DisplayName_Javadoc() { return holder.format("TestJavadocArchiver.DisplayName.Javadoc", new Object[0]); }
  
  public static Localizable _TestJavadocArchiver_DisplayName_Javadoc() { return new Localizable(holder, "TestJavadocArchiver.DisplayName.Javadoc", new Object[0]); }
  
  public static String CommandInterpreter_UnableToProduceScript() { return holder.format("CommandInterpreter.UnableToProduceScript", new Object[0]); }
  
  public static Localizable _CommandInterpreter_UnableToProduceScript() { return new Localizable(holder, "CommandInterpreter.UnableToProduceScript", new Object[0]); }
  
  public static String Fingerprinter_DigestFailed(Object arg0) { return holder.format("Fingerprinter.DigestFailed", new Object[] { arg0 }); }
  
  public static Localizable _Fingerprinter_DigestFailed(Object arg0) { return new Localizable(holder, "Fingerprinter.DigestFailed", new Object[] { arg0 }); }
  
  public static String Fingerprinter_Failed() { return holder.format("Fingerprinter.Failed", new Object[0]); }
  
  public static Localizable _Fingerprinter_Failed() { return new Localizable(holder, "Fingerprinter.Failed", new Object[0]); }
  
  public static String Ant_ProjectConfigNeeded() { return holder.format("Ant.ProjectConfigNeeded", new Object[0]); }
  
  public static Localizable _Ant_ProjectConfigNeeded() { return new Localizable(holder, "Ant.ProjectConfigNeeded", new Object[0]); }
  
  public static String Ant_ExecFailed() { return holder.format("Ant.ExecFailed", new Object[0]); }
  
  public static Localizable _Ant_ExecFailed() { return new Localizable(holder, "Ant.ExecFailed", new Object[0]); }
  
  public static String Fingerprinter_Recording() { return holder.format("Fingerprinter.Recording", new Object[0]); }
  
  public static Localizable _Fingerprinter_Recording() { return new Localizable(holder, "Fingerprinter.Recording", new Object[0]); }
  
  public static String BatchFile_invalid_exit_code_range(Object arg0) { return holder.format("BatchFile.invalid_exit_code_range", new Object[] { arg0 }); }
  
  public static Localizable _BatchFile_invalid_exit_code_range(Object arg0) { return new Localizable(holder, "BatchFile.invalid_exit_code_range", new Object[] { arg0 }); }
  
  public static String Fingerprinter_Aborted() { return holder.format("Fingerprinter.Aborted", new Object[0]); }
  
  public static Localizable _Fingerprinter_Aborted() { return new Localizable(holder, "Fingerprinter.Aborted", new Object[0]); }
  
  public static String Ant_DisplayName() { return holder.format("Ant.DisplayName", new Object[0]); }
  
  public static Localizable _Ant_DisplayName() { return new Localizable(holder, "Ant.DisplayName", new Object[0]); }
  
  public static String InstallFromApache() { return holder.format("InstallFromApache", new Object[0]); }
  
  public static Localizable _InstallFromApache() { return new Localizable(holder, "InstallFromApache", new Object[0]); }
  
  public static String Ant_NotAntDirectory(Object arg0) { return holder.format("Ant.NotAntDirectory", new Object[] { arg0 }); }
  
  public static Localizable _Ant_NotAntDirectory(Object arg0) { return new Localizable(holder, "Ant.NotAntDirectory", new Object[] { arg0 }); }
  
  public static String Shell_DisplayName() { return holder.format("Shell.DisplayName", new Object[0]); }
  
  public static Localizable _Shell_DisplayName() { return new Localizable(holder, "Shell.DisplayName", new Object[0]); }
  
  public static String Fingerprinter_DisplayName() { return holder.format("Fingerprinter.DisplayName", new Object[0]); }
  
  public static Localizable _Fingerprinter_DisplayName() { return new Localizable(holder, "Fingerprinter.DisplayName", new Object[0]); }
  
  public static String BuildTrigger_NotBuildable(Object arg0) { return holder.format("BuildTrigger.NotBuildable", new Object[] { arg0 }); }
  
  public static Localizable _BuildTrigger_NotBuildable(Object arg0) { return new Localizable(holder, "BuildTrigger.NotBuildable", new Object[] { arg0 }); }
  
  public static String Ant_NotADirectory(Object arg0) { return holder.format("Ant.NotADirectory", new Object[] { arg0 }); }
  
  public static Localizable _Ant_NotADirectory(Object arg0) { return new Localizable(holder, "Ant.NotADirectory", new Object[] { arg0 }); }
  
  public static String BuildTrigger_NoSuchProject(Object arg0, Object arg1) { return holder.format("BuildTrigger.NoSuchProject", new Object[] { arg0, arg1 }); }
  
  public static Localizable _BuildTrigger_NoSuchProject(Object arg0, Object arg1) { return new Localizable(holder, "BuildTrigger.NoSuchProject", new Object[] { arg0, arg1 }); }
  
  public static String CommandInterpreter_CommandFailed() { return holder.format("CommandInterpreter.CommandFailed", new Object[0]); }
  
  public static Localizable _CommandInterpreter_CommandFailed() { return new Localizable(holder, "CommandInterpreter.CommandFailed", new Object[0]); }
  
  public static String ArtifactArchiver_NoIncludes() { return holder.format("ArtifactArchiver.NoIncludes", new Object[0]); }
  
  public static Localizable _ArtifactArchiver_NoIncludes() { return new Localizable(holder, "ArtifactArchiver.NoIncludes", new Object[0]); }
  
  public static String Fingerprinter_Action_DisplayName() { return holder.format("Fingerprinter.Action.DisplayName", new Object[0]); }
  
  public static Localizable _Fingerprinter_Action_DisplayName() { return new Localizable(holder, "Fingerprinter.Action.DisplayName", new Object[0]); }
  
  public static String JavadocArchiver_UnableToCopy(Object arg0, Object arg1) { return holder.format("JavadocArchiver.UnableToCopy", new Object[] { arg0, arg1 }); }
  
  public static Localizable _JavadocArchiver_UnableToCopy(Object arg0, Object arg1) { return new Localizable(holder, "JavadocArchiver.UnableToCopy", new Object[] { arg0, arg1 }); }
  
  public static String BuildTrigger_DisplayName() { return holder.format("BuildTrigger.DisplayName", new Object[0]); }
  
  public static Localizable _BuildTrigger_DisplayName() { return new Localizable(holder, "BuildTrigger.DisplayName", new Object[0]); }
  
  public static String JavadocArchiver_Publishing() { return holder.format("JavadocArchiver.Publishing", new Object[0]); }
  
  public static Localizable _JavadocArchiver_Publishing() { return new Localizable(holder, "JavadocArchiver.Publishing", new Object[0]); }
  
  public static String BuildTrigger_InQueue(Object arg0) { return holder.format("BuildTrigger.InQueue", new Object[] { arg0 }); }
  
  public static Localizable _BuildTrigger_InQueue(Object arg0) { return new Localizable(holder, "BuildTrigger.InQueue", new Object[] { arg0 }); }
  
  public static String CommandInterpreter_UnableToDelete(Object arg0) { return holder.format("CommandInterpreter.UnableToDelete", new Object[] { arg0 }); }
  
  public static Localizable _CommandInterpreter_UnableToDelete(Object arg0) { return new Localizable(holder, "CommandInterpreter.UnableToDelete", new Object[] { arg0 }); }
  
  public static String ArtifactArchiver_SkipBecauseOnlyIfSuccessful() { return holder.format("ArtifactArchiver.SkipBecauseOnlyIfSuccessful", new Object[0]); }
  
  public static Localizable _ArtifactArchiver_SkipBecauseOnlyIfSuccessful() { return new Localizable(holder, "ArtifactArchiver.SkipBecauseOnlyIfSuccessful", new Object[0]); }
  
  public static String ArtifactArchiver_NoMatchFound(Object arg0) { return holder.format("ArtifactArchiver.NoMatchFound", new Object[] { arg0 }); }
  
  public static Localizable _ArtifactArchiver_NoMatchFound(Object arg0) { return new Localizable(holder, "ArtifactArchiver.NoMatchFound", new Object[] { arg0 }); }
  
  public static String JavadocArchiver_DisplayName_Generic() { return holder.format("JavadocArchiver.DisplayName.Generic", new Object[0]); }
  
  public static Localizable _JavadocArchiver_DisplayName_Generic() { return new Localizable(holder, "JavadocArchiver.DisplayName.Generic", new Object[0]); }
  
  public static String ArtifactArchiver_DisplayName() { return holder.format("ArtifactArchiver.DisplayName", new Object[0]); }
  
  public static Localizable _ArtifactArchiver_DisplayName() { return new Localizable(holder, "ArtifactArchiver.DisplayName", new Object[0]); }
  
  public static String JavadocArchiver_DisplayName() { return holder.format("JavadocArchiver.DisplayName", new Object[0]); }
  
  public static Localizable _JavadocArchiver_DisplayName() { return new Localizable(holder, "JavadocArchiver.DisplayName", new Object[0]); }
  
  public static String Ant_ExecutableNotFound(Object arg0) { return holder.format("Ant.ExecutableNotFound", new Object[] { arg0 }); }
  
  public static Localizable _Ant_ExecutableNotFound(Object arg0) { return new Localizable(holder, "Ant.ExecutableNotFound", new Object[] { arg0 }); }
  
  public static String BuildTrigger_Triggering(Object arg0) { return holder.format("BuildTrigger.Triggering", new Object[] { arg0 }); }
  
  public static Localizable _BuildTrigger_Triggering(Object arg0) { return new Localizable(holder, "BuildTrigger.Triggering", new Object[] { arg0 }); }
}
