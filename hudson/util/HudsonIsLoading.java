package hudson.util;

import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class HudsonIsLoading {
  public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
    rsp.setStatus(503);
    req.getView(this, "index.jelly").forward(req, rsp);
  }
}
