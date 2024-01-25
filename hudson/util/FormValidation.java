package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.Messages;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class FormValidation extends IOException implements HttpResponse {
  static boolean APPLY_CONTENT_SECURITY_POLICY_HEADERS = SystemProperties.getBoolean(FormValidation.class.getName() + ".applyContentSecurityPolicyHeaders", true);
  
  public static FormValidation error(String message) { return errorWithMarkup((message == null) ? null : Util.escape(message)); }
  
  public static FormValidation warning(String message) { return warningWithMarkup((message == null) ? null : Util.escape(message)); }
  
  public static FormValidation ok(String message) { return okWithMarkup((message == null) ? null : Util.escape(message)); }
  
  private static final FormValidation OK = respond(Kind.OK, "<div/>");
  
  public final Kind kind;
  
  public static FormValidation ok() { return OK; }
  
  public static FormValidation error(String format, Object... args) { return error(String.format(format, args)); }
  
  public static FormValidation warning(String format, Object... args) { return warning(String.format(format, args)); }
  
  public static FormValidation ok(String format, Object... args) { return ok(String.format(format, args)); }
  
  public static FormValidation error(Throwable e, String message) { return _error(Kind.ERROR, e, message); }
  
  public static FormValidation warning(Throwable e, String message) { return _error(Kind.WARNING, e, message); }
  
  private static FormValidation _error(Kind kind, Throwable e, String message) {
    if (e == null)
      return _errorWithMarkup(Util.escape(message), kind); 
    return _errorWithMarkup(Util.escape(message) + " </div><div><a href='#' class='showDetails'>" + Util.escape(message) + "</a><pre style='display:none'>" + 
        
        Messages.FormValidation_Error_Details() + "</pre>", 
        
        kind);
  }
  
  public static FormValidation error(Throwable e, String format, Object... args) { return error(e, String.format(format, args)); }
  
  public static FormValidation warning(Throwable e, String format, Object... args) { return warning(e, String.format(format, args)); }
  
  @NonNull
  public static FormValidation aggregate(@NonNull Collection<FormValidation> validations) {
    if (validations == null || validations.isEmpty())
      return ok(); 
    if (validations.size() == 1)
      return (FormValidation)validations.iterator().next(); 
    StringBuilder sb = new StringBuilder("<ul style='list-style-type: none; padding-left: 0; margin: 0'>");
    Kind worst = Kind.OK;
    for (FormValidation validation : validations) {
      sb.append("<li>").append(validation.renderHtml()).append("</li>");
      if (validation.kind.ordinal() > worst.ordinal())
        worst = validation.kind; 
    } 
    sb.append("</ul>");
    return respond(worst, sb.toString());
  }
  
  public static FormValidation errorWithMarkup(String message) { return _errorWithMarkup(message, Kind.ERROR); }
  
  public static FormValidation warningWithMarkup(String message) { return _errorWithMarkup(message, Kind.WARNING); }
  
  public static FormValidation okWithMarkup(String message) { return _errorWithMarkup(message, Kind.OK); }
  
  private static FormValidation _errorWithMarkup(String message, Kind kind) {
    if (message == null)
      return ok(); 
    return new Object(kind, message, message);
  }
  
  public static FormValidation respond(Kind kind, String html) { return new Object(kind, html); }
  
  public static FormValidation validateExecutable(String exe) { return validateExecutable(exe, FileValidator.NOOP); }
  
  public static FormValidation validateExecutable(String exe, FileValidator exeValidator) {
    if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER))
      return ok(); 
    FormValidation[] result = { null };
    try {
      DOSToUnixPathHelper.iteratePath(exe, new Object(result, exeValidator));
      return result[0];
    } catch (RuntimeException e) {
      return error(e, "Unexpected error");
    } 
  }
  
  public static FormValidation validateNonNegativeInteger(String value) {
    try {
      if (Integer.parseInt(value) < 0)
        return error(Messages.Hudson_NotANonNegativeNumber()); 
      return ok();
    } catch (NumberFormatException e) {
      return error(Messages.Hudson_NotANumber());
    } 
  }
  
  public static FormValidation validateIntegerInRange(String value, int lower, int upper) {
    try {
      int intValue = Integer.parseInt(value);
      if (intValue < lower)
        return error(Messages.Hudson_MustBeAtLeast(Integer.valueOf(lower))); 
      if (intValue > upper)
        return error(Messages.Hudson_MustBeAtMost(Integer.valueOf(upper))); 
      return ok();
    } catch (NumberFormatException e) {
      return error(Messages.Hudson_NotANumber());
    } 
  }
  
  public static FormValidation validatePositiveInteger(String value) {
    try {
      if (Integer.parseInt(value) <= 0)
        return error(Messages.Hudson_NotAPositiveNumber()); 
      return ok();
    } catch (NumberFormatException e) {
      return error(Messages.Hudson_NotANumber());
    } 
  }
  
  public static FormValidation validateRequired(String value) {
    if (Util.fixEmptyAndTrim(value) == null)
      return error(Messages.FormValidation_ValidateRequired()); 
    return ok();
  }
  
  public static FormValidation validateBase64(String value, boolean allowWhitespace, boolean allowEmpty, String errorMessage) {
    try {
      String v = value;
      if (!allowWhitespace && (
        v.indexOf(' ') >= 0 || v.indexOf('\n') >= 0))
        return error(errorMessage); 
      v = v.trim();
      if (!allowEmpty && v.isEmpty())
        return error(errorMessage); 
      Base64.getDecoder().decode(v.getBytes(StandardCharsets.UTF_8));
      return ok();
    } catch (IllegalArgumentException e) {
      return error(errorMessage);
    } 
  }
  
  private FormValidation(Kind kind) { this.kind = kind; }
  
  private FormValidation(Kind kind, String message) {
    super(message);
    this.kind = kind;
  }
  
  public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException { respond(rsp, renderHtml()); }
  
  public abstract String renderHtml();
  
  protected void respond(StaplerResponse rsp, String html) throws IOException, ServletException {
    rsp.setContentType("text/html;charset=UTF-8");
    if (APPLY_CONTENT_SECURITY_POLICY_HEADERS)
      for (String header : new String[] { "Content-Security-Policy", "X-WebKit-CSP", "X-Content-Security-Policy" })
        rsp.setHeader(header, "sandbox; default-src 'none';");  
    rsp.getWriter().print(html);
  }
}
