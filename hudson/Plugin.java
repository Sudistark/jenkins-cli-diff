package hudson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Descriptor;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class Plugin implements Saveable, StaplerProxy {
  private static final Logger LOGGER = Logger.getLogger(Plugin.class.getName());
  
  PluginWrapper wrapper;
  
  public void setServletContext(ServletContext context) {}
  
  public PluginWrapper getWrapper() { return this.wrapper; }
  
  public void start() {}
  
  public void postInitialize() {}
  
  public void stop() {}
  
  @Deprecated
  public void configure(JSONObject formData) throws IOException, ServletException, Descriptor.FormException {}
  
  public void configure(StaplerRequest req, JSONObject formData) throws IOException, ServletException, Descriptor.FormException { configure(formData); }
  
  public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    String path = req.getRestOfPath();
    String pathUC = path.toUpperCase(Locale.ENGLISH);
    if (path.isEmpty() || path.contains("..") || path.startsWith(".") || path.contains("%") || pathUC
      .contains("META-INF") || pathUC.contains("WEB-INF") || pathUC
      
      .equals("/.TIMESTAMP2")) {
      LOGGER.warning("rejecting possibly malicious " + req.getRequestURIWithQueryString());
      rsp.sendError(400);
      return;
    } 
    String requestPath = req.getRequestURI().substring(req.getContextPath().length());
    boolean staticLink = requestPath.startsWith("/static/");
    long expires = staticLink ? TimeUnit.DAYS.toMillis(365L) : -1L;
    rsp.serveLocalizedFile(req, new URL(this.wrapper.baseResourceURL, "." + path), expires);
  }
  
  protected void load() {
    XmlFile xml = getConfigXml();
    if (xml.exists())
      xml.unmarshal(this); 
  }
  
  public void save() {
    if (BulkChange.contains(this))
      return; 
    XmlFile config = getConfigXml();
    config.write(this);
    SaveableListener.fireOnChange(this, config);
  }
  
  protected XmlFile getConfigXml() {
    return new XmlFile(Jenkins.XSTREAM, new File(
          Jenkins.get().getRootDir(), this.wrapper.getShortName() + ".xml"));
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK)
      Jenkins.get().checkPermission(Jenkins.READ); 
    return this;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(Plugin.class.getName() + ".skipPermissionCheck");
}
