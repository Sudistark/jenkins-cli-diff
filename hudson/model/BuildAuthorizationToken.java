package hudson.model;

import hudson.Util;
import java.io.IOException;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.security.access.AccessDeniedException;

@Deprecated
public final class BuildAuthorizationToken {
  private final String token;
  
  public BuildAuthorizationToken(String token) { this.token = token; }
  
  public static BuildAuthorizationToken create(StaplerRequest req) {
    if (req.getParameter("pseudoRemoteTrigger") != null) {
      String token = Util.fixEmpty(req.getParameter("authToken"));
      if (token != null)
        return new BuildAuthorizationToken(token); 
    } 
    return null;
  }
  
  @Deprecated
  public static void checkPermission(AbstractProject<?, ?> project, BuildAuthorizationToken token, StaplerRequest req, StaplerResponse rsp) throws IOException { checkPermission(project, token, req, rsp); }
  
  public static void checkPermission(Job<?, ?> project, BuildAuthorizationToken token, StaplerRequest req, StaplerResponse rsp) throws IOException {
    if (token != null && token.token != null) {
      String providedToken = req.getParameter("token");
      if (providedToken != null && providedToken.equals(token.token))
        return; 
      if (providedToken != null)
        throw new AccessDeniedException(Messages.BuildAuthorizationToken_InvalidTokenProvided()); 
    } 
    project.checkPermission(Item.BUILD);
    if (req.getMethod().equals("POST"))
      return; 
    if (req.getAttribute(jenkins.security.ApiTokenProperty.class.getName()) instanceof User)
      return; 
    rsp.setStatus(405);
    rsp.addHeader("Allow", "POST");
    throw HttpResponses.forwardToView(project, "requirePOST.jelly");
  }
  
  public String getToken() { return this.token; }
}
