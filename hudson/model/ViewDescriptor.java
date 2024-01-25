package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.FormValidation;
import hudson.views.ListViewColumn;
import hudson.views.ViewJobFilter;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import jenkins.model.DirectlyModifiableTopLevelItemGroup;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

public abstract class ViewDescriptor extends Descriptor<View> {
  @NonNull
  public String getDisplayName() { return super.getDisplayName(); }
  
  public boolean isInstantiable() { return true; }
  
  public final String getNewViewDetailPage() { return "/" + this.clazz.getName().replace('.', '/').replace('$', '/') + "/newViewDetail.jelly"; }
  
  protected ViewDescriptor(Class<? extends View> clazz) { super(clazz); }
  
  protected ViewDescriptor() {}
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public AutoCompletionCandidates doAutoCompleteCopyNewItemFrom(@QueryParameter String value, @AncestorInPath ItemGroup<?> container) {
    AutoCompletionCandidates candidates = AutoCompletionCandidates.ofJobNames(TopLevelItem.class, value, container);
    if (container instanceof DirectlyModifiableTopLevelItemGroup) {
      DirectlyModifiableTopLevelItemGroup modifiableContainer = (DirectlyModifiableTopLevelItemGroup)container;
      Iterator<String> it = candidates.getValues().iterator();
      while (it.hasNext()) {
        TopLevelItem item = (TopLevelItem)Jenkins.get().getItem((String)it.next(), container, TopLevelItem.class);
        if (item == null)
          continue; 
        if (!modifiableContainer.canAdd(item))
          it.remove(); 
      } 
    } 
    return candidates;
  }
  
  public List<Descriptor<ListViewColumn>> getColumnsDescriptors() {
    StaplerRequest request = Stapler.getCurrentRequest();
    if (request != null) {
      View view = (View)request.findAncestorObject(this.clazz);
      return (view == null) ? DescriptorVisibilityFilter.applyType(this.clazz, ListViewColumn.all()) : 
        DescriptorVisibilityFilter.apply(view, ListViewColumn.all());
    } 
    return ListViewColumn.all();
  }
  
  public List<Descriptor<ViewJobFilter>> getJobFiltersDescriptors() {
    StaplerRequest request = Stapler.getCurrentRequest();
    if (request != null) {
      View view = (View)request.findAncestorObject(this.clazz);
      return (view == null) ? DescriptorVisibilityFilter.applyType(this.clazz, ViewJobFilter.all()) : 
        DescriptorVisibilityFilter.apply(view, ViewJobFilter.all());
    } 
    return ViewJobFilter.all();
  }
  
  protected FormValidation checkDisplayName(@NonNull View view, @CheckForNull String value) {
    if (StringUtils.isBlank(value))
      return FormValidation.ok(); 
    for (View v : view.owner.getViews()) {
      if (v.getViewName().equals(view.getViewName()))
        continue; 
      if (Objects.equals(v.getDisplayName(), value))
        return FormValidation.warning(Messages.View_DisplayNameNotUniqueWarning(value)); 
    } 
    return FormValidation.ok();
  }
  
  public boolean isApplicable(Class<? extends ViewGroup> ownerType) { return true; }
  
  public boolean isApplicableIn(ViewGroup owner) { return isApplicable(owner.getClass()); }
}
