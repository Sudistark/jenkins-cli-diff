package hudson.init;

import org.jvnet.hudson.reactor.Executable;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;

public static enum InitMilestone implements Milestone {
  STARTED("Started initialization"),
  PLUGINS_LISTED("Listed all plugins"),
  PLUGINS_PREPARED("Prepared all plugins"),
  PLUGINS_STARTED("Started all plugins"),
  EXTENSIONS_AUGMENTED("Augmented all extensions"),
  SYSTEM_CONFIG_LOADED("System config loaded"),
  SYSTEM_CONFIG_ADAPTED("System config adapted"),
  JOB_LOADED("Loaded all jobs"),
  JOB_CONFIG_ADAPTED("Configuration for all jobs updated"),
  COMPLETED("Completed initialization");
  
  private final String message;
  
  InitMilestone(String message) { this.message = message; }
  
  public static TaskBuilder ordering() {
    b = new TaskGraphBuilder();
    InitMilestone[] v = values();
    for (int i = 0; i < v.length - 1; i++)
      b.add(null, Executable.NOOP).requires(v[i]).attains(v[i + 1]); 
    return b;
  }
  
  public String toString() { return this.message; }
}
