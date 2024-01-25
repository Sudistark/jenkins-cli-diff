package hudson.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acegisecurity.acls.sid.Sid;
import org.springframework.security.core.Authentication;

public class SparseACL extends SidACL {
  private final List<Entry> entries;
  
  private ACL parent;
  
  public SparseACL(ACL parent) {
    this.entries = new ArrayList();
    this.parent = parent;
  }
  
  public void add(Entry e) { this.entries.add(e); }
  
  public void add(Sid sid, Permission permission, boolean allowed) { add(new Entry(sid, permission, allowed)); }
  
  public boolean hasPermission2(Authentication a, Permission permission) {
    if (a.equals(SYSTEM2))
      return true; 
    Boolean b = _hasPermission(a, permission);
    if (b != null)
      return b.booleanValue(); 
    if (this.parent != null) {
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.fine("hasPermission(" + a + "," + permission + ") is delegating to parent ACL: " + this.parent); 
      return this.parent.hasPermission2(a, permission);
    } 
    return false;
  }
  
  @SuppressFBWarnings(value = {"NP_BOOLEAN_RETURN_NULL"}, justification = "converting this to YesNoMaybe would break backward compatibility")
  protected Boolean hasPermission(Sid p, Permission permission) {
    for (; permission != null; permission = permission.impliedBy) {
      for (Entry e : this.entries) {
        if (e.permission == permission && e.sid.equals(p))
          return Boolean.valueOf(e.allowed); 
      } 
    } 
    return null;
  }
  
  private static final Logger LOGGER = Logger.getLogger(SparseACL.class.getName());
}
