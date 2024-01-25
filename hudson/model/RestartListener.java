package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.IOException;

public abstract class RestartListener implements ExtensionPoint {
  public abstract boolean isReadyToRestart() throws IOException, InterruptedException;
  
  public void onRestart() {}
  
  public static ExtensionList<RestartListener> all() { return ExtensionList.lookup(RestartListener.class); }
  
  public static boolean isAllReady() throws IOException, InterruptedException {
    for (RestartListener listener : all()) {
      if (!listener.isReadyToRestart())
        return false; 
    } 
    return true;
  }
}
