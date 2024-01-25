package hudson.model;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class MultiStageTimeSeries implements Serializable {
  public final Localizable title;
  
  public final Color color;
  
  @Exported
  public final TimeSeries sec10;
  
  @Exported
  public final TimeSeries min;
  
  @Exported
  public final TimeSeries hour;
  
  private int counter;
  
  private static final Font CHART_FONT = Font.getFont(MultiStageTimeSeries.class.getName() + ".chartFont", new Font("SansSerif", 0, 10));
  
  private static final long serialVersionUID = 1L;
  
  public MultiStageTimeSeries(Localizable title, Color color, float initialValue, float decay) {
    this.title = title;
    this.color = color;
    this.sec10 = new TimeSeries(initialValue, decay, 6 * (int)TimeUnit.HOURS.toMinutes(6L));
    this.min = new TimeSeries(initialValue, decay, (int)TimeUnit.DAYS.toMinutes(2L));
    this.hour = new TimeSeries(initialValue, decay, (int)TimeUnit.DAYS.toHours(56L));
  }
  
  @Deprecated
  public MultiStageTimeSeries(float initialValue, float decay) { this(Messages._MultiStageTimeSeries_EMPTY_STRING(), Color.WHITE, initialValue, decay); }
  
  public void update(float f) {
    this.counter = (this.counter + 1) % 360;
    this.sec10.update(f);
    if (this.counter % 6 == 0)
      this.min.update(f); 
    if (this.counter == 0)
      this.hour.update(f); 
  }
  
  public TimeSeries pick(TimeScale timeScale) {
    switch (null.$SwitchMap$hudson$model$MultiStageTimeSeries$TimeScale[timeScale.ordinal()]) {
      case 1:
        return this.hour;
      case 2:
        return this.min;
      case 3:
        return this.sec10;
    } 
    throw new AssertionError();
  }
  
  public float getLatest(TimeScale timeScale) { return pick(timeScale).getLatest(); }
  
  public Api getApi() { return new Api(this); }
  
  public static TrendChart createTrendChart(TimeScale scale, MultiStageTimeSeries... data) { return new TrendChart(scale, data); }
}
