package hudson.model.queue;

public final class FutureLoad {
  public final long startTime;
  
  public final int numExecutors;
  
  public final long duration;
  
  public FutureLoad(long startTime, long duration, int numExecutors) {
    this.startTime = startTime;
    this.numExecutors = numExecutors;
    this.duration = duration;
  }
  
  public String toString() { return "startTime=" + this.startTime + ",#executors=" + this.numExecutors + ",duration=" + this.duration; }
}
