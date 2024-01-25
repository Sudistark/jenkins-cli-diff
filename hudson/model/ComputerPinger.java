package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

public abstract class ComputerPinger implements ExtensionPoint {
  public abstract boolean isReachable(InetAddress paramInetAddress, int paramInt) throws IOException;
  
  public static ExtensionList<ComputerPinger> all() { return ExtensionList.lookup(ComputerPinger.class); }
  
  public static boolean checkIsReachable(InetAddress ia, int timeout) throws IOException {
    for (ComputerPinger pinger : all()) {
      try {
        if (pinger.isReachable(ia, timeout))
          return true; 
      } catch (IOException e) {
        LOGGER.fine("Error checking reachability with " + pinger + ": " + e.getMessage());
      } 
    } 
    return false;
  }
  
  private static final Logger LOGGER = Logger.getLogger(ComputerPinger.class.getName());
}
