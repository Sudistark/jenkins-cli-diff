package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public interface Action extends ModelObject {
  @CheckForNull
  String getIconFileName();
  
  @CheckForNull
  String getDisplayName();
  
  @CheckForNull
  String getUrlName();
}
