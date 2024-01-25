package jenkins.model;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;

@Extension
public class Uptime {
  private long startTime;
  
  public long getStartTime() { return this.startTime; }
  
  public long getUptime() { return System.currentTimeMillis() - this.startTime; }
  
  @Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED)
  public void init() { this.startTime = System.currentTimeMillis(); }
}
