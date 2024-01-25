package hudson.model;

import hudson.util.PersistedList;

public class PaneStatusProperties extends UserProperty implements Saveable {
  private final PersistedList<String> collapsed = new PersistedList(this);
  
  private static final PaneStatusProperties FALLBACK = new PaneStatusPropertiesSessionFallback();
  
  public boolean isCollapsed(String paneId) { return this.collapsed.contains(paneId); }
  
  public boolean toggleCollapsed(String paneId) {
    if (this.collapsed.contains(paneId)) {
      this.collapsed.remove(paneId);
      return false;
    } 
    this.collapsed.add(paneId);
    return true;
  }
  
  public void save() { this.user.save(); }
  
  private Object readResolve() {
    this.collapsed.setOwner(this);
    return this;
  }
  
  public static PaneStatusProperties forCurrentUser() {
    current = User.current();
    if (current == null)
      return FALLBACK; 
    return (PaneStatusProperties)current.getProperty(PaneStatusProperties.class);
  }
}
