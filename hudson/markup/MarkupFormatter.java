package hudson.markup;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletException;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.POST;

public abstract class MarkupFormatter extends AbstractDescribableImpl<MarkupFormatter> implements ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(MarkupFormatter.class.getName());
  
  private static boolean PREVIEWS_ALLOW_GET = SystemProperties.getBoolean(MarkupFormatter.class.getName() + ".previewsAllowGET");
  
  private static boolean PREVIEWS_SET_CSP = SystemProperties.getBoolean(MarkupFormatter.class.getName() + ".previewsSetCSP", true);
  
  @NonNull
  public final String translate(@CheckForNull String markup) throws IOException {
    StringWriter w = new StringWriter();
    translate(markup, w);
    return w.toString();
  }
  
  public String getHelpUrl() { return getDescriptor().getHelpFile("syntax"); }
  
  public MarkupFormatterDescriptor getDescriptor() { return (MarkupFormatterDescriptor)super.getDescriptor(); }
  
  @POST
  public HttpResponse doPreviewDescription(@QueryParameter String text) throws IOException {
    StringWriter w = new StringWriter();
    translate(text, w);
    Map<String, String> extraHeaders = Collections.emptyMap();
    if (PREVIEWS_SET_CSP)
      extraHeaders = (Map)Stream.of(new String[] { "Content-Security-Policy", "X-WebKit-CSP", "X-Content-Security-Policy" }).collect(Collectors.toMap(Function.identity(), v -> "default-src 'none';")); 
    return html(200, w.toString(), extraHeaders);
  }
  
  @GET
  @WebMethod(name = {"previewDescription"})
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public HttpResponse previewsNowNeedPostForSecurity2153(@QueryParameter String text, StaplerRequest req) throws IOException {
    LOGGER.log(Level.FINE, "Received a GET request at " + req.getRequestURL());
    if (PREVIEWS_ALLOW_GET)
      return doPreviewDescription(text); 
    return html(405, "This endpoint now requires that POST requests are sent. Update the component implementing this preview feature.", Collections.emptyMap());
  }
  
  private static HttpResponse html(int status, @NonNull String html, @NonNull Map<String, String> headers) {
    return (req, rsp, node) -> {
        rsp.setContentType("text/html;charset=UTF-8");
        rsp.setStatus(status);
        for (Map.Entry<String, String> header : headers.entrySet())
          rsp.setHeader((String)header.getKey(), (String)header.getValue()); 
        PrintWriter pw = rsp.getWriter();
        pw.print(html);
        pw.flush();
      };
  }
  
  public abstract void translate(@CheckForNull String paramString, @NonNull Writer paramWriter) throws IOException;
}
