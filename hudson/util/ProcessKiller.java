package hudson.util;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.IOException;
import java.io.Serializable;

public abstract class ProcessKiller implements ExtensionPoint, Serializable {
  private static final long serialVersionUID = 1L;
  
  public static ExtensionList<ProcessKiller> all() { return ExtensionList.lookup(ProcessKiller.class); }
  
  public abstract boolean kill(ProcessTree.OSProcess paramOSProcess) throws IOException, InterruptedException;
}
