package jenkins.slaves.restarter;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.Serializable;
import java.util.logging.Logger;

public abstract class SlaveRestarter implements ExtensionPoint, Serializable {
  public abstract boolean canWork();
  
  public abstract void restart();
  
  public static ExtensionList<SlaveRestarter> all() { return ExtensionList.lookup(SlaveRestarter.class); }
  
  private static final Logger LOGGER = Logger.getLogger(SlaveRestarter.class.getName());
  
  private static final long serialVersionUID = 1L;
}
