package hudson.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.queue.SubTask;
import hudson.util.ColorPalette;
import hudson.util.NoOverlapCategoryAxis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class LoadStatistics {
  private final boolean modern;
  
  @Exported
  public final MultiStageTimeSeries definedExecutors;
  
  @Exported
  public final MultiStageTimeSeries onlineExecutors;
  
  @Exported
  public final MultiStageTimeSeries connectingExecutors;
  
  @Exported
  public final MultiStageTimeSeries busyExecutors;
  
  @Exported
  public final MultiStageTimeSeries idleExecutors;
  
  @Exported
  public final MultiStageTimeSeries availableExecutors;
  
  @Exported
  @Deprecated
  public final MultiStageTimeSeries totalExecutors;
  
  @Exported
  public final MultiStageTimeSeries queueLength;
  
  protected LoadStatistics(int initialOnlineExecutors, int initialBusyExecutors) {
    this.definedExecutors = new MultiStageTimeSeries(Messages._LoadStatistics_Legends_DefinedExecutors(), ColorPalette.YELLOW, initialOnlineExecutors, DECAY);
    this
      .onlineExecutors = new MultiStageTimeSeries(Messages._LoadStatistics_Legends_OnlineExecutors(), ColorPalette.BLUE, initialOnlineExecutors, DECAY);
    this.connectingExecutors = new MultiStageTimeSeries(Messages._LoadStatistics_Legends_ConnectingExecutors(), ColorPalette.YELLOW, 0.0F, DECAY);
    this
      .busyExecutors = new MultiStageTimeSeries(Messages._LoadStatistics_Legends_BusyExecutors(), ColorPalette.RED, initialBusyExecutors, DECAY);
    this.idleExecutors = new MultiStageTimeSeries(Messages._LoadStatistics_Legends_IdleExecutors(), ColorPalette.YELLOW, (initialOnlineExecutors - initialBusyExecutors), DECAY);
    this.availableExecutors = new MultiStageTimeSeries(Messages._LoadStatistics_Legends_AvailableExecutors(), ColorPalette.YELLOW, (initialOnlineExecutors - initialBusyExecutors), DECAY);
    this
      .queueLength = new MultiStageTimeSeries(Messages._LoadStatistics_Legends_QueueLength(), ColorPalette.GREY, 0.0F, DECAY);
    this.totalExecutors = this.onlineExecutors;
    this.modern = isModern(getClass());
  }
  
  static boolean isModern(Class<? extends LoadStatistics> clazz) {
    boolean hasGetNodes = false;
    boolean hasMatches = false;
    while (clazz != LoadStatistics.class && clazz != null && (!hasGetNodes || !hasMatches)) {
      if (!hasGetNodes)
        try {
          Method getNodes = clazz.getDeclaredMethod("getNodes", new Class[0]);
          hasGetNodes = !Modifier.isAbstract(getNodes.getModifiers());
        } catch (NoSuchMethodException noSuchMethodException) {} 
      if (!hasMatches)
        try {
          Method getNodes = clazz.getDeclaredMethod("matches", new Class[] { Queue.Item.class, SubTask.class });
          hasMatches = !Modifier.isAbstract(getNodes.getModifiers());
        } catch (NoSuchMethodException noSuchMethodException) {} 
      if ((!hasGetNodes || !hasMatches) && LoadStatistics.class.isAssignableFrom(clazz.getSuperclass()))
        clazz = clazz.getSuperclass(); 
    } 
    return (hasGetNodes && hasMatches);
  }
  
  @Deprecated
  public float getLatestIdleExecutors(MultiStageTimeSeries.TimeScale timeScale) { return this.idleExecutors.pick(timeScale).getLatest(); }
  
  @Deprecated
  public abstract int computeIdleExecutors();
  
  @Deprecated
  public abstract int computeTotalExecutors();
  
  @Deprecated
  public abstract int computeQueueLength();
  
  public JFreeChart createChart(CategoryDataset ds) {
    JFreeChart chart = ChartFactory.createLineChart(null, null, null, ds, PlotOrientation.VERTICAL, true, true, false);
    chart.setBackgroundPaint(Color.white);
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(Color.WHITE);
    plot.setOutlinePaint(null);
    plot.setRangeGridlinesVisible(true);
    plot.setRangeGridlinePaint(Color.black);
    LineAndShapeRenderer renderer = (LineAndShapeRenderer)plot.getRenderer();
    renderer.setBaseStroke(new BasicStroke(3.0F));
    configureRenderer(renderer);
    NoOverlapCategoryAxis noOverlapCategoryAxis = new NoOverlapCategoryAxis(null);
    plot.setDomainAxis(noOverlapCategoryAxis);
    noOverlapCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
    noOverlapCategoryAxis.setLowerMargin(0.0D);
    noOverlapCategoryAxis.setUpperMargin(0.0D);
    noOverlapCategoryAxis.setCategoryMargin(0.0D);
    NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    plot.setInsets(new RectangleInsets(0.0D, 0.0D, 0.0D, 5.0D));
    return chart;
  }
  
  protected void configureRenderer(LineAndShapeRenderer renderer) {
    renderer.setSeriesPaint(0, ColorPalette.BLUE);
    renderer.setSeriesPaint(1, ColorPalette.RED);
    renderer.setSeriesPaint(2, ColorPalette.GREY);
    renderer.setSeriesPaint(3, ColorPalette.YELLOW);
  }
  
  public MultiStageTimeSeries.TrendChart createTrendChart(MultiStageTimeSeries.TimeScale timeScale) { return MultiStageTimeSeries.createTrendChart(timeScale, new MultiStageTimeSeries[] { this.onlineExecutors, this.busyExecutors, this.queueLength, this.availableExecutors }); }
  
  public MultiStageTimeSeries.TrendChart doGraph(@QueryParameter String type) throws IOException { return createTrendChart(MultiStageTimeSeries.TimeScale.parse(type)); }
  
  public Api getApi() { return new Api(this); }
  
  @Deprecated
  protected void updateExecutorCounts() { updateCounts(computeSnapshot()); }
  
  protected void updateCounts(LoadStatisticsSnapshot current) {
    this.definedExecutors.update(current.getDefinedExecutors());
    this.onlineExecutors.update(current.getOnlineExecutors());
    this.connectingExecutors.update(current.getConnectingExecutors());
    this.busyExecutors.update(current.getBusyExecutors());
    this.idleExecutors.update(current.getIdleExecutors());
    this.availableExecutors.update(current.getAvailableExecutors());
    this.queueLength.update(current.getQueueLength());
  }
  
  protected abstract Iterable<Node> getNodes();
  
  protected abstract boolean matches(Queue.Item paramItem, SubTask paramSubTask);
  
  public LoadStatisticsSnapshot computeSnapshot() {
    if (this.modern)
      return computeSnapshot(Jenkins.get().getQueue().getBuildableItems()); 
    int t = computeTotalExecutors();
    int i = computeIdleExecutors();
    return new LoadStatisticsSnapshot(t, t, Math.max(i - t, 0), Math.max(t - i, 0), i, i, computeQueueLength());
  }
  
  protected LoadStatisticsSnapshot computeSnapshot(Iterable<Queue.BuildableItem> queue) {
    LoadStatisticsSnapshot.Builder builder = LoadStatisticsSnapshot.builder();
    Iterable<Node> nodes = getNodes();
    if (nodes != null)
      for (Node node : nodes)
        builder.with(node);  
    int q = 0;
    if (queue != null)
      for (Queue.BuildableItem item : queue) {
        for (SubTask st : item.task.getSubTasks()) {
          if (matches(item, st))
            q++; 
        } 
      }  
    return builder.withQueueLength(q).build();
  }
  
  public static final float DECAY = Float.parseFloat(SystemProperties.getString(LoadStatistics.class.getName() + ".decay", "0.9"));
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static int CLOCK = SystemProperties.getInteger(LoadStatistics.class.getName() + ".clock", Integer.valueOf((int)TimeUnit.SECONDS.toMillis(10L))).intValue();
}
