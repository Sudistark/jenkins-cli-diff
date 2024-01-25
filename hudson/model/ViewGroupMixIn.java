package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.kohsuke.stapler.export.Exported;

public abstract class ViewGroupMixIn {
  private final ViewGroup owner;
  
  @NonNull
  protected abstract List<View> views();
  
  @CheckForNull
  protected abstract String primaryView();
  
  protected abstract void primaryView(String paramString);
  
  protected ViewGroupMixIn(ViewGroup owner) { this.owner = owner; }
  
  public void addView(@NonNull View v) throws IOException {
    v.owner = this.owner;
    views().add(v);
    this.owner.save();
  }
  
  public boolean canDelete(@NonNull View view) { return !view.isDefault(); }
  
  public void deleteView(@NonNull View view) throws IOException {
    if (views().size() <= 1)
      throw new IllegalStateException("Cannot delete last view"); 
    views().remove(view);
    this.owner.save();
  }
  
  @CheckForNull
  public View getView(@CheckForNull String name) {
    if (name == null)
      return null; 
    List<View> views = views();
    for (View v : views) {
      if (v.getViewName().equals(name))
        return v; 
    } 
    for (View v : views) {
      if (v instanceof ViewGroup) {
        View nestedView = ((ViewGroup)v).getView(name);
        if (nestedView != null)
          return nestedView; 
      } 
    } 
    if (!name.equals(primaryView())) {
      View pv = getPrimaryView();
      if (pv instanceof ViewGroup)
        return ((ViewGroup)pv).getView(name); 
      if (pv instanceof AllView && "all".equals(pv.name))
        for (Locale l : Locale.getAvailableLocales()) {
          if (name.equals(Messages._Hudson_ViewName().toString(l)))
            return pv; 
        }  
    } 
    return null;
  }
  
  @Exported
  public Collection<View> getViews() {
    List<View> orig = views();
    List<View> copy = new ArrayList<View>(orig.size());
    for (View v : orig) {
      if (v.hasPermission(View.READ))
        copy.add(v); 
    } 
    copy.sort(View.SORTER);
    return copy;
  }
  
  @Exported
  @CheckForNull
  public View getPrimaryView() {
    View v = getView(primaryView());
    if (v == null && !views().isEmpty())
      v = (View)views().get(0); 
    return v;
  }
  
  public void onViewRenamed(View view, String oldName, String newName) {
    if (oldName.equals(primaryView()))
      primaryView(newName); 
  }
}
