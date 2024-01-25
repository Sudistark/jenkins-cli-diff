package hudson.slaves;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import java.io.IOException;

public abstract class ComputerListener implements ExtensionPoint {
  public void preLaunch(Computer c, TaskListener taskListener) throws IOException, InterruptedException {}
  
  public void onLaunchFailure(Computer c, TaskListener taskListener) throws IOException, InterruptedException {}
  
  public void preOnline(Computer c, Channel channel, FilePath root, TaskListener listener) throws IOException, InterruptedException {}
  
  @Deprecated
  public void onOnline(Computer c) {}
  
  public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException { onOnline(c); }
  
  @Deprecated
  public void onOffline(Computer c) {}
  
  public void onOffline(@NonNull Computer c, @CheckForNull OfflineCause cause) { onOffline(c); }
  
  public void onTemporarilyOnline(Computer c) {}
  
  public void onTemporarilyOffline(Computer c, OfflineCause cause) {}
  
  public void onConfigurationChange() {}
  
  @Deprecated
  public final void register() { all().add(this); }
  
  public final boolean unregister() { return all().remove(this); }
  
  public static ExtensionList<ComputerListener> all() { return ExtensionList.lookup(ComputerListener.class); }
}
