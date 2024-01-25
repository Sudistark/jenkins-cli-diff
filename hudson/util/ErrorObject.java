package hudson.util;

import hudson.Functions;
import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class ErrorObject extends Exception {
  protected ErrorObject() {}
  
  protected ErrorObject(Throwable cause) { super(cause); }
  
  public String getStackTraceString() { return Functions.printThrowable(this); }
  
  public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
    rsp.setStatus(503);
    req.getView(this, "index.jelly").forward(req, rsp);
  }
}
