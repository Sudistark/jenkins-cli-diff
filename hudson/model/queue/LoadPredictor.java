package hudson.model.queue;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Computer;
import java.util.Collections;

public abstract class LoadPredictor implements ExtensionPoint {
  public Iterable<FutureLoad> predict(MappingWorksheet plan, Computer computer, long start, long end) { return predict(computer, start, end); }
  
  @Deprecated
  public Iterable<FutureLoad> predict(Computer computer, long start, long end) { return Collections.emptyList(); }
  
  public static ExtensionList<LoadPredictor> all() { return ExtensionList.lookup(LoadPredictor.class); }
}
