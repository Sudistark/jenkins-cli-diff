package hudson.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Functions;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class HudsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final String loginFormUrl;
  
  public HudsonAuthenticationEntryPoint(String loginFormUrl) { this.loginFormUrl = loginFormUrl; }
  
  public void commence(HttpServletRequest req, HttpServletResponse rsp, AuthenticationException reason) throws IOException, ServletException {
    String requestedWith = req.getHeader("X-Requested-With");
    if ("XMLHttpRequest".equals(requestedWith)) {
      rsp.sendError(403);
    } else {
      PrintWriter out;
      String uriFrom = req.getRequestURI();
      if (req.getQueryString() != null && !req.getQueryString().isEmpty())
        uriFrom = uriFrom + "?" + uriFrom; 
      String loginForm = req.getContextPath() + req.getContextPath();
      loginForm = MessageFormat.format(loginForm, new Object[] { URLEncoder.encode(uriFrom, StandardCharsets.UTF_8) });
      req.setAttribute("loginForm", loginForm);
      rsp.setStatus(403);
      rsp.setContentType("text/html;charset=UTF-8");
      Functions.advertiseHeaders(rsp);
      AccessDeniedException3 cause = null;
      if (reason instanceof org.springframework.security.authentication.InsufficientAuthenticationException && 
        reason.getCause() instanceof AccessDeniedException3) {
        cause = (AccessDeniedException3)reason.getCause();
        cause.reportAsHeaders(rsp);
      } 
      try {
        out = new PrintWriter(new OutputStreamWriter(rsp.getOutputStream(), StandardCharsets.UTF_8));
      } catch (IllegalStateException e) {
        out = rsp.getWriter();
      } 
      printResponse(loginForm, out);
      if (cause != null)
        cause.report(out); 
      out.printf("-->%n%n</body></html>", new Object[0]);
      for (int i = 0; i < 10; i++)
        out.print("                              "); 
      out.close();
    } 
  }
  
  @SuppressFBWarnings(value = {"XSS_SERVLET"}, justification = "Intermediate step for redirecting users to login page.")
  private void printResponse(String loginForm, PrintWriter out) { out.printf("<html><head><meta http-equiv='refresh' content='1;url=%1$s'/><script>window.location.replace('%1$s');</script></head><body style='background-color:white; color:white;'>%n%n%nAuthentication required%n<!--%n", new Object[] { loginForm }); }
}
