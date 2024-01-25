package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.acegisecurity.Authentication;
import org.springframework.security.core.Authentication;

public abstract class BasicHeaderAuthenticator implements ExtensionPoint {
  @CheckForNull
  public Authentication authenticate2(HttpServletRequest req, HttpServletResponse rsp, String username, String password) throws IOException, ServletException {
    if (Util.isOverridden(BasicHeaderAuthenticator.class, getClass(), "authenticate", new Class[] { HttpServletRequest.class, HttpServletResponse.class, String.class, String.class })) {
      Authentication a = authenticate(req, rsp, username, password);
      return (a != null) ? a.toSpring() : null;
    } 
    throw new AbstractMethodError("implement authenticate2");
  }
  
  @Deprecated
  @CheckForNull
  public Authentication authenticate(HttpServletRequest req, HttpServletResponse rsp, String username, String password) throws IOException, ServletException {
    Authentication a = authenticate2(req, rsp, username, password);
    return (a != null) ? Authentication.fromSpring(a) : null;
  }
  
  public static ExtensionList<BasicHeaderAuthenticator> all() { return ExtensionList.lookup(BasicHeaderAuthenticator.class); }
}
