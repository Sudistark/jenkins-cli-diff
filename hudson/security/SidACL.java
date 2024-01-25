package hudson.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.acegisecurity.acls.sid.GrantedAuthoritySid;
import org.acegisecurity.acls.sid.PrincipalSid;
import org.acegisecurity.acls.sid.Sid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public abstract class SidACL extends ACL {
  public boolean hasPermission2(@NonNull Authentication a, Permission permission) {
    if (a.equals(SYSTEM2)) {
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.fine("hasPermission(" + a + "," + permission + ")=>SYSTEM user has full access"); 
      return true;
    } 
    Boolean b = _hasPermission(a, permission);
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("hasPermission(" + a + "," + permission + ")=>" + ((b == null) ? "null, thus false" : b)); 
    if (b == null)
      b = Boolean.valueOf(false); 
    return b.booleanValue();
  }
  
  @SuppressFBWarnings(value = {"NP_BOOLEAN_RETURN_NULL"}, justification = "converting this to YesNoMaybe would break backward compatibility")
  protected Boolean _hasPermission(@NonNull Authentication a, Permission permission) {
    Boolean b = hasPermission(new PrincipalSid(a), permission);
    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.finer("hasPermission(PrincipalSID:" + a.getPrincipal() + "," + permission + ")=>" + b); 
    if (b != null)
      return b; 
    for (GrantedAuthority ga : a.getAuthorities()) {
      b = hasPermission(new GrantedAuthoritySid(ga), permission);
      if (LOGGER.isLoggable(Level.FINER))
        LOGGER.finer("hasPermission(GroupSID:" + ga.getAuthority() + "," + permission + ")=>" + b); 
      if (b != null)
        return b; 
    } 
    for (Sid sid : AUTOMATIC_SIDS) {
      b = hasPermission(sid, permission);
      if (LOGGER.isLoggable(Level.FINER))
        LOGGER.finer("hasPermission(" + sid + "," + permission + ")=>" + b); 
      if (b != null)
        return b; 
    } 
    return null;
  }
  
  protected abstract Boolean hasPermission(Sid paramSid, Permission paramPermission);
  
  protected String toString(Sid p) {
    if (p instanceof GrantedAuthoritySid)
      return ((GrantedAuthoritySid)p).getGrantedAuthority(); 
    if (p instanceof PrincipalSid)
      return ((PrincipalSid)p).getPrincipal(); 
    if (p == EVERYONE)
      return "role_everyone"; 
    return p.toString();
  }
  
  public final SidACL newInheritingACL(SidACL parent) {
    SidACL child = this;
    return new Object(this, child, parent);
  }
  
  private static final Logger LOGGER = Logger.getLogger(SidACL.class.getName());
}
