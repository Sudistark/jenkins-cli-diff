package hudson.security;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import jenkins.util.SystemProperties;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class AccessDeniedException3 extends AccessDeniedException {
  private static boolean REPORT_GROUP_HEADERS = SystemProperties.getBoolean(AccessDeniedException2.class.getName() + ".REPORT_GROUP_HEADERS");
  
  public final Authentication authentication;
  
  public final Permission permission;
  
  public AccessDeniedException3(Authentication authentication, Permission permission) { this(null, authentication, permission); }
  
  public AccessDeniedException3(Throwable t, Authentication authentication, Permission permission) {
    super(Messages.AccessDeniedException2_MissingPermission(authentication.getName(), "" + permission.group.title + "/" + permission.group.title), t);
    this.authentication = authentication;
    this.permission = permission;
  }
  
  public void reportAsHeaders(HttpServletResponse rsp) {
    rsp.addHeader("X-You-Are-Authenticated-As", this.authentication.getName());
    if (REPORT_GROUP_HEADERS) {
      for (GrantedAuthority auth : this.authentication.getAuthorities())
        rsp.addHeader("X-You-Are-In-Group", auth.getAuthority()); 
    } else {
      rsp.addHeader("X-You-Are-In-Group-Disabled", "JENKINS-39402: use -Dhudson.security.AccessDeniedException2.REPORT_GROUP_HEADERS=true or use /whoAmI to diagnose");
    } 
    rsp.addHeader("X-Required-Permission", this.permission.getId());
    for (Permission p = this.permission.impliedBy; p != null; p = p.impliedBy)
      rsp.addHeader("X-Permission-Implied-By", p.getId()); 
  }
  
  public void report(PrintWriter w) {
    w.println("You are authenticated as: " + this.authentication.getName());
    w.println("Groups that you are in:");
    for (GrantedAuthority auth : this.authentication.getAuthorities())
      w.println("  " + auth.getAuthority()); 
    w.println("Permission you need to have (but didn't): " + this.permission.getId());
    for (Permission p = this.permission.impliedBy; p != null; p = p.impliedBy)
      w.println(" ... which is implied by: " + p.getId()); 
  }
}
