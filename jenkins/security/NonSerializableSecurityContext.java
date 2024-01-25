package jenkins.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@SuppressFBWarnings(value = {"SE_NO_SERIALVERSIONID", "SE_TRANSIENT_FIELD_NOT_RESTORED"}, justification = "It is not intended to be serialized. Default values will be used in case of deserialization")
public class NonSerializableSecurityContext implements SecurityContext {
  private Authentication authentication;
  
  public NonSerializableSecurityContext() {}
  
  public NonSerializableSecurityContext(Authentication authentication) { this.authentication = authentication; }
  
  public boolean equals(Object obj) {
    if (obj instanceof SecurityContext) {
      SecurityContext test = (SecurityContext)obj;
      if (getAuthentication() == null && test.getAuthentication() == null)
        return true; 
      if (getAuthentication() != null && test.getAuthentication() != null && 
        getAuthentication().equals(test.getAuthentication()))
        return true; 
    } 
    return false;
  }
  
  public Authentication getAuthentication() { return this.authentication; }
  
  public int hashCode() {
    if (this.authentication == null)
      return -1; 
    return this.authentication.hashCode();
  }
  
  public void setAuthentication(Authentication authentication) { this.authentication = authentication; }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    if (this.authentication == null) {
      sb.append(": Null authentication");
    } else {
      sb.append(": Authentication: ").append(this.authentication);
    } 
    return sb.toString();
  }
}
