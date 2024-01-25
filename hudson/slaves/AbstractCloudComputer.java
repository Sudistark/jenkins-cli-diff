package hudson.slaves;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Node;
import hudson.model.Slave;
import java.io.IOException;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class AbstractCloudComputer<T extends AbstractCloudSlave> extends SlaveComputer {
  public AbstractCloudComputer(T slave) { super(slave); }
  
  @CheckForNull
  public T getNode() { return (T)(AbstractCloudSlave)super.getNode(); }
  
  @RequirePOST
  public HttpResponse doDoDelete() throws IOException {
    checkPermission(DELETE);
    try {
      T node = (T)getNode();
      if (node != null)
        node.terminate(); 
      return new HttpRedirect("..");
    } catch (InterruptedException e) {
      return HttpResponses.error(500, e);
    } 
  }
}
