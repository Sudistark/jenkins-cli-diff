package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.search.Search;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Flavor;

public class AutoCompletionCandidates implements HttpResponse {
  private final List<String> values = new ArrayList();
  
  public AutoCompletionCandidates add(String v) {
    this.values.add(v);
    return this;
  }
  
  public AutoCompletionCandidates add(String... v) {
    this.values.addAll(Arrays.asList(v));
    return this;
  }
  
  public List<String> getValues() { return this.values; }
  
  public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object o) throws IOException, ServletException {
    Search.Result r = new Search.Result();
    for (String value : this.values)
      r.suggestions.add(new Search.Item(value)); 
    rsp.serveExposedBean(req, r, Flavor.JSON);
  }
  
  public static <T extends Item> AutoCompletionCandidates ofJobNames(Class<T> type, String value, @CheckForNull Item self, ItemGroup container) {
    if (self == container)
      container = self.getParent(); 
    return ofJobNames(type, value, container);
  }
  
  @SuppressFBWarnings(value = {"SBSC_USE_STRINGBUFFER_CONCATENATION"}, justification = "no big deal")
  public static <T extends Item> AutoCompletionCandidates ofJobNames(Class<T> type, String value, ItemGroup container) {
    AutoCompletionCandidates candidates = new AutoCompletionCandidates();
    if (container == null || container == Jenkins.get()) {
      (new Visitor("", value, type, candidates)).onItemGroup(Jenkins.get());
    } else {
      (new Visitor("", value, type, candidates)).onItemGroup(container);
      if (value.startsWith("/"))
        (new Visitor("/", value, type, candidates)).onItemGroup(Jenkins.get()); 
      for (String p = "../"; value.startsWith(p); p = p + "../") {
        container = ((Item)container).getParent();
        (new Visitor(p, value, type, candidates)).onItemGroup(container);
      } 
    } 
    return candidates;
  }
  
  private static boolean startsWithImpl(String str, String prefix, boolean ignoreCase) { return ignoreCase ? StringUtils.startsWithIgnoreCase(str, prefix) : str.startsWith(prefix); }
}
