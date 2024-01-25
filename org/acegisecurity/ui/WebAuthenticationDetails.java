package org.acegisecurity.ui;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;

@Deprecated
public class WebAuthenticationDetails implements Serializable {
  public WebAuthenticationDetails(HttpServletRequest request) {}
}
