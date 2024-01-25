package hudson.model;

import hudson.ExtensionPoint;

public interface ExecutorListener extends ExtensionPoint {
  default void taskAccepted(Executor executor, Queue.Task task) {}
  
  default void taskStarted(Executor executor, Queue.Task task) {}
  
  default void taskCompleted(Executor executor, Queue.Task task, long durationMS) {}
  
  default void taskCompletedWithProblems(Executor executor, Queue.Task task, long durationMS, Throwable problems) {}
}
