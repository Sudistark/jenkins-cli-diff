package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class AllView extends View {
  public static final String DEFAULT_VIEW_NAME = "all";
  
  private static final Logger LOGGER = Logger.getLogger(AllView.class.getName());
  
  @DataBoundConstructor
  public AllView(String name) { super(name); }
  
  public AllView(String name, ViewGroup owner) {
    this(name);
    this.owner = owner;
  }
  
  public boolean isEditable() { return false; }
  
  public boolean contains(TopLevelItem item) { return true; }
  
  public String getDisplayName() { return "all".equals(this.name) ? Messages.Hudson_ViewName() : this.name; }
  
  @RequirePOST
  public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    ItemGroup<? extends TopLevelItem> ig = getOwner().getItemGroup();
    if (ig instanceof ModifiableItemGroup)
      return ((ModifiableItemGroup)ig).doCreateItem(req, rsp); 
    return null;
  }
  
  public Collection<TopLevelItem> getItems() { return getOwner().getItemGroup().getItems(); }
  
  public String getPostConstructLandingPage() { return ""; }
  
  protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {}
  
  @NonNull
  public static String migrateLegacyPrimaryAllViewLocalizedName(@NonNull List<View> views, @NonNull String primaryView) {
    if ("all".equals(primaryView))
      return primaryView; 
    if (SystemProperties.getBoolean(AllView.class.getName() + ".JENKINS-38606", true)) {
      AllView allView = null;
      for (View v : views) {
        if ("all".equals(v.getViewName()))
          return primaryView; 
        if (Objects.equals(v.getViewName(), primaryView)) {
          if (v instanceof AllView) {
            allView = (AllView)v;
            continue;
          } 
          return primaryView;
        } 
      } 
      if (allView != null)
        for (Locale l : Locale.getAvailableLocales()) {
          if (primaryView.equals(Messages._Hudson_ViewName().toString(l))) {
            LOGGER.log(Level.INFO, "JENKINS-38606 detected for AllView in {0}; renaming view from {1} to {2}", new Object[] { allView.owner, primaryView, "all" });
            allView.name = "all";
            return "all";
          } 
        }  
    } 
    return primaryView;
  }
}
