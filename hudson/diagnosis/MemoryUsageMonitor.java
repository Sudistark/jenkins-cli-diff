package hudson.diagnosis;

import hudson.Extension;
import hudson.model.PeriodicWork;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"memoryUsage"})
public final class MemoryUsageMonitor extends PeriodicWork {
  public final MemoryGroup heap;
  
  public final MemoryGroup nonHeap;
  
  public MemoryUsageMonitor() {
    List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
    this.heap = new MemoryGroup(pools, MemoryType.HEAP);
    this.nonHeap = new MemoryGroup(pools, MemoryType.NON_HEAP);
  }
  
  public long getRecurrencePeriod() { return TimeUnit.SECONDS.toMillis(10L); }
  
  protected void doRun() {
    this.heap.update();
    this.nonHeap.update();
  }
}
