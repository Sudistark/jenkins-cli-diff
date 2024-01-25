package jenkins.security;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.JsonInErrorMessageSanitizer;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class RedactSecretJsonInErrorMessageSanitizer implements JsonInErrorMessageSanitizer {
  private static final Logger LOGGER = Logger.getLogger(RedactSecretJsonInErrorMessageSanitizer.class.getName());
  
  public static final String REDACT_KEY = "$redact";
  
  public static final String REDACT_VALUE = "[value redacted]";
  
  public static final RedactSecretJsonInErrorMessageSanitizer INSTANCE = new RedactSecretJsonInErrorMessageSanitizer();
  
  public JSONObject sanitize(JSONObject jsonObject) { return copyAndSanitizeObject(jsonObject); }
  
  private Set<String> retrieveRedactedKeys(JSONObject jsonObject) {
    Set<String> redactedKeySet = new HashSet<String>();
    if (jsonObject.has("$redact")) {
      Object value = jsonObject.get("$redact");
      if (value instanceof JSONArray) {
        for (Object o : jsonObject.getJSONArray("$redact")) {
          if (o instanceof String) {
            redactedKeySet.add((String)o);
            continue;
          } 
          LOGGER.log(Level.WARNING, "Unsupported type " + o.getClass().getName() + " for $redact, please use either a single String value or an Array");
        } 
      } else if (value instanceof String) {
        redactedKeySet.add((String)value);
      } else {
        LOGGER.log(Level.WARNING, "Unsupported type " + value.getClass().getName() + " for $redact, please use either a single String value or an Array");
      } 
    } 
    return redactedKeySet;
  }
  
  private Object copyAndSanitize(Object value) {
    if (value instanceof JSONObject)
      return copyAndSanitizeObject((JSONObject)value); 
    if (value instanceof JSONArray)
      return copyAndSanitizeArray((JSONArray)value); 
    return value;
  }
  
  private JSONObject copyAndSanitizeObject(JSONObject jsonObject) {
    Set<String> redactedKeySet = retrieveRedactedKeys(jsonObject);
    JSONObject result = new JSONObject();
    jsonObject.keySet().forEach(keyObject -> {
          String key = keyObject.toString();
          if (redactedKeySet.contains(key)) {
            result.accumulate(key, "[value redacted]");
          } else {
            Object value = jsonObject.get(keyObject);
            result.accumulate(key, copyAndSanitize(value));
          } 
        });
    return result;
  }
  
  private JSONArray copyAndSanitizeArray(JSONArray jsonArray) {
    JSONArray result = new JSONArray();
    jsonArray.forEach(value -> 
        result.add(copyAndSanitize(value)));
    return result;
  }
}
