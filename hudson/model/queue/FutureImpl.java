package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Executor;
import hudson.model.Queue;
import hudson.remoting.AsyncFutureImpl;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import jenkins.model.Jenkins;

public final class FutureImpl extends AsyncFutureImpl<Queue.Executable> implements QueueTaskFuture<Queue.Executable> {
  private final Queue.Task task;
  
  private final Set<Executor> executors;
  
  final AsyncFutureImpl<Queue.Executable> start;
  
  public FutureImpl(Queue.Task task) {
    this.executors = new HashSet();
    this.start = new AsyncFutureImpl();
    this.task = task;
  }
  
  public Future<Queue.Executable> getStartCondition() { return this.start; }
  
  public Queue.Executable waitForStart() throws InterruptedException, ExecutionException { return (Queue.Executable)getStartCondition().get(); }
  
  public boolean cancel(boolean mayInterruptIfRunning) {
    Queue q = Jenkins.get().getQueue();
    synchronized (this) {
      if (!this.executors.isEmpty()) {
        if (mayInterruptIfRunning)
          for (Executor e : this.executors)
            e.interrupt();  
        return mayInterruptIfRunning;
      } 
      return q.cancel(this.task);
    } 
  }
  
  public void setAsCancelled() {
    super.setAsCancelled();
    if (!this.start.isDone())
      this.start.setAsCancelled(); 
  }
  
  void addExecutor(@NonNull Executor executor) { this.executors.add(executor); }
}
