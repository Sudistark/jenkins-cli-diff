package org.acegisecurity.context;

import org.acegisecurity.Authentication;

@Deprecated
public class SecurityContextImpl implements SecurityContext {
  private Authentication authentication;
  
  public Authentication getAuthentication() { return this.authentication; }
  
  public void setAuthentication(Authentication authentication) { this.authentication = authentication; }
}
