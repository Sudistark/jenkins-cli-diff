package jenkins.slaves;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.remoting.Channel;
import java.io.IOException;
import jenkins.model.Jenkins;

public abstract class PingFailureAnalyzer implements ExtensionPoint {
  public abstract void onPingFailure(Channel paramChannel, Throwable paramThrowable) throws IOException;
  
  public static ExtensionList<PingFailureAnalyzer> all() { return Jenkins.get().getExtensionList(PingFailureAnalyzer.class); }
}
