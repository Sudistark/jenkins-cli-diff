package jenkins.widgets;

import hudson.model.Run;
import java.util.ArrayList;
import java.util.List;
import jenkins.util.ProgressiveRendering;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public abstract class RunListProgressiveRendering extends ProgressiveRendering {
  private static final double MAX_LIKELY_RUNS = 20.0D;
  
  private final List<JSONObject> results = new ArrayList();
  
  private Iterable<? extends Run<?, ?>> builds;
  
  public void setBuilds(Iterable<? extends Run<?, ?>> builds) { this.builds = builds; }
  
  protected void compute() {
    double decay = 1.0D;
    for (Run<?, ?> build : this.builds) {
      if (canceled())
        return; 
      JSONObject element = new JSONObject();
      calculate(build, element);
      synchronized (this) {
        this.results.add(element);
      } 
      decay *= 0.95D;
      progress(1.0D - decay);
    } 
  }
  
  protected JSON data() {
    JSONArray d = JSONArray.fromObject(this.results);
    this.results.clear();
    return d;
  }
  
  protected abstract void calculate(Run<?, ?> paramRun, JSONObject paramJSONObject);
}
