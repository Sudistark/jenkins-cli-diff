package hudson.init;

import org.jvnet.hudson.reactor.Executable;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;

public static enum TermMilestone implements Milestone {
  STARTED("Started termination"),
  COMPLETED("Completed termination");
  
  private final String message;
  
  TermMilestone(String message) { this.message = message; }
  
  public static TaskBuilder ordering() {
    b = new TaskGraphBuilder();
    TermMilestone[] v = values();
    for (int i = 0; i < v.length - 1; i++)
      b.add(null, Executable.NOOP).requires(v[i]).attains(v[i + 1]); 
    return b;
  }
  
  public String toString() { return this.message; }
}
