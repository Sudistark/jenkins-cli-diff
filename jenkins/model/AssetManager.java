package jenkins.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import jenkins.ClassLoaderReflectionToolkit;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

@Extension
@Symbol({"assetManager"})
public class AssetManager implements UnprotectedRootAction {
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return null; }
  
  public String getUrlName() { return "assets"; }
  
  public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    String path = req.getRestOfPath();
    URL resource = findResource(path);
    if (resource == null) {
      rsp.setStatus(404);
      return;
    } 
    String requestPath = req.getRequestURI().substring(req.getContextPath().length());
    boolean staticLink = requestPath.startsWith("/static/");
    long expires = staticLink ? TimeUnit.DAYS.toMillis(365L) : -1L;
    rsp.serveLocalizedFile(req, resource, expires);
  }
  
  @CheckForNull
  private URL findResource(@NonNull String path) throws IOException {
    String name;
    if (StringUtils.isBlank(path))
      return null; 
    if (path.contains(".."))
      throw new IllegalArgumentException(path); 
    if (path.charAt(0) == '/') {
      name = "assets" + path;
    } else {
      name = "assets/" + path;
    } 
    ClassLoader cl = Jenkins.class.getClassLoader();
    URL url = ClassLoaderReflectionToolkit._findResource(cl, name);
    if (url == null) {
      Enumeration<URL> e = cl.getResources(name);
      while (e.hasMoreElements())
        url = (URL)e.nextElement(); 
    } 
    return url;
  }
}
