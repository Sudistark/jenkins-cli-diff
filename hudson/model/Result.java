package hudson.model;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.init.Initializer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.export.CustomExportedBean;

public final class Result implements Serializable, CustomExportedBean {
  @NonNull
  public static final Result SUCCESS = new Result("SUCCESS", BallColor.BLUE, 0, true);
  
  @NonNull
  public static final Result UNSTABLE = new Result("UNSTABLE", BallColor.YELLOW, 1, true);
  
  @NonNull
  public static final Result FAILURE = new Result("FAILURE", BallColor.RED, 2, true);
  
  @NonNull
  public static final Result NOT_BUILT = new Result("NOT_BUILT", BallColor.NOTBUILT, 3, false);
  
  @NonNull
  public static final Result ABORTED = new Result("ABORTED", BallColor.ABORTED, 4, false);
  
  @NonNull
  private final String name;
  
  public final int ordinal;
  
  @NonNull
  public final BallColor color;
  
  public final boolean completeBuild;
  
  private static final long serialVersionUID = 1L;
  
  private Result(@NonNull String name, @NonNull BallColor color, int ordinal, boolean complete) {
    this.name = name;
    this.color = color;
    this.ordinal = ordinal;
    this.completeBuild = complete;
  }
  
  @NonNull
  public Result combine(@NonNull Result that) {
    if (this.ordinal < that.ordinal)
      return that; 
    return this;
  }
  
  public static Result combine(Result r1, Result r2) {
    if (r1 == null)
      return r2; 
    if (r2 == null)
      return r1; 
    return r1.combine(r2);
  }
  
  public boolean isWorseThan(@NonNull Result that) { return (this.ordinal > that.ordinal); }
  
  public boolean isWorseOrEqualTo(@NonNull Result that) { return (this.ordinal >= that.ordinal); }
  
  public boolean isBetterThan(@NonNull Result that) { return (this.ordinal < that.ordinal); }
  
  public boolean isBetterOrEqualTo(@NonNull Result that) { return (this.ordinal <= that.ordinal); }
  
  public boolean isCompleteBuild() { return this.completeBuild; }
  
  @NonNull
  public String toString() { return this.name; }
  
  @NonNull
  public String toExportedObject() { return this.name; }
  
  @NonNull
  public static Result fromString(@NonNull String s) {
    for (Result r : all) {
      if (s.equalsIgnoreCase(r.name))
        return r; 
    } 
    return FAILURE;
  }
  
  @NonNull
  private static List<String> getNames() {
    l = new ArrayList();
    for (Result r : all)
      l.add(r.name); 
    return l;
  }
  
  private Object readResolve() {
    for (Result r : all) {
      if (this.ordinal == r.ordinal)
        return r; 
    } 
    return FAILURE;
  }
  
  private static final Result[] all = { SUCCESS, UNSTABLE, FAILURE, NOT_BUILT, ABORTED };
  
  public static final SingleValueConverter conv = new Object();
  
  @Initializer
  public static void init() { Stapler.CONVERT_UTILS.register(new Object(), Result.class); }
}
