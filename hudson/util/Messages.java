package hudson.util;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String FormValidation_Error_Details() { return holder.format("FormValidation.Error.Details", new Object[0]); }
  
  public static Localizable _FormValidation_Error_Details() { return new Localizable(holder, "FormValidation.Error.Details", new Object[0]); }
  
  public static String Retrier_Interruption(Object arg0) { return holder.format("Retrier.Interruption", new Object[] { arg0 }); }
  
  public static Localizable _Retrier_Interruption(Object arg0) { return new Localizable(holder, "Retrier.Interruption", new Object[] { arg0 }); }
  
  public static String Retrier_ExceptionThrown(Object arg0, Object arg1) { return holder.format("Retrier.ExceptionThrown", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Retrier_ExceptionThrown(Object arg0, Object arg1) { return new Localizable(holder, "Retrier.ExceptionThrown", new Object[] { arg0, arg1 }); }
  
  public static String HttpResponses_Saved() { return holder.format("HttpResponses.Saved", new Object[0]); }
  
  public static Localizable _HttpResponses_Saved() { return new Localizable(holder, "HttpResponses.Saved", new Object[0]); }
  
  public static String ClockDifference_Failed() { return holder.format("ClockDifference.Failed", new Object[0]); }
  
  public static Localizable _ClockDifference_Failed() { return new Localizable(holder, "ClockDifference.Failed", new Object[0]); }
  
  public static String ClockDifference_Behind(Object arg0) { return holder.format("ClockDifference.Behind", new Object[] { arg0 }); }
  
  public static Localizable _ClockDifference_Behind(Object arg0) { return new Localizable(holder, "ClockDifference.Behind", new Object[] { arg0 }); }
  
  public static String Retrier_Sleeping(Object arg0, Object arg1) { return holder.format("Retrier.Sleeping", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Retrier_Sleeping(Object arg0, Object arg1) { return new Localizable(holder, "Retrier.Sleeping", new Object[] { arg0, arg1 }); }
  
  public static String Retrier_NoSuccess(Object arg0, Object arg1) { return holder.format("Retrier.NoSuccess", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Retrier_NoSuccess(Object arg0, Object arg1) { return new Localizable(holder, "Retrier.NoSuccess", new Object[] { arg0, arg1 }); }
  
  public static String Retrier_CallingListener(Object arg0, Object arg1, Object arg2) { return holder.format("Retrier.CallingListener", new Object[] { arg0, arg1, arg2 }); }
  
  public static Localizable _Retrier_CallingListener(Object arg0, Object arg1, Object arg2) { return new Localizable(holder, "Retrier.CallingListener", new Object[] { arg0, arg1, arg2 }); }
  
  public static String FormValidation_ValidateRequired() { return holder.format("FormValidation.ValidateRequired", new Object[0]); }
  
  public static Localizable _FormValidation_ValidateRequired() { return new Localizable(holder, "FormValidation.ValidateRequired", new Object[0]); }
  
  public static String Retrier_Attempt(Object arg0, Object arg1) { return holder.format("Retrier.Attempt", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Retrier_Attempt(Object arg0, Object arg1) { return new Localizable(holder, "Retrier.Attempt", new Object[] { arg0, arg1 }); }
  
  public static String ClockDifference_Ahead(Object arg0) { return holder.format("ClockDifference.Ahead", new Object[] { arg0 }); }
  
  public static Localizable _ClockDifference_Ahead(Object arg0) { return new Localizable(holder, "ClockDifference.Ahead", new Object[] { arg0 }); }
  
  public static String ClockDifference_InSync() { return holder.format("ClockDifference.InSync", new Object[0]); }
  
  public static Localizable _ClockDifference_InSync() { return new Localizable(holder, "ClockDifference.InSync", new Object[0]); }
  
  public static String DoubleLaunchChecker_duplicate_jenkins_checker() { return holder.format("DoubleLaunchChecker.duplicate_jenkins_checker", new Object[0]); }
  
  public static Localizable _DoubleLaunchChecker_duplicate_jenkins_checker() { return new Localizable(holder, "DoubleLaunchChecker.duplicate_jenkins_checker", new Object[0]); }
  
  public static String Retrier_ExceptionFailed(Object arg0, Object arg1) { return holder.format("Retrier.ExceptionFailed", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Retrier_ExceptionFailed(Object arg0, Object arg1) { return new Localizable(holder, "Retrier.ExceptionFailed", new Object[] { arg0, arg1 }); }
  
  public static String Retrier_Success(Object arg0, Object arg1) { return holder.format("Retrier.Success", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Retrier_Success(Object arg0, Object arg1) { return new Localizable(holder, "Retrier.Success", new Object[] { arg0, arg1 }); }
  
  public static String FormFieldValidator_did_not_manage_to_validate_may_be_too_sl(Object arg0) { return holder.format("FormFieldValidator.did_not_manage_to_validate_may_be_too_sl", new Object[] { arg0 }); }
  
  public static Localizable _FormFieldValidator_did_not_manage_to_validate_may_be_too_sl(Object arg0) { return new Localizable(holder, "FormFieldValidator.did_not_manage_to_validate_may_be_too_sl", new Object[] { arg0 }); }
  
  public static String Retrier_AttemptFailed(Object arg0, Object arg1) { return holder.format("Retrier.AttemptFailed", new Object[] { arg0, arg1 }); }
  
  public static Localizable _Retrier_AttemptFailed(Object arg0, Object arg1) { return new Localizable(holder, "Retrier.AttemptFailed", new Object[] { arg0, arg1 }); }
}
