package hudson.scm;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Action;

public abstract class SCMRevisionState implements Action {
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return null; }
  
  public String getUrlName() { return null; }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "used in several plugins")
  public static SCMRevisionState NONE = new None();
}
