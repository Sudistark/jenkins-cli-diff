package hudson.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class RememberMeServicesProxy implements RememberMeServices {
  public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
    RememberMeServices d = this.delegate;
    if (d != null)
      return d.autoLogin(request, response); 
    return null;
  }
  
  public void loginFail(HttpServletRequest request, HttpServletResponse response) {
    RememberMeServices d = this.delegate;
    if (d != null)
      d.loginFail(request, response); 
  }
  
  public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
    RememberMeServices d = this.delegate;
    if (d != null)
      d.loginSuccess(request, response, successfulAuthentication); 
  }
  
  public void setDelegate(RememberMeServices delegate) { this.delegate = delegate; }
}
