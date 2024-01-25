package org.acegisecurity.ui.rememberme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.acegisecurity.Authentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;

final class RememberMeServicesSpringImpl implements RememberMeServices {
  final RememberMeServices delegate;
  
  RememberMeServicesSpringImpl(RememberMeServices delegate) { this.delegate = delegate; }
  
  public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
    Authentication a = this.delegate.autoLogin(request, response);
    return (a != null) ? a.toSpring() : null;
  }
  
  public void loginFail(HttpServletRequest request, HttpServletResponse response) { this.delegate.loginFail(request, response); }
  
  public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) { this.delegate.loginSuccess(request, response, Authentication.fromSpring(successfulAuthentication)); }
}
