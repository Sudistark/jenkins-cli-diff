package hudson.scm;

import hudson.model.Descriptor;
import hudson.util.DescriptorList;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class RepositoryBrowsers {
  @Deprecated
  public static final List<Descriptor<RepositoryBrowser<?>>> LIST = new DescriptorList(RepositoryBrowser.class);
  
  public static List<Descriptor<RepositoryBrowser<?>>> filter(Class<? extends RepositoryBrowser> t) {
    List<Descriptor<RepositoryBrowser<?>>> r = new ArrayList<Descriptor<RepositoryBrowser<?>>>();
    for (Descriptor<RepositoryBrowser<?>> d : RepositoryBrowser.all()) {
      if (d.isSubTypeOf(t))
        r.add(d); 
    } 
    return r;
  }
  
  @Deprecated
  public static <T extends RepositoryBrowser> T createInstance(Class<T> type, StaplerRequest req, String fieldName) throws Descriptor.FormException {
    List<Descriptor<RepositoryBrowser<?>>> list = filter(type);
    String value = req.getParameter(fieldName);
    if (value == null || value.equals("auto"))
      return null; 
    JSONObject emptyJSON = new JSONObject();
    return (T)(RepositoryBrowser)type.cast(((Descriptor)list.get(Integer.parseInt(value))).newInstance(req, emptyJSON));
  }
  
  public static <T extends RepositoryBrowser> T createInstance(Class<T> type, StaplerRequest req, JSONObject parent, String fieldName) throws Descriptor.FormException {
    JSONObject o = (JSONObject)parent.get(fieldName);
    if (o == null)
      return null; 
    return (T)(RepositoryBrowser)req.bindJSON(type, o);
  }
}
