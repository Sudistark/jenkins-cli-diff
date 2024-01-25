package hudson.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.security.access.AccessDeniedException;

@Deprecated
public abstract class FormFieldValidator {
  public static final Permission CHECK = Jenkins.ADMINISTER;
  
  protected final StaplerRequest request;
  
  protected final StaplerResponse response;
  
  protected final Permission permission;
  
  protected final AccessControlled subject;
  
  protected FormFieldValidator(StaplerRequest request, StaplerResponse response, boolean adminOnly) { this(request, response, adminOnly ? Jenkins.get() : null, adminOnly ? CHECK : null); }
  
  @Deprecated
  protected FormFieldValidator(StaplerRequest request, StaplerResponse response, Permission permission) { this(request, response, Jenkins.get(), permission); }
  
  protected FormFieldValidator(Permission permission) { this(Stapler.getCurrentRequest(), Stapler.getCurrentResponse(), permission); }
  
  @Deprecated
  protected FormFieldValidator(StaplerRequest request, StaplerResponse response, AccessControlled subject, Permission permission) {
    this.request = request;
    this.response = response;
    this.subject = subject;
    this.permission = permission;
  }
  
  protected FormFieldValidator(AccessControlled subject, Permission permission) { this(Stapler.getCurrentRequest(), Stapler.getCurrentResponse(), subject, permission); }
  
  public final void process() throws IOException, ServletException {
    if (this.permission != null)
      try {
        if (this.subject == null)
          throw new AccessDeniedException("No subject"); 
        this.subject.checkPermission(this.permission);
      } catch (AccessDeniedException e) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER))
          throw e; 
      }  
    check();
  }
  
  protected abstract void check() throws IOException, ServletException;
  
  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"}, justification = "Not used.")
  protected final File getFileParameter(String paramName) { return new File(Util.fixNull(this.request.getParameter(paramName))); }
  
  public void ok() throws IOException, ServletException { respond("<div/>"); }
  
  public void respond(String html) throws IOException, ServletException {
    this.response.setContentType("text/html");
    this.response.getWriter().print(html);
  }
  
  public void error(String message) throws IOException, ServletException { errorWithMarkup((message == null) ? null : Util.escape(message)); }
  
  public void warning(String message) throws IOException, ServletException { warningWithMarkup((message == null) ? null : Util.escape(message)); }
  
  public void ok(String message) throws IOException, ServletException { okWithMarkup((message == null) ? null : Util.escape(message)); }
  
  public void error(String format, Object... args) throws IOException, ServletException { error(String.format(format, args)); }
  
  public void warning(String format, Object... args) throws IOException, ServletException { warning(String.format(format, args)); }
  
  public void ok(String format, Object... args) throws IOException, ServletException { ok(String.format(format, args)); }
  
  public void errorWithMarkup(String message) throws IOException, ServletException { _errorWithMarkup(message, "error"); }
  
  public void warningWithMarkup(String message) throws IOException, ServletException { _errorWithMarkup(message, "warning"); }
  
  public void okWithMarkup(String message) throws IOException, ServletException { _errorWithMarkup(message, "ok"); }
  
  private void _errorWithMarkup(String message, String cssClass) throws IOException, ServletException {
    if (message == null) {
      ok();
    } else {
      this.response.setContentType("text/html;charset=UTF-8");
      if (FormValidation.APPLY_CONTENT_SECURITY_POLICY_HEADERS)
        for (String header : new String[] { "Content-Security-Policy", "X-WebKit-CSP", "X-Content-Security-Policy" })
          this.response.setHeader(header, "sandbox; default-src 'none';");  
      this.response.getWriter().print("<div class=" + cssClass + ">" + message + "</div>");
    } 
  }
}
