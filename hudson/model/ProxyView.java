package hudson.model;

import hudson.Util;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class ProxyView extends View implements StaplerFallback {
  private String proxiedViewName;
  
  @DataBoundConstructor
  public ProxyView(String name) {
    super(name);
    if (Jenkins.get().getView(name) != null)
      this.proxiedViewName = name; 
  }
  
  public View getProxiedView() {
    if (this.proxiedViewName == null)
      return Jenkins.get().getPrimaryView(); 
    return Jenkins.get().getView(this.proxiedViewName);
  }
  
  public String getProxiedViewName() { return this.proxiedViewName; }
  
  public void setProxiedViewName(String proxiedViewName) { this.proxiedViewName = proxiedViewName; }
  
  public Collection<TopLevelItem> getItems() { return getProxiedView().getItems(); }
  
  public boolean contains(TopLevelItem item) { return getProxiedView().contains(item); }
  
  public TopLevelItem getItem(String name) { return getProxiedView().getItem(name); }
  
  protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
    String proxiedViewName = req.getSubmittedForm().getString("proxiedViewName");
    if (Jenkins.get().getView(proxiedViewName) == null)
      throw new Descriptor.FormException("Not an existing global view", "proxiedViewName"); 
    this.proxiedViewName = proxiedViewName;
  }
  
  @RequirePOST
  public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { return getProxiedView().doCreateItem(req, rsp); }
  
  public FormValidation doViewExistsCheck(@QueryParameter String value) {
    checkPermission(View.CREATE);
    String view = Util.fixEmpty(value);
    if (view == null)
      return FormValidation.ok(); 
    if (Jenkins.get().getView(view) != null)
      return FormValidation.ok(); 
    return FormValidation.error(Messages.ProxyView_NoSuchViewExists(value));
  }
  
  public Object getStaplerFallback() { return getProxiedView(); }
}
