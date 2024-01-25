package hudson.model;

import java.util.Locale;
import org.jvnet.localizer.Localizable;

public static enum ResultTrend {
  FIXED(Messages._ResultTrend_Fixed()),
  SUCCESS(Messages._ResultTrend_Success()),
  NOW_UNSTABLE(Messages._ResultTrend_NowUnstable()),
  STILL_UNSTABLE(Messages._ResultTrend_StillUnstable()),
  UNSTABLE(Messages._ResultTrend_Unstable()),
  STILL_FAILING(Messages._ResultTrend_StillFailing()),
  FAILURE(Messages._ResultTrend_Failure()),
  ABORTED(Messages._ResultTrend_Aborted()),
  NOT_BUILT(Messages._ResultTrend_NotBuilt());
  
  private final Localizable description;
  
  ResultTrend(Localizable description) { this.description = description; }
  
  public String getDescription() { return this.description.toString(); }
  
  public String getID() { return this.description.toString(Locale.ENGLISH).toUpperCase(Locale.ENGLISH); }
  
  public static ResultTrend getResultTrend(AbstractBuild<?, ?> build) { return getResultTrend(build); }
  
  public static ResultTrend getResultTrend(Run<?, ?> run) {
    Result result = run.getResult();
    if (result == Result.ABORTED)
      return ABORTED; 
    if (result == Result.NOT_BUILT)
      return NOT_BUILT; 
    if (result == Result.SUCCESS) {
      if (isFix(run))
        return FIXED; 
      return SUCCESS;
    } 
    Run<?, ?> previousBuild = getPreviousNonAbortedBuild(run);
    if (result == Result.UNSTABLE) {
      if (previousBuild == null)
        return UNSTABLE; 
      if (previousBuild.getResult() == Result.UNSTABLE)
        return STILL_UNSTABLE; 
      if (previousBuild.getResult() == Result.FAILURE)
        return NOW_UNSTABLE; 
      return UNSTABLE;
    } 
    if (result == Result.FAILURE) {
      if (previousBuild != null && previousBuild.getResult() == Result.FAILURE)
        return STILL_FAILING; 
      return FAILURE;
    } 
    throw new IllegalArgumentException("Unknown result: '" + result + "' for build: " + run);
  }
  
  private static Run<?, ?> getPreviousNonAbortedBuild(Run<?, ?> build) {
    Run<?, ?> previousBuild = build.getPreviousBuild();
    while (previousBuild != null) {
      if (previousBuild.getResult() == null || previousBuild
        .getResult() == Result.ABORTED || previousBuild
        .getResult() == Result.NOT_BUILT) {
        previousBuild = previousBuild.getPreviousBuild();
        continue;
      } 
      return previousBuild;
    } 
    return previousBuild;
  }
  
  private static boolean isFix(Run<?, ?> build) {
    if (build.getResult() != Result.SUCCESS)
      return false; 
    Run<?, ?> previousBuild = getPreviousNonAbortedBuild(build);
    if (previousBuild != null)
      return previousBuild.getResult().isWorseThan(Result.SUCCESS); 
    return false;
  }
}
