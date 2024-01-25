package jenkins.slaves;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String JnlpSlaveAgentProtocol_displayName() { return holder.format("JnlpSlaveAgentProtocol.displayName", new Object[0]); }
  
  public static Localizable _JnlpSlaveAgentProtocol_displayName() { return new Localizable(holder, "JnlpSlaveAgentProtocol.displayName", new Object[0]); }
  
  public static String JnlpSlaveAgentProtocol2_displayName() { return holder.format("JnlpSlaveAgentProtocol2.displayName", new Object[0]); }
  
  public static Localizable _JnlpSlaveAgentProtocol2_displayName() { return new Localizable(holder, "JnlpSlaveAgentProtocol2.displayName", new Object[0]); }
  
  public static String JnlpSlaveAgentProtocol4_displayName() { return holder.format("JnlpSlaveAgentProtocol4.displayName", new Object[0]); }
  
  public static Localizable _JnlpSlaveAgentProtocol4_displayName() { return new Localizable(holder, "JnlpSlaveAgentProtocol4.displayName", new Object[0]); }
  
  public static String DeprecatedAgentProtocolMonitor_displayName() { return holder.format("DeprecatedAgentProtocolMonitor.displayName", new Object[0]); }
  
  public static Localizable _DeprecatedAgentProtocolMonitor_displayName() { return new Localizable(holder, "DeprecatedAgentProtocolMonitor.displayName", new Object[0]); }
  
  public static String JnlpSlaveAgentProtocol3_displayName() { return holder.format("JnlpSlaveAgentProtocol3.displayName", new Object[0]); }
  
  public static Localizable _JnlpSlaveAgentProtocol3_displayName() { return new Localizable(holder, "JnlpSlaveAgentProtocol3.displayName", new Object[0]); }
}
