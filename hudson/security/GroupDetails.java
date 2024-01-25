package hudson.security;

import java.util.Set;

public abstract class GroupDetails {
  public abstract String getName();
  
  public String getDisplayName() { return getName(); }
  
  public Set<String> getMembers() { return null; }
}
