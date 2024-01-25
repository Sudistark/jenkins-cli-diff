package hudson.cli;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.remoting.Channel;

@Deprecated
public abstract class CliTransportAuthenticator implements ExtensionPoint {
  public abstract boolean supportsProtocol(String paramString);
  
  public abstract void authenticate(String paramString, Channel paramChannel, Connection paramConnection);
  
  public static ExtensionList<CliTransportAuthenticator> all() { return ExtensionList.lookup(CliTransportAuthenticator.class); }
}
