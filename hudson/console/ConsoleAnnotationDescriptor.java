package hudson.console;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Descriptor;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;

public abstract class ConsoleAnnotationDescriptor extends Descriptor<ConsoleNote<?>> implements ExtensionPoint {
  protected ConsoleAnnotationDescriptor(Class<? extends ConsoleNote<?>> clazz) { super(clazz); }
  
  protected ConsoleAnnotationDescriptor() {}
  
  @NonNull
  public String getDisplayName() { return super.getDisplayName(); }
  
  public boolean hasScript() { return (hasResource("/script.js") != null); }
  
  public boolean hasStylesheet() { return (hasResource("/style.css") != null); }
  
  private URL hasResource(String name) { return this.clazz.getClassLoader().getResource(this.clazz.getName().replace('.', '/').replace('$', '/') + this.clazz.getName().replace('.', '/').replace('$', '/')); }
  
  @WebMethod(name = {"script.js"})
  public void doScriptJs(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { rsp.serveFile(req, hasResource("/script.js"), TimeUnit.DAYS.toMillis(1L)); }
  
  @WebMethod(name = {"style.css"})
  public void doStyleCss(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { rsp.serveFile(req, hasResource("/style.css"), TimeUnit.DAYS.toMillis(1L)); }
  
  public static DescriptorExtensionList<ConsoleNote<?>, ConsoleAnnotationDescriptor> all() { return Jenkins.get().getDescriptorList(ConsoleNote.class); }
}
