package jenkins.agents;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.FilePath;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import jenkins.util.JenkinsJVM;

public final class AgentComputerUtil {
  @CheckForNull
  public static VirtualChannel getChannelToController() {
    if (JenkinsJVM.isJenkinsJVM())
      return FilePath.localChannel; 
    c = Channel.current();
    if (c != null && Boolean.TRUE.equals(c.getProperty("agent")))
      return c; 
    return null;
  }
  
  @Deprecated
  @CheckForNull
  public static VirtualChannel getChannelToMaster() { return getChannelToController(); }
}
