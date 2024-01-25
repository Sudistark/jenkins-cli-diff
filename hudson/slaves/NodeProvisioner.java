package hudson.slaves;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionList;
import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.LoadStatistics;
import hudson.model.MultiStageTimeSeries;
import hudson.model.Node;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import jenkins.util.Timer;
import net.jcip.annotations.GuardedBy;

public class NodeProvisioner {
  private final LoadStatistics stat;
  
  @CheckForNull
  private final Label label;
  
  private final AtomicReference<List<PlannedNode>> pendingLaunches;
  
  private final Lock provisioningLock;
  
  @GuardedBy("provisioningLock")
  private StrategyState provisioningState;
  
  private final MultiStageTimeSeries plannedCapacitiesEMA;
  
  public NodeProvisioner(@CheckForNull Label label, LoadStatistics loadStatistics) {
    this.pendingLaunches = new AtomicReference(new ArrayList());
    this.provisioningLock = new ReentrantLock();
    this.provisioningState = null;
    this
      .plannedCapacitiesEMA = new MultiStageTimeSeries(Messages._NodeProvisioner_EmptyString(), Color.WHITE, 0.0F, LoadStatistics.DECAY);
    this.label = label;
    this.stat = loadStatistics;
  }
  
  public List<PlannedNode> getPendingLaunches() { return new ArrayList((Collection)this.pendingLaunches.get()); }
  
  public void suggestReviewNow() {
    if (!this.queuedReview) {
      long delay = TimeUnit.SECONDS.toMillis(1L) - System.currentTimeMillis() - this.lastSuggestedReview;
      if (delay < 0L) {
        this.lastSuggestedReview = System.currentTimeMillis();
        Computer.threadPoolForRemoting.submit(() -> {
              LOGGER.fine(());
              update();
            });
      } else {
        this.queuedReview = true;
        LOGGER.fine(() -> "running suggested review in " + delay + " ms for " + this.label);
        Timer.get().schedule(() -> {
              this.lastSuggestedReview = System.currentTimeMillis();
              LOGGER.fine(());
              update();
            }delay, TimeUnit.MILLISECONDS);
      } 
    } else {
      LOGGER.fine(() -> "ignoring suggested review for " + this.label);
    } 
  }
  
  private void update() {
    long start = LOGGER.isLoggable(Level.FINER) ? System.nanoTime() : 0L;
    this.provisioningLock.lock();
    try {
      this.lastSuggestedReview = System.currentTimeMillis();
      this.queuedReview = false;
      Jenkins jenkins = Jenkins.get();
      int plannedCapacitySnapshot = 0;
      List<PlannedNode> snapPendingLaunches = new ArrayList<PlannedNode>((Collection)this.pendingLaunches.get());
      for (PlannedNode f : snapPendingLaunches) {
        if (f.future.isDone())
          try {
            boolean changed;
            List<PlannedNode> repl, orig = null;
            try {
              orig = (Node)f.future.get();
            } catch (InterruptedException e) {
              throw new AssertionError("InterruptedException occurred", repl);
            } catch (ExecutionException e) {
              Throwable cause = repl.getCause();
              if (!(cause instanceof hudson.AbortException))
                LOGGER.log(Level.WARNING, "Unexpected exception encountered while provisioning agent " + f.displayName, cause); 
              fireOnFailure(f, cause);
            } 
            if (orig != null) {
              fireOnComplete(f, orig);
              try {
                jenkins.addNode(orig);
                LOGGER.log(Level.INFO, "{0} provisioning successfully completed. We have now {1,number,integer} computer(s)", new Object[] { f.displayName, 

                      
                      Integer.valueOf(jenkins.getComputers().length) });
                fireOnCommit(f, orig);
              } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Provisioned agent " + f.displayName + " failed to launch", repl);
                fireOnRollback(f, orig, repl);
              } 
            } 
            do {
              orig = (List)this.pendingLaunches.get();
              repl = new ArrayList<PlannedNode>(orig);
              changed = false;
              for (Iterator<PlannedNode> iterator = repl.iterator(); iterator.hasNext(); ) {
                PlannedNode p = (PlannedNode)iterator.next();
                if (p == f) {
                  iterator.remove();
                  changed = true;
                  break;
                } 
              } 
            } while (changed && !this.pendingLaunches.compareAndSet(orig, repl));
          } catch (Error e) {
            throw e;
          } catch (Throwable e) {
            boolean changed;
            List<PlannedNode> repl, orig;
            LOGGER.log(Level.SEVERE, "Unexpected uncaught exception encountered while processing agent " + f.displayName, orig);
            do {
              orig = (List)this.pendingLaunches.get();
              repl = new ArrayList<PlannedNode>(orig);
              changed = false;
              for (Iterator<PlannedNode> iterator = repl.iterator(); iterator.hasNext(); ) {
                PlannedNode p = (PlannedNode)iterator.next();
                if (p == f) {
                  iterator.remove();
                  changed = true;
                  break;
                } 
              } 
            } while (changed && !this.pendingLaunches.compareAndSet(orig, repl));
          } finally {
            boolean changed;
            List<PlannedNode> repl, orig;
            do {
              orig = (List)this.pendingLaunches.get();
              repl = new ArrayList<PlannedNode>(orig);
              changed = false;
              for (Iterator<PlannedNode> iterator = repl.iterator(); iterator.hasNext(); ) {
                PlannedNode p = (PlannedNode)iterator.next();
                if (p == f) {
                  iterator.remove();
                  changed = true;
                  break;
                } 
              } 
            } while (changed && !this.pendingLaunches.compareAndSet(orig, repl));
            f.spent();
          }  
        plannedCapacitySnapshot += f.numExecutors;
      } 
      float plannedCapacity = plannedCapacitySnapshot;
      this.plannedCapacitiesEMA.update(plannedCapacity);
      LoadStatistics.LoadStatisticsSnapshot snapshot = this.stat.computeSnapshot();
      int availableSnapshot = snapshot.getAvailableExecutors();
      int queueLengthSnapshot = snapshot.getQueueLength();
      if (queueLengthSnapshot <= availableSnapshot) {
        LOGGER.log(Level.FINER, "Queue length {0} is less than the available capacity {1}. No provisioning strategy required", new Object[] { Integer.valueOf(queueLengthSnapshot), Integer.valueOf(availableSnapshot) });
        this.provisioningState = null;
      } else {
        this.provisioningState = new StrategyState(this, snapshot, this.label, plannedCapacitySnapshot);
      } 
      if (this.provisioningState != null) {
        ExtensionList extensionList = Jenkins.get().getExtensionList(Strategy.class);
        for (Strategy strategy : extensionList.isEmpty() ? 
          List.of(new StandardStrategyImpl()) : 
          extensionList) {
          LOGGER.log(Level.FINER, "Consulting {0} provisioning strategy with state {1}", new Object[] { strategy, this.provisioningState });
          if (StrategyDecision.PROVISIONING_COMPLETED == strategy.apply(this.provisioningState)) {
            LOGGER.log(Level.FINER, "Provisioning strategy {0} declared provisioning complete", strategy);
            break;
          } 
        } 
      } 
    } finally {
      this.provisioningLock.unlock();
    } 
    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.finer(() -> "ran update on " + this.label + " in " + (System.nanoTime() - start) / 1000000L + "ms"); 
  }
  
  private static final Logger LOGGER = Logger.getLogger(NodeProvisioner.class.getName());
  
  private static final float MARGIN = SystemProperties.getInteger(NodeProvisioner.class.getName() + ".MARGIN", Integer.valueOf(10)).intValue() / 100.0F;
  
  private static final float MARGIN0 = Math.max(MARGIN, getFloatSystemProperty(NodeProvisioner.class.getName() + ".MARGIN0", 0.5F));
  
  private static final float MARGIN_DECAY = getFloatSystemProperty(NodeProvisioner.class.getName() + ".MARGIN_DECAY", 0.5F);
  
  private static final MultiStageTimeSeries.TimeScale TIME_SCALE = MultiStageTimeSeries.TimeScale.SEC10;
  
  private static float getFloatSystemProperty(String propName, float defaultValue) {
    String v = SystemProperties.getString(propName);
    if (v != null)
      try {
        return Float.parseFloat(v);
      } catch (NumberFormatException e) {
        LOGGER.warning("Failed to parse a float value from system property " + propName + ". value was " + v);
      }  
    return defaultValue;
  }
  
  private static void fireOnFailure(PlannedNode plannedNode, Throwable cause) {
    for (CloudProvisioningListener cl : CloudProvisioningListener.all()) {
      try {
        cl.onFailure(plannedNode, cause);
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Unexpected uncaught exception encountered while processing onFailure() listener call in " + cl + " for agent " + plannedNode.displayName, e);
      } 
    } 
  }
  
  private static void fireOnRollback(PlannedNode plannedNode, Node newNode, Throwable cause) {
    for (CloudProvisioningListener cl : CloudProvisioningListener.all()) {
      try {
        cl.onRollback(plannedNode, newNode, cause);
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Unexpected uncaught exception encountered while processing onRollback() listener call in " + cl + " for agent " + newNode
            
            .getDisplayName(), e);
      } 
    } 
  }
  
  private static void fireOnComplete(PlannedNode plannedNode, Node newNode) {
    for (CloudProvisioningListener cl : CloudProvisioningListener.all()) {
      try {
        cl.onComplete(plannedNode, newNode);
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Unexpected uncaught exception encountered while processing onComplete() listener call in " + cl + " for agent " + plannedNode.displayName, e);
      } 
    } 
  }
  
  private static void fireOnCommit(PlannedNode plannedNode, Node newNode) {
    for (CloudProvisioningListener cl : CloudProvisioningListener.all()) {
      try {
        cl.onCommit(plannedNode, newNode);
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Unexpected uncaught exception encountered while processing onCommit() listener call in " + cl + " for agent " + newNode
            
            .getDisplayName(), e);
      } 
    } 
  }
  
  private static void fireOnStarted(Cloud cloud, Label label, Collection<PlannedNode> plannedNodes) {
    for (CloudProvisioningListener cl : CloudProvisioningListener.all()) {
      try {
        cl.onStarted(cloud, label, plannedNodes);
      } catch (Error e) {
        throw e;
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Unexpected uncaught exception encountered while processing onStarted() listener call in " + cl + " for label " + label
            
            .toString(), e);
      } 
    } 
  }
}
