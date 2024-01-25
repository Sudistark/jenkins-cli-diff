package jenkins.security;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.remoting.ChannelBuilder;

public abstract class ChannelConfigurator implements ExtensionPoint {
  public void onChannelBuilding(ChannelBuilder builder, @Nullable Object context) {}
  
  public static ExtensionList<ChannelConfigurator> all() { return ExtensionList.lookup(ChannelConfigurator.class); }
}
