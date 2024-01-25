package hudson.model;

public abstract class InvisibleAction implements Action {
  public final String getIconFileName() { return null; }
  
  public final String getDisplayName() { return null; }
  
  public String getUrlName() { return null; }
}
