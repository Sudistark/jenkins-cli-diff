package hudson;

import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class StructuredForm {
  @Deprecated
  public static JSONObject get(StaplerRequest req) throws ServletException { return req.getSubmittedForm(); }
  
  public static List<JSONObject> toList(JSONObject parent, String propertyName) {
    Object v = parent.get(propertyName);
    if (v == null)
      return Collections.emptyList(); 
    if (v instanceof JSONObject)
      return List.of((JSONObject)v); 
    if (v instanceof net.sf.json.JSONArray)
      return (List)v; 
    throw new IllegalArgumentException();
  }
}
