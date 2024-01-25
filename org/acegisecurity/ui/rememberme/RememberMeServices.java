package org.acegisecurity.ui.rememberme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.acegisecurity.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;

@Deprecated
public interface RememberMeServices {
  Authentication autoLogin(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse);
  
  void loginFail(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse);
  
  void loginSuccess(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, Authentication paramAuthentication);
  
  static RememberMeServices fromSpring(RememberMeServices rms) {
    if (rms instanceof RememberMeServicesSpringImpl)
      return ((RememberMeServicesSpringImpl)rms).delegate; 
    return new Object(rms);
  }
  
  default RememberMeServices toSpring() { return new RememberMeServicesSpringImpl(this); }
}
