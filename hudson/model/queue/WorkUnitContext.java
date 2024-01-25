package hudson.model.queue;

import hudson.ExtensionList;
import hudson.model.Action;
import hudson.model.Executor;
import hudson.model.ExecutorListener;
import hudson.model.Queue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

public final class WorkUnitContext {
  private static final Logger LOGGER = Logger.getLogger(WorkUnitContext.class.getName());
  
  public final Queue.BuildableItem item;
  
  public final Queue.Task task;
  
  public final FutureImpl future;
  
  public final List<Action> actions;
  
  private final Latch startLatch;
  
  private final Latch endLatch;
  
  private List<WorkUnit> workUnits;
  
  public WorkUnitContext(Queue.BuildableItem item) {
    this.workUnits = new ArrayList();
    this.item = item;
    this.task = item.task;
    this.future = (FutureImpl)item.getFuture();
    this.actions = new ArrayList(item.getActions());
    int workUnitSize = this.task.getSubTasks().size();
    this.startLatch = new Object(this, workUnitSize);
    this.endLatch = new Latch(workUnitSize);
  }
  
  public WorkUnit createWorkUnit(SubTask execUnit) {
    WorkUnit wu = new WorkUnit(this, execUnit);
    this.workUnits.add(wu);
    return wu;
  }
  
  public List<WorkUnit> getWorkUnits() { return Collections.unmodifiableList(this.workUnits); }
  
  public WorkUnit getPrimaryWorkUnit() { return (WorkUnit)this.workUnits.get(0); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void synchronizeStart() throws InterruptedException {
    try {
      this.startLatch.synchronize();
    } finally {
      Executor e = Executor.currentExecutor();
      WorkUnit wu = e.getCurrentWorkUnit();
      if (wu.isMainWork()) {
        this.future.start.set(e.getCurrentExecutable());
        for (ExecutorListener listener : ExtensionList.lookup(ExecutorListener.class)) {
          try {
            listener.taskStarted(e, this.task);
          } catch (RuntimeException x) {
            LOGGER.log(Level.WARNING, null, x);
          } 
        } 
      } 
    } 
  }
  
  @Deprecated
  public void synchronizeEnd(Queue.Executable executable, Throwable problems, long duration) throws InterruptedException { synchronizeEnd(Executor.currentExecutor(), executable, problems, duration); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void synchronizeEnd(Executor e, Queue.Executable executable, Throwable problems, long duration) throws InterruptedException {
    try {
      this.endLatch.synchronize();
    } finally {
      WorkUnit wu = e.getCurrentWorkUnit();
      if (wu.isMainWork())
        if (problems == null) {
          this.future.set(executable);
          e.getOwner().taskCompleted(e, this.task, duration);
          for (ExecutorListener listener : ExtensionList.lookup(ExecutorListener.class)) {
            try {
              listener.taskCompleted(e, this.task, duration);
            } catch (RuntimeException x) {
              LOGGER.log(Level.WARNING, null, x);
            } 
          } 
        } else {
          this.future.set(problems);
          e.getOwner().taskCompletedWithProblems(e, this.task, duration, problems);
          for (ExecutorListener listener : ExtensionList.lookup(ExecutorListener.class)) {
            try {
              listener.taskCompletedWithProblems(e, this.task, duration, problems);
            } catch (RuntimeException x) {
              LOGGER.log(Level.WARNING, null, x);
            } 
          } 
        }  
    } 
  }
  
  public void abort(Throwable cause) {
    if (cause == null)
      throw new IllegalArgumentException(); 
    if (this.aborted != null)
      return; 
    this.aborted = cause;
    this.startLatch.abort(cause);
    this.endLatch.abort(cause);
    Thread c = Thread.currentThread();
    for (WorkUnit wu : this.workUnits) {
      Executor e = wu.getExecutor();
      if (e != null && e != c)
        e.interrupt(); 
    } 
  }
}
