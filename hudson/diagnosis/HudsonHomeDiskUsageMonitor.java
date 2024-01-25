package hudson.diagnosis;

import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import java.io.IOException;
import java.util.List;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol({"diskUsageCheck"})
public final class HudsonHomeDiskUsageMonitor extends AdministrativeMonitor {
  boolean activated;
  
  public HudsonHomeDiskUsageMonitor() { super("hudsonHomeIsFull"); }
  
  public boolean isActivated() { return this.activated; }
  
  public String getDisplayName() { return Messages.HudsonHomeDiskUsageMonitor_DisplayName(); }
  
  @RequirePOST
  public HttpResponse doAct(@QueryParameter String no) throws IOException {
    if (no != null) {
      disable(true);
      return HttpResponses.redirectViaContextPath("/manage");
    } 
    return HttpResponses.redirectToDot();
  }
  
  public List<Solution> getSolutions() { return Solution.all(); }
  
  public Solution getSolution(String id) {
    for (Solution s : Solution.all()) {
      if (s.id.equals(id))
        return s; 
    } 
    return null;
  }
  
  public static HudsonHomeDiskUsageMonitor get() { return (HudsonHomeDiskUsageMonitor)all().get(HudsonHomeDiskUsageMonitor.class); }
}
