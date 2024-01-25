package hudson.slaves;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.PeriodicWork;
import hudson.model.Queue;
import java.util.Map;
import java.util.WeakHashMap;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"computerRetention"})
public class ComputerRetentionWork extends PeriodicWork {
  private final Map<Computer, Long> nextCheck = new WeakHashMap();
  
  public long getRecurrencePeriod() { return 60000L; }
  
  protected void doRun() {
    long startRun = System.currentTimeMillis();
    for (Computer c : Jenkins.get().getComputers())
      Queue.withLock(new Object(this, c, startRun)); 
  }
}
