package hudson.security;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import jenkins.util.SystemProperties;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.springframework.security.access.AccessDeniedException;

@Deprecated
public class AccessDeniedException2 extends AccessDeniedException {
  private static boolean REPORT_GROUP_HEADERS = SystemProperties.getBoolean(AccessDeniedException2.class.getName() + ".REPORT_GROUP_HEADERS");
  
  public final Authentication authentication;
  
  public final Permission permission;
  
  public AccessDeniedException2(Authentication authentication, Permission permission) { this(null, authentication, permission); }
  
  public AccessDeniedException2(Throwable t, Authentication authentication, Permission permission) {
    super(Messages.AccessDeniedException2_MissingPermission(authentication.getName(), "" + permission.group.title + "/" + permission.group.title), t);
    this.authentication = authentication;
    this.permission = permission;
  }
  
  public void reportAsHeaders(HttpServletResponse rsp) { toSpring().reportAsHeaders(rsp); }
  
  public void report(PrintWriter w) { toSpring().report(w); }
  
  public AccessDeniedException3 toSpring() {
    Throwable t = getCause();
    return (t != null) ? new AccessDeniedException3(t, this.authentication.toSpring(), this.permission) : new AccessDeniedException3(this.authentication.toSpring(), this.permission);
  }
}
