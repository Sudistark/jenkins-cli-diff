package jenkins.agents;

import hudson.Functions;
import hudson.Util;
import hudson.model.AbstractModelObject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Failure;
import hudson.model.RootAction;
import hudson.model.UpdateCenter;
import hudson.slaves.Cloud;
import hudson.util.FormValidation;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithChildren;
import jenkins.model.ModelObjectWithContextMenu;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class CloudSet extends AbstractModelObject implements Describable<CloudSet>, ModelObjectWithChildren, RootAction, StaplerProxy {
  private static final Logger LOGGER = Logger.getLogger(CloudSet.class.getName());
  
  public Descriptor<CloudSet> getDescriptor() { return Jenkins.get().getDescriptorOrDie(CloudSet.class); }
  
  public Cloud getDynamic(String token) { return Jenkins.get().getCloud(token); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    Jenkins.get().checkPermission(Jenkins.SYSTEM_READ);
    return this;
  }
  
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return Messages.CloudSet_DisplayName(); }
  
  public String getUrlName() { return "cloud"; }
  
  public String getSearchUrl() { return "/cloud/"; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public String getCloudUrl(StaplerRequest request, Jenkins jenkins, Cloud cloud) {
    String context = Functions.getNearestAncestorUrl(request, jenkins);
    if (Jenkins.get().getCloud(cloud.name) != cloud)
      return context + "/cloud/cloudByIndex/" + context + "/"; 
    return context + "/" + context;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public Cloud getCloudByIndex(int index) { return (Cloud)(Jenkins.get()).clouds.get(index); }
  
  public boolean isCloudAvailable() { return !Cloud.all().isEmpty(); }
  
  public String getCloudUpdateCenterCategoryLabel() { return URLEncoder.encode(UpdateCenter.getCategoryDisplayName("cloud"), StandardCharsets.UTF_8); }
  
  public ModelObjectWithContextMenu.ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
    ModelObjectWithContextMenu.ContextMenu m = new ModelObjectWithContextMenu.ContextMenu();
    (Jenkins.get()).clouds.stream().forEach(c -> m.add(c));
    return m;
  }
  
  public Cloud getDynamic(String name, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { return (Jenkins.get()).clouds.getByName(name); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public Jenkins.CloudList getClouds() { return (Jenkins.get()).clouds; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public boolean hasClouds() { return !(Jenkins.get()).clouds.isEmpty(); }
  
  public String checkName(String name) throws Failure {
    if (name == null)
      throw new Failure("Query parameter 'name' is required"); 
    name = name.trim();
    Jenkins.checkGoodName(name);
    if (Jenkins.get().getCloud(name) != null)
      throw new Failure(Messages.CloudSet_CloudAlreadyExists(name)); 
    return name;
  }
  
  public FormValidation doCheckName(@QueryParameter String value) {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (Util.fixEmpty(value) == null)
      return FormValidation.ok(); 
    try {
      checkName(value);
      return FormValidation.ok();
    } catch (Failure e) {
      return FormValidation.error(e.getMessage());
    } 
  }
  
  @RequirePOST
  public void doCreate(StaplerRequest req, StaplerResponse rsp, @QueryParameter String name, @QueryParameter String mode, @QueryParameter String from) throws IOException, ServletException, Descriptor.FormException {
    Jenkins jenkins = Jenkins.get();
    jenkins.checkPermission(Jenkins.ADMINISTER);
    if (mode != null && mode.equals("copy")) {
      name = checkName(name);
      Cloud src = jenkins.getCloud(from);
      if (src == null) {
        if (Util.fixEmpty(from) == null)
          throw new Failure(Messages.CloudSet_SpecifyCloudToCopy()); 
        throw new Failure(Messages.CloudSet_NoSuchCloud(from));
      } 
      String xml = Jenkins.XSTREAM.toXML(src);
      xml = xml.replace("<name>" + src.name + "</name>", "<name>" + name + "</name>");
      Cloud result = (Cloud)Jenkins.XSTREAM.fromXML(xml);
      jenkins.clouds.add(result);
      rsp.sendRedirect2(Functions.getNearestAncestorUrl(req, jenkins) + "/" + Functions.getNearestAncestorUrl(req, jenkins) + "configure");
    } else {
      if (mode == null)
        throw new Failure("No mode given"); 
      Descriptor<Cloud> d = Cloud.all().findByName(mode);
      if (d == null)
        throw new Failure("No node type ‘" + mode + "’ is known"); 
      handleNewCloudPage(d, name, req, rsp);
    } 
  }
  
  private void handleNewCloudPage(Descriptor<Cloud> descriptor, String name, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    checkName(name);
    JSONObject formData = req.getSubmittedForm();
    formData.put("name", name);
    formData.remove("mode");
    req.setAttribute("instance", formData);
    req.setAttribute("descriptor", descriptor);
    req.getView(this, "_new.jelly").forward(req, rsp);
  }
  
  @POST
  public void doDoCreate(StaplerRequest req, StaplerResponse rsp, @QueryParameter String type) throws IOException, ServletException, Descriptor.FormException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    Cloud cloud = (Cloud)Cloud.all().find(type).newInstance(req, req.getSubmittedForm());
    if (!(Jenkins.get()).clouds.add(cloud))
      LOGGER.log(Level.WARNING, () -> "Creating duplicate cloud name " + cloud.name + ". Plugin " + Jenkins.get().getPluginManager().whichPlugin(cloud.getClass()) + " should be updated to support user provided name."); 
    rsp.sendRedirect2(".");
  }
  
  @POST
  public void doReorder(StaplerRequest req, StaplerResponse rsp) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    String[] names = req.getParameterValues("name");
    if (names == null)
      throw new Failure("No cloud names given"); 
    List<String> namesList = Arrays.asList(names);
    ArrayList<Cloud> clouds = new ArrayList<Cloud>((Jenkins.get()).clouds);
    Collections.sort(clouds, Comparator.comparingInt(c -> getIndexOf(namesList, c)));
    (Jenkins.get()).clouds.replaceBy(clouds);
    rsp.sendRedirect2(".");
  }
  
  private static int getIndexOf(List<String> namesList, Cloud cloud) {
    int i = namesList.indexOf(cloud.name);
    return (i == -1) ? Integer.MAX_VALUE : i;
  }
}
