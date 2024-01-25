package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.IOException;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class Failure extends RuntimeException implements HttpResponse {
  private final boolean pre;
  
  public Failure(String message) { this(message, false); }
  
  public Failure(String message, boolean pre) {
    super(message);
    this.pre = pre;
  }
  
  public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node, @CheckForNull Throwable throwable) throws IOException, ServletException {
    if (throwable != null)
      req.setAttribute("exception", throwable); 
    generateResponse(req, rsp, node);
  }
  
  public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
    req.setAttribute("message", getMessage());
    if (this.pre)
      req.setAttribute("pre", Boolean.valueOf(true)); 
    if (node instanceof AbstractItem) {
      rsp.forward(Jenkins.get(), ((AbstractItem)node).getUrl() + "error", req);
    } else {
      rsp.forward((node instanceof AbstractModelObject) ? node : Jenkins.get(), "error", req);
    } 
  }
}
