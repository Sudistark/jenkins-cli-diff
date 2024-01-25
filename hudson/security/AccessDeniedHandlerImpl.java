package hudson.security;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.WebApp;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
  public void handle(HttpServletRequest req, HttpServletResponse rsp, AccessDeniedException cause) throws IOException, ServletException {
    rsp.setStatus(403);
    req.setAttribute("exception", cause);
    if (cause instanceof AccessDeniedException3)
      ((AccessDeniedException3)cause).reportAsHeaders(rsp); 
    WebApp.get((Jenkins.get()).servletContext).getSomeStapler()
      .invoke(req, rsp, Jenkins.get(), "/accessDenied");
  }
}
