package jenkins.util;

import hudson.Util;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import jenkins.plugins.DetachedPluginsUtil;
import net.sf.json.JSONArray;

public class PluginLabelUtil {
  private static HashMap<String, String> renamedLabels;
  
  private static String canonicalLabel(String label) {
    if (renamedLabels == null) {
      renamedLabels = new HashMap();
      try {
        InputStream is = PluginLabelUtil.class.getResourceAsStream("/jenkins/canonical-labels.txt");
        try {
          DetachedPluginsUtil.configLines(is).forEach(line -> {
                String[] pieces = line.split(" ");
                renamedLabels.put(pieces[0], pieces[1]);
              });
          if (is != null)
            is.close(); 
        } catch (Throwable throwable) {
          if (is != null)
            try {
              is.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } catch (IOException x) {
        throw new ExceptionInInitializerError(x);
      } 
    } 
    return (String)renamedLabels.getOrDefault(label, label);
  }
  
  public static String[] canonicalLabels(JSONArray labels) {
    HashSet<String> uniqueLabels = new HashSet<String>();
    for (Object label : labels)
      uniqueLabels.add(Util.intern(canonicalLabel(label.toString()))); 
    return (String[])uniqueLabels.toArray(MemoryReductionUtil.EMPTY_STRING_ARRAY);
  }
}
