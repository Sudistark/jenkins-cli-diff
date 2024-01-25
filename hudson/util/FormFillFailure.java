package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class FormFillFailure extends IOException implements HttpResponse {
  private final FormValidation.Kind kind;
  
  private boolean selectionCleared;
  
  public static FormFillFailure error(@NonNull String message) { return errorWithMarkup(Util.escape(message)); }
  
  public static FormFillFailure warning(@NonNull String message) { return warningWithMarkup(Util.escape(message)); }
  
  public static FormFillFailure error(String format, Object... args) { return error(String.format(format, args)); }
  
  public static FormFillFailure warning(String format, Object... args) { return warning(String.format(format, args)); }
  
  public static FormFillFailure error(Throwable e, String message) { return _error(FormValidation.Kind.ERROR, e, message); }
  
  public static FormFillFailure warning(Throwable e, String message) { return _error(FormValidation.Kind.WARNING, e, message); }
  
  private static FormFillFailure _error(FormValidation.Kind kind, Throwable e, String message) {
    if (e == null)
      return _errorWithMarkup(Util.escape(message), kind); 
    return _errorWithMarkup(Util.escape(message) + " </div><div><a href='#' class='showDetails'>" + Util.escape(message) + "</a><pre style='display:none'>" + 
        
        Messages.FormValidation_Error_Details() + "</pre>", 
        
        kind);
  }
  
  public static FormFillFailure error(Throwable e, String format, Object... args) { return error(e, String.format(format, args)); }
  
  public static FormFillFailure warning(Throwable e, String format, Object... args) { return warning(e, String.format(format, args)); }
  
  public static FormFillFailure errorWithMarkup(String message) { return _errorWithMarkup(message, FormValidation.Kind.ERROR); }
  
  public static FormFillFailure warningWithMarkup(String message) { return _errorWithMarkup(message, FormValidation.Kind.WARNING); }
  
  private static FormFillFailure _errorWithMarkup(@NonNull String message, FormValidation.Kind kind) { return new Object(kind, message, message, kind); }
  
  public static FormFillFailure respond(FormValidation.Kind kind, String html) { return new Object(kind, html); }
  
  private FormFillFailure(FormValidation.Kind kind) { this.kind = kind; }
  
  private FormFillFailure(FormValidation.Kind kind, String message) {
    super(message);
    this.kind = kind;
  }
  
  public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
    rsp.setContentType("text/html;charset=UTF-8");
    rsp.setStatus(500);
    rsp.setHeader("X-Jenkins-Select-Error", this.selectionCleared ? "clear" : "retain");
    rsp.getWriter().print(renderHtml());
  }
  
  public FormValidation.Kind getKind() { return this.kind; }
  
  public boolean isSelectionCleared() { return this.selectionCleared; }
  
  public FormFillFailure withSelectionCleared() {
    this.selectionCleared = true;
    return this;
  }
  
  public abstract String renderHtml();
}
