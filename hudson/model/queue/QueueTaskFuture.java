package hudson.model.queue;

import hudson.model.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface QueueTaskFuture<R extends Queue.Executable> extends Future<R> {
  Future<R> getStartCondition();
  
  R waitForStart() throws InterruptedException, ExecutionException;
}
