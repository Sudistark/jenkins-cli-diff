package jenkins;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.IOException;
import java.net.Socket;

public abstract class AgentProtocol implements ExtensionPoint {
  public boolean isOptIn() { return false; }
  
  public boolean isRequired() { return false; }
  
  public boolean isDeprecated() { return false; }
  
  public abstract String getName();
  
  public String getDisplayName() { return getName(); }
  
  public abstract void handle(Socket paramSocket) throws IOException, InterruptedException;
  
  public static ExtensionList<AgentProtocol> all() { return ExtensionList.lookup(AgentProtocol.class); }
  
  @CheckForNull
  public static AgentProtocol of(String protocolName) {
    for (AgentProtocol p : all()) {
      String n = p.getName();
      if (n != null && n.equals(protocolName))
        return p; 
    } 
    return null;
  }
}
