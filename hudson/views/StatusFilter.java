package hudson.views;

import hudson.model.TopLevelItem;
import hudson.model.View;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.ParameterizedJobMixIn;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public final class StatusFilter extends ViewJobFilter {
  private final boolean statusFilter;
  
  @DataBoundConstructor
  public StatusFilter(boolean statusFilter) { this.statusFilter = statusFilter; }
  
  public List<TopLevelItem> filter(List<TopLevelItem> added, List<TopLevelItem> all, View filteringView) {
    List<TopLevelItem> filtered = new ArrayList<TopLevelItem>();
    for (TopLevelItem item : added) {
      if (!(item instanceof ParameterizedJobMixIn.ParameterizedJob) || ((ParameterizedJobMixIn.ParameterizedJob)item)
        .isDisabled() ^ this.statusFilter)
        filtered.add(item); 
    } 
    return filtered;
  }
  
  public boolean getStatusFilter() { return this.statusFilter; }
}
