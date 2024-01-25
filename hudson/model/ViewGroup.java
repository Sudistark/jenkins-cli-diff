package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.security.AccessControlled;
import hudson.views.ViewsTabBar;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import jenkins.model.Jenkins;

public interface ViewGroup extends Saveable, ModelObject, AccessControlled {
  boolean canDelete(View paramView);
  
  void deleteView(View paramView) throws IOException;
  
  Collection<View> getViews();
  
  @NonNull
  default Collection<View> getAllViews() {
    Collection<View> views = new LinkedHashSet<View>(getViews());
    for (View view : getViews()) {
      if (view instanceof ViewGroup)
        views.addAll(((ViewGroup)view).getAllViews()); 
    } 
    return views;
  }
  
  View getView(String paramString);
  
  default View getPrimaryView() { return null; }
  
  String getUrl();
  
  void onViewRenamed(View paramView, String paramString1, String paramString2);
  
  ViewsTabBar getViewsTabBar();
  
  default ItemGroup<? extends TopLevelItem> getItemGroup() { return Jenkins.get(); }
  
  default List<Action> getViewActions() { return Jenkins.get().getActions(); }
}
