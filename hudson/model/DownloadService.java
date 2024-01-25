package hudson.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.ProxyConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import jenkins.util.SystemProperties;
import org.apache.commons.io.IOUtils;
import org.kohsuke.accmod.Restricted;

@Extension
public class DownloadService {
  private static final String signatureValidatorPrefix = "downloadable";
  
  @Deprecated
  public String generateFragment() { return ""; }
  
  public Downloadable getById(String id) {
    for (Downloadable d : Downloadable.all()) {
      if (d.getId().equals(id))
        return d; 
    } 
    return null;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String loadJSON(URL src) throws IOException {
    URLConnection con = ProxyConfiguration.open(src);
    if (con instanceof HttpURLConnection)
      ((HttpURLConnection)con).setInstanceFollowRedirects(true); 
    InputStream is = con.getInputStream();
    try {
      String jsonp = IOUtils.toString(is, StandardCharsets.UTF_8);
      int start = jsonp.indexOf('{');
      int end = jsonp.lastIndexOf('}');
      if (start >= 0 && end > start) {
        String str = jsonp.substring(start, end + 1);
        if (is != null)
          is.close(); 
        return str;
      } 
      throw new IOException("Could not find JSON in " + src);
    } catch (Throwable throwable) {
      if (is != null)
        try {
          is.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String loadJSONHTML(URL src) throws IOException {
    URLConnection con = ProxyConfiguration.open(src);
    if (con instanceof HttpURLConnection)
      ((HttpURLConnection)con).setInstanceFollowRedirects(true); 
    InputStream is = con.getInputStream();
    try {
      String jsonp = IOUtils.toString(is, StandardCharsets.UTF_8);
      String preamble = "window.parent.postMessage(JSON.stringify(";
      int start = jsonp.indexOf(preamble);
      int end = jsonp.lastIndexOf("),'*');");
      if (start >= 0 && end > start) {
        String str = jsonp.substring(start + preamble.length(), end).trim();
        if (is != null)
          is.close(); 
        return str;
      } 
      throw new IOException("Could not find JSON in " + src);
    } catch (Throwable throwable) {
      if (is != null)
        try {
          is.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean neverUpdate = SystemProperties.getBoolean(DownloadService.class.getName() + ".never");
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean signatureCheck = !SystemProperties.getBoolean(DownloadService.class.getName() + ".noSignatureCheck");
}
