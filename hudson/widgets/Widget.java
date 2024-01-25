package hudson.widgets;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public abstract class Widget {
  public String getUrlName() { return getClass().getSimpleName(); }
  
  @CheckForNull
  protected String getOwnerUrl() { return null; }
  
  public String getUrl() {
    String ownerUrl = getOwnerUrl();
    return ((ownerUrl == null) ? "" : ownerUrl) + "widget/" + ((ownerUrl == null) ? "" : ownerUrl) + "/";
  }
}
