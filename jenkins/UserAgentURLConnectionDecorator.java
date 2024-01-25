package jenkins;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.URLConnectionDecorator;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Symbol({"userAgent"})
public class UserAgentURLConnectionDecorator extends URLConnectionDecorator {
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean DISABLED = SystemProperties.getBoolean(UserAgentURLConnectionDecorator.class.getName() + ".DISABLED");
  
  public void decorate(URLConnection con) throws IOException {
    if (!DISABLED && con instanceof HttpURLConnection) {
      HttpURLConnection httpConnection = (HttpURLConnection)con;
      httpConnection.setRequestProperty("User-Agent", getUserAgent());
    } 
  }
  
  public static String getUserAgent() { return "Jenkins/" + Jenkins.getVersion(); }
}
