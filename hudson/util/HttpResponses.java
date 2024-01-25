package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

public class HttpResponses extends HttpResponses {
  public static HttpResponse staticResource(File f) throws IOException { return staticResource(f.toURI().toURL()); }
  
  public static HttpResponse okJSON() { return new JSONObjectResponse(); }
  
  public static HttpResponse okJSON(@NonNull JSONObject data) { return new JSONObjectResponse(data); }
  
  public static HttpResponse okJSON(@NonNull JSONArray data) { return new JSONObjectResponse(data); }
  
  public static HttpResponse okJSON(@NonNull Map<?, ?> data) { return new JSONObjectResponse(data); }
  
  public static HttpResponse errorJSON(@NonNull String message) { return (new JSONObjectResponse()).error(message); }
  
  public static HttpResponse errorJSON(@NonNull String message, @NonNull Map<?, ?> data) { return (new JSONObjectResponse(data)).error(message); }
  
  public static HttpResponse errorJSON(@NonNull String message, @NonNull JSONObject data) { return (new JSONObjectResponse(data)).error(message); }
  
  public static HttpResponse errorJSON(@NonNull String message, @NonNull JSONArray data) { return (new JSONObjectResponse(data)).error(message); }
}
