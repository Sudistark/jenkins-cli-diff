package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Util;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.views.MyViewsTabBar;
import hudson.views.ViewsTabBar;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

public class MyViewsProperty extends UserProperty implements ModifiableViewGroup, Action, StaplerFallback {
  @CheckForNull
  private String primaryViewName;
  
  private CopyOnWriteArrayList<View> views;
  
  private ViewGroupMixIn viewGroupMixIn;
  
  @DataBoundConstructor
  public MyViewsProperty(@CheckForNull String primaryViewName) {
    this.views = new CopyOnWriteArrayList();
    this.primaryViewName = primaryViewName;
    readResolve();
  }
  
  private MyViewsProperty() { this(null); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object readResolve() {
    if (this.views == null)
      this.views = new CopyOnWriteArrayList(); 
    if (this.views.isEmpty())
      this.views.add(new AllView("all", this)); 
    if (this.primaryViewName != null)
      this.primaryViewName = AllView.migrateLegacyPrimaryAllViewLocalizedName(this.views, this.primaryViewName); 
    this.viewGroupMixIn = new Object(this, this);
    return this;
  }
  
  @CheckForNull
  public String getPrimaryViewName() { return this.primaryViewName; }
  
  public void setPrimaryViewName(@CheckForNull String primaryViewName) { this.primaryViewName = primaryViewName; }
  
  public User getUser() { return this.user; }
  
  public String getUrl() {
    return this.user.getUrl() + "/my-views/";
  }
  
  public void save() {
    if (this.user != null)
      this.user.save(); 
  }
  
  public Collection<View> getViews() { return this.viewGroupMixIn.getViews(); }
  
  public View getView(String name) { return this.viewGroupMixIn.getView(name); }
  
  public boolean canDelete(View view) { return this.viewGroupMixIn.canDelete(view); }
  
  public void deleteView(View view) throws IOException { this.viewGroupMixIn.deleteView(view); }
  
  public void onViewRenamed(View view, String oldName, String newName) { this.viewGroupMixIn.onViewRenamed(view, oldName, newName); }
  
  public void addView(View view) throws IOException { this.viewGroupMixIn.addView(view); }
  
  public View getPrimaryView() { return this.viewGroupMixIn.getPrimaryView(); }
  
  public HttpResponse doIndex() {
    return new HttpRedirect("view/" + Util.rawEncode(getPrimaryView().getViewName()) + "/");
  }
  
  @POST
  public void doCreateView(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, ParseException, Descriptor.FormException {
    checkPermission(View.CREATE);
    addView(View.create(req, rsp, this));
  }
  
  public FormValidation doViewExistsCheck(@QueryParameter String value, @QueryParameter boolean exists) {
    checkPermission(View.CREATE);
    String view = Util.fixEmpty(value);
    if (view == null)
      return FormValidation.ok(); 
    if (exists)
      return (getView(view) != null) ? 
        FormValidation.ok() : 
        FormValidation.error(Messages.MyViewsProperty_ViewExistsCheck_NotExist(view)); 
    return (getView(view) == null) ? 
      FormValidation.ok() : 
      FormValidation.error(Messages.MyViewsProperty_ViewExistsCheck_AlreadyExists(view));
  }
  
  public ACL getACL() { return this.user.getACL(); }
  
  public String getDisplayName() { return Messages.MyViewsProperty_DisplayName(); }
  
  public String getIconFileName() { return "symbol-browsers"; }
  
  public String getUrlName() { return "my-views"; }
  
  public UserProperty reconfigure(StaplerRequest req, JSONObject form) throws Descriptor.FormException {
    req.bindJSON(this, form);
    return this;
  }
  
  public ViewsTabBar getViewsTabBar() { return Jenkins.get().getViewsTabBar(); }
  
  public List<Action> getViewActions() { return Collections.emptyList(); }
  
  public Object getStaplerFallback() { return getPrimaryView(); }
  
  public MyViewsTabBar getMyViewsTabBar() { return Jenkins.get().getMyViewsTabBar(); }
}
