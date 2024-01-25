package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Executor;
import hudson.model.Queue;
import hudson.model.Run;
import org.kohsuke.accmod.Restricted;

public final class WorkUnit {
  public final SubTask work;
  
  public final WorkUnitContext context;
  
  private Queue.Executable executable;
  
  WorkUnit(WorkUnitContext context, SubTask work) {
    this.context = context;
    this.work = work;
  }
  
  @CheckForNull
  public Executor getExecutor() { return this.executor; }
  
  public void setExecutor(@CheckForNull Executor e) {
    this.executor = e;
    if (e != null)
      this.context.future.addExecutor(e); 
  }
  
  @CheckForNull
  public Queue.Executable getExecutable() { return this.executable; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void setExecutable(Queue.Executable executable) {
    this.executable = executable;
    if (executable instanceof Run)
      ((Run)executable).setQueueId(this.context.item.getId()); 
  }
  
  public boolean isMainWork() { return (this.context.task == this.work); }
  
  public String toString() {
    if (this.work == this.context.task)
      return super.toString() + "[work=" + super.toString() + "]"; 
    return super.toString() + "[work=" + super.toString() + ",context.task=" + this.work + "]";
  }
}
