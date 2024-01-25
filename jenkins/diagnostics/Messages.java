package jenkins.diagnostics;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String SecurityIsOffMonitor_DisplayName() { return holder.format("SecurityIsOffMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _SecurityIsOffMonitor_DisplayName() { return new Localizable(holder, "SecurityIsOffMonitor.DisplayName", new Object[0]); }
  
  public static String RootUrlNotSetMonitor_DisplayName() { return holder.format("RootUrlNotSetMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _RootUrlNotSetMonitor_DisplayName() { return new Localizable(holder, "RootUrlNotSetMonitor.DisplayName", new Object[0]); }
  
  public static String ControllerExecutorsNoAgents_DisplayName() { return holder.format("ControllerExecutorsNoAgents.DisplayName", new Object[0]); }
  
  public static Localizable _ControllerExecutorsNoAgents_DisplayName() { return new Localizable(holder, "ControllerExecutorsNoAgents.DisplayName", new Object[0]); }
  
  public static String ControllerExecutorsAgents_DisplayName() { return holder.format("ControllerExecutorsAgents.DisplayName", new Object[0]); }
  
  public static Localizable _ControllerExecutorsAgents_DisplayName() { return new Localizable(holder, "ControllerExecutorsAgents.DisplayName", new Object[0]); }
  
  public static String CompletedInitializationMonitor_DisplayName() { return holder.format("CompletedInitializationMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _CompletedInitializationMonitor_DisplayName() { return new Localizable(holder, "CompletedInitializationMonitor.DisplayName", new Object[0]); }
  
  public static String URICheckEncodingMonitor_DisplayName() { return holder.format("URICheckEncodingMonitor.DisplayName", new Object[0]); }
  
  public static Localizable _URICheckEncodingMonitor_DisplayName() { return new Localizable(holder, "URICheckEncodingMonitor.DisplayName", new Object[0]); }
}
