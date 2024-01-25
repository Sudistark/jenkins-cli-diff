package jenkins.tasks.filters.impl;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String RetainVariablesLocalRule_RESET_DisplayName() { return holder.format("RetainVariablesLocalRule_RESET_DisplayName", new Object[0]); }
  
  public static Localizable _RetainVariablesLocalRule_RESET_DisplayName() { return new Localizable(holder, "RetainVariablesLocalRule_RESET_DisplayName", new Object[0]); }
  
  public static String RetainVariablesLocalRule_RemovalMessage(Object arg0, Object arg1) { return holder.format("RetainVariablesLocalRule.RemovalMessage", new Object[] { arg0, arg1 }); }
  
  public static Localizable _RetainVariablesLocalRule_RemovalMessage(Object arg0, Object arg1) { return new Localizable(holder, "RetainVariablesLocalRule.RemovalMessage", new Object[] { arg0, arg1 }); }
  
  public static String RetainVariablesLocalRule_DisplayName() { return holder.format("RetainVariablesLocalRule.DisplayName", new Object[0]); }
  
  public static Localizable _RetainVariablesLocalRule_DisplayName() { return new Localizable(holder, "RetainVariablesLocalRule.DisplayName", new Object[0]); }
  
  public static String RetainVariablesLocalRule_CharacteristicEnvVarsFormValidationOK() { return holder.format("RetainVariablesLocalRule.CharacteristicEnvVarsFormValidationOK", new Object[0]); }
  
  public static Localizable _RetainVariablesLocalRule_CharacteristicEnvVarsFormValidationOK() { return new Localizable(holder, "RetainVariablesLocalRule.CharacteristicEnvVarsFormValidationOK", new Object[0]); }
  
  public static String RetainVariablesLocalRule_ResetMessage(Object arg0, Object arg1) { return holder.format("RetainVariablesLocalRule.ResetMessage", new Object[] { arg0, arg1 }); }
  
  public static Localizable _RetainVariablesLocalRule_ResetMessage(Object arg0, Object arg1) { return new Localizable(holder, "RetainVariablesLocalRule.ResetMessage", new Object[] { arg0, arg1 }); }
  
  public static String RetainVariablesLocalRule_CharacteristicEnvVarsFormValidationWarning() { return holder.format("RetainVariablesLocalRule.CharacteristicEnvVarsFormValidationWarning", new Object[0]); }
  
  public static Localizable _RetainVariablesLocalRule_CharacteristicEnvVarsFormValidationWarning() { return new Localizable(holder, "RetainVariablesLocalRule.CharacteristicEnvVarsFormValidationWarning", new Object[0]); }
  
  public static String RetainVariablesLocalRule_REMOVE_DisplayName() { return holder.format("RetainVariablesLocalRule_REMOVE_DisplayName", new Object[0]); }
  
  public static Localizable _RetainVariablesLocalRule_REMOVE_DisplayName() { return new Localizable(holder, "RetainVariablesLocalRule_REMOVE_DisplayName", new Object[0]); }
}
