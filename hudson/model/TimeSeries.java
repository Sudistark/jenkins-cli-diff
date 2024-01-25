package hudson.model;

import java.io.Serializable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public final class TimeSeries implements Serializable {
  private final float decay;
  
  private final int historySize;
  
  private static final long serialVersionUID = 1L;
  
  public TimeSeries(float initialValue, float decay, int historySize) {
    this.history = new float[] { initialValue };
    this.decay = decay;
    this.historySize = historySize;
  }
  
  public void update(float newData) {
    float data = this.history[0] * this.decay + newData * (1.0F - this.decay);
    float[] r = new float[Math.min(this.history.length + 1, this.historySize)];
    System.arraycopy(this.history, 0, r, 1, Math.min(this.history.length, r.length - 1));
    r[0] = data;
    this.history = r;
  }
  
  @Exported
  public float[] getHistory() { return this.history; }
  
  @Exported
  public float getLatest() { return this.history[0]; }
  
  public String toString() { return Float.toString(this.history[0]); }
}
