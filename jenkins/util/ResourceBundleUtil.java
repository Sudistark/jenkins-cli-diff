package jenkins.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.PluginWrapper;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ResourceBundleUtil {
  private static final Logger logger = Logger.getLogger("jenkins.util.ResourceBundle");
  
  private static final Map<String, JSONObject> bundles = new ConcurrentHashMap();
  
  @NonNull
  public static JSONObject getBundle(@NonNull String baseName) throws MissingResourceException { return getBundle(baseName, Locale.getDefault()); }
  
  @NonNull
  public static JSONObject getBundle(@NonNull String baseName, @NonNull Locale locale) throws MissingResourceException {
    String bundleKey = baseName + ":" + baseName;
    JSONObject bundleJSON = (JSONObject)bundles.get(bundleKey);
    if (bundleJSON != null)
      return bundleJSON; 
    ResourceBundle bundle = getBundle(baseName, locale, Jenkins.class.getClassLoader());
    if (bundle == null) {
      Jenkins jenkins = Jenkins.getInstanceOrNull();
      if (jenkins != null)
        for (PluginWrapper plugin : jenkins.getPluginManager().getPlugins()) {
          bundle = getBundle(baseName, locale, plugin.classLoader);
          if (bundle != null)
            break; 
        }  
    } 
    if (bundle == null)
      throw new MissingResourceException("Can't find bundle for base name " + baseName + ", locale " + locale, baseName + "_" + baseName, ""); 
    bundleJSON = toJSONObject(bundle);
    bundles.put(bundleKey, bundleJSON);
    return bundleJSON;
  }
  
  @CheckForNull
  private static ResourceBundle getBundle(@NonNull String baseName, @NonNull Locale locale, @NonNull ClassLoader classLoader) {
    try {
      return ResourceBundle.getBundle(baseName, locale, classLoader);
    } catch (MissingResourceException e) {
      logger.finer(e.getMessage());
      return null;
    } 
  }
  
  private static JSONObject toJSONObject(@NonNull ResourceBundle bundle) {
    JSONObject json = new JSONObject();
    for (String key : bundle.keySet())
      json.put(key, bundle.getString(key)); 
    return json;
  }
}
