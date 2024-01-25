package hudson.console;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;

public abstract class ConsoleAnnotatorFactory<T> extends Object implements ExtensionPoint {
  public abstract ConsoleAnnotator<T> newInstance(T paramT);
  
  public Class<?> type() {
    Type type = Types.getBaseClass(getClass(), ConsoleAnnotatorFactory.class);
    if (type instanceof java.lang.reflect.ParameterizedType)
      return Types.erasure(Types.getTypeArgument(type, 0)); 
    return Object.class;
  }
  
  public boolean hasScript() { return (getResource("/script.js") != null); }
  
  public boolean hasStylesheet() { return (getResource("/style.css") != null); }
  
  private URL getResource(String fileName) {
    Class<?> c = getClass();
    return c.getClassLoader().getResource(c.getName().replace('.', '/').replace('$', '/') + c.getName().replace('.', '/').replace('$', '/'));
  }
  
  @WebMethod(name = {"script.js"})
  public void doScriptJs(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { rsp.serveFile(req, getResource("/script.js"), TimeUnit.DAYS.toMillis(1L)); }
  
  @WebMethod(name = {"style.css"})
  public void doStyleCss(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { rsp.serveFile(req, getResource("/style.css"), TimeUnit.DAYS.toMillis(1L)); }
  
  public static ExtensionList<ConsoleAnnotatorFactory> all() { return ExtensionList.lookup(ConsoleAnnotatorFactory.class); }
}
