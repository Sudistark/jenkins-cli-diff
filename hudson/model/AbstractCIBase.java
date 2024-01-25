package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.security.AccessControlled;
import hudson.slaves.ComputerListener;
import hudson.slaves.RetentionStrategy;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.Listeners;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerProxy;

public abstract class AbstractCIBase extends Node implements ItemGroup<TopLevelItem>, StaplerProxy, StaplerFallback, ViewGroup, AccessControlled, DescriptorByNameOwner {
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean LOG_STARTUP_PERFORMANCE = SystemProperties.getBoolean(Jenkins.class.getName() + ".logStartupPerformance", false);
  
  private static final Logger LOGGER = Logger.getLogger(AbstractCIBase.class.getName());
  
  @Deprecated
  @NonNull
  public String getNodeName() { return ""; }
  
  @Deprecated
  public String getUrl() { return ""; }
  
  protected void resetLabel(Label l) { l.reset(); }
  
  protected void setViewOwner(View v) { v.owner = this; }
  
  protected void interruptReloadThread() { ViewJob.interruptReloadThread(); }
  
  protected void killComputer(Computer c) { c.kill(); }
  
  private final Set<String> disabledAdministrativeMonitors = new HashSet();
  
  public Set<String> getDisabledAdministrativeMonitors() {
    synchronized (this.disabledAdministrativeMonitors) {
      return new HashSet(this.disabledAdministrativeMonitors);
    } 
  }
  
  public void setDisabledAdministrativeMonitors(Set<String> disabledAdministrativeMonitors) {
    synchronized (this.disabledAdministrativeMonitors) {
      this.disabledAdministrativeMonitors.clear();
      this.disabledAdministrativeMonitors.addAll(disabledAdministrativeMonitors);
    } 
  }
  
  private void updateComputer(Node n, Map<String, Computer> byNameMap, Set<Computer> used, boolean automaticAgentLaunch) {
    Computer c = (Computer)byNameMap.get(n.getNodeName());
    if (c != null) {
      try {
        c.setNode(n);
        used.add(c);
      } catch (RuntimeException e) {
        LOGGER.log(Level.WARNING, "Error updating node " + n.getNodeName() + ", continuing", e);
      } 
    } else {
      c = createNewComputerForNode(n, automaticAgentLaunch);
      if (c != null)
        used.add(c); 
    } 
  }
  
  @CheckForNull
  private Computer createNewComputerForNode(Node n, boolean automaticAgentLaunch) {
    Computer c = null;
    ConcurrentMap<Node, Computer> computers = getComputerMap();
    if (n.getNumExecutors() > 0 || n == Jenkins.get()) {
      AtomicBoolean creationWasAttempted = new AtomicBoolean(false);
      try {
        c = (Computer)computers.computeIfAbsent(n, node -> {
              creationWasAttempted.set(true);
              return node.createComputer();
            });
      } catch (RuntimeException ex) {
        LOGGER.log(Level.WARNING, "Error retrieving computer for node " + n.getNodeName() + ", continuing", ex);
      } 
      if (!creationWasAttempted.get()) {
        LOGGER.log(Level.FINE, "Node {0} is not a new node skipping", n.getNodeName());
        return null;
      } 
      if (c == null) {
        LOGGER.log(Level.WARNING, "Cannot create computer for node {0}, the {1}#createComputer() method returned null. Skipping this node", new Object[] { n
              .getNodeName(), n.getClass().getName() });
        return null;
      } 
      if (!n.isHoldOffLaunchUntilSave() && automaticAgentLaunch) {
        RetentionStrategy retentionStrategy = c.getRetentionStrategy();
        if (retentionStrategy != null) {
          retentionStrategy.start(c);
        } else {
          c.connect(true);
        } 
      } 
      return c;
    } 
    LOGGER.log(Level.WARNING, "Node {0} has no executors. Cannot update the Computer instance of it", n.getNodeName());
    return null;
  }
  
  void removeComputer(Computer computer) {
    ConcurrentMap<Node, Computer> computers = getComputerMap();
    Queue.withLock(() -> {
          if (computers.values().remove(computer))
            computer.onRemoved(); 
        });
  }
  
  @CheckForNull
  Computer getComputer(Node n) {
    ConcurrentMap<Node, Computer> computers = getComputerMap();
    return (Computer)computers.get(n);
  }
  
  protected void updateNewComputer(Node n, boolean automaticAgentLaunch) {
    if (createNewComputerForNode(n, automaticAgentLaunch) == null)
      return; 
    getQueue().scheduleMaintenance();
    Listeners.notify(ComputerListener.class, false, ComputerListener::onConfigurationChange);
  }
  
  protected void updateComputerList(boolean automaticAgentLaunch) {
    ConcurrentMap<Node, Computer> computers = getComputerMap();
    Set<Computer> old = new HashSet<Computer>(computers.size());
    Queue.withLock(new Object(this, computers, old, automaticAgentLaunch));
    for (Computer c : old)
      killComputer(c); 
    getQueue().scheduleMaintenance();
    Listeners.notify(ComputerListener.class, false, ComputerListener::onConfigurationChange);
  }
  
  public abstract List<Node> getNodes();
  
  public abstract Queue getQueue();
  
  protected abstract ConcurrentMap<Node, Computer> getComputerMap();
}
