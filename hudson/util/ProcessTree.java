package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.agents.AgentComputerUtil;
import jenkins.util.SystemProperties;
import org.jenkinsci.remoting.SerializableOnlyOverRemoting;

public abstract class ProcessTree extends Object implements Iterable<ProcessTree.OSProcess>, ProcessTreeRemoting.IProcessTree, SerializableOnlyOverRemoting {
  protected final Map<Integer, OSProcess> processes = new HashMap();
  
  private boolean skipVetoes;
  
  private ProcessTree() { this.skipVetoes = false; }
  
  private ProcessTree(boolean vetoesExist) { this.skipVetoes = !vetoesExist; }
  
  @CheckForNull
  public final OSProcess get(int pid) { return (OSProcess)this.processes.get(Integer.valueOf(pid)); }
  
  @NonNull
  public final Iterator<OSProcess> iterator() { return this.processes.values().iterator(); }
  
  private final long softKillWaitSeconds = Integer.getInteger("SoftKillWaitSeconds", 5).intValue();
  
  @CheckForNull
  public abstract OSProcess get(@NonNull Process paramProcess);
  
  public abstract void killAll(@NonNull Map<String, String> paramMap) throws InterruptedException;
  
  public void killAll(@CheckForNull Process proc, @CheckForNull Map<String, String> modelEnvVars) throws InterruptedException {
    LOGGER.fine("killAll: process=" + proc + " and envs=" + modelEnvVars);
    if (proc != null) {
      OSProcess p = get(proc);
      if (p != null)
        p.killRecursively(); 
    } 
    if (modelEnvVars != null)
      killAll(modelEnvVars); 
  }
  
  @NonNull
  final List<ProcessKiller> getKillers() throws InterruptedException {
    if (this.killers == null)
      try {
        VirtualChannel channelToController = AgentComputerUtil.getChannelToController();
        if (channelToController != null) {
          this.killers = (List)channelToController.call(new ListAll());
        } else {
          this.killers = Collections.emptyList();
        } 
      } catch (IOException|Error e) {
        LOGGER.log(Level.WARNING, "Failed to obtain killers", e);
        this.killers = Collections.emptyList();
      }  
    return this.killers;
  }
  
  public static ProcessTree get() {
    if (!enabled)
      return DEFAULT; 
    if (vetoersExist == null)
      try {
        channelToController = AgentComputerUtil.getChannelToController();
        if (channelToController != null)
          vetoersExist = (Boolean)channelToController.call(new DoVetoersExist()); 
      } catch (InterruptedException ie) {
        InterruptedException interruptedException;
        LOGGER.log(Level.FINE, "Caught InterruptedException while checking if vetoers exist: ", interruptedException);
        Thread.interrupted();
      } catch (Exception ie) {
        LOGGER.log(Level.FINE, "Error while determining if vetoers exist", e);
      }  
    boolean bool = (vetoersExist == null) ? true : vetoersExist.booleanValue();
    try {
      if (File.pathSeparatorChar == ';')
        return new Windows(bool); 
      String os = Util.fixNull(System.getProperty("os.name"));
      if (os.equals("Linux"))
        return new Linux(bool); 
      if (os.equals("AIX"))
        return new AIX(bool); 
      if (os.equals("SunOS"))
        return new Solaris(bool); 
      if (os.equals("Mac OS X"))
        return new Darwin(bool); 
      if (os.equals("FreeBSD"))
        return new FreeBSD(bool); 
    } catch (LinkageError e) {
      LOGGER.log(Level.FINE, "Failed to load OS-specific implementation; reverting to the default", e);
      enabled = false;
    } 
    return DEFAULT;
  }
  
  static final ProcessTree DEFAULT = new Object();
  
  Object writeReplace() throws ObjectStreamException { return new Remote(this, getChannelForSerialization()); }
  
  private static final boolean IS_LITTLE_ENDIAN = "little".equals(System.getProperty("sun.cpu.endian"));
  
  private static final Logger LOGGER = Logger.getLogger(ProcessTree.class.getName());
  
  static boolean enabled = (!SystemProperties.getBoolean("hudson.util.ProcessTreeKiller.disable") && 
    !SystemProperties.getBoolean(ProcessTree.class.getName() + ".disable"));
}
