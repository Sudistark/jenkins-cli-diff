package hudson.model;

import hudson.model.queue.SubTask;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.export.Exported;

public class OverallLoadStatistics extends LoadStatistics {
  @Exported
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public final MultiStageTimeSeries totalQueueLength = this.queueLength;
  
  public OverallLoadStatistics() { super(0, 0); }
  
  public int computeIdleExecutors() { return (new ComputerSet()).getIdleExecutors(); }
  
  public int computeTotalExecutors() { return (new ComputerSet()).getTotalExecutors(); }
  
  public int computeQueueLength() { return Jenkins.get().getQueue().countBuildableItems(); }
  
  protected Iterable<Node> getNodes() { return Jenkins.get().getNodes(); }
  
  protected boolean matches(Queue.Item item, SubTask subTask) { return true; }
  
  protected MultiStageTimeSeries.TrendChart createOverallTrendChart(MultiStageTimeSeries.TimeScale timeScale) { return MultiStageTimeSeries.createTrendChart(timeScale, new MultiStageTimeSeries[] { this.busyExecutors, this.onlineExecutors, this.queueLength, this.availableExecutors }); }
}
