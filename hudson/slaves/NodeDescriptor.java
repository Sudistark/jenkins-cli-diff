package hudson.slaves;

import hudson.DescriptorExtensionList;
import hudson.Util;
import hudson.model.ComputerSet;
import hudson.model.Descriptor;
import hudson.model.Failure;
import hudson.model.Node;
import hudson.util.DescriptorList;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class NodeDescriptor extends Descriptor<Node> {
  protected NodeDescriptor(Class<? extends Node> clazz) { super(clazz); }
  
  protected NodeDescriptor() {}
  
  public boolean isInstantiable() { return true; }
  
  public final String newInstanceDetailPage() { return "/" + this.clazz.getName().replace('.', '/').replace('$', '/') + "/newInstanceDetail.jelly"; }
  
  public void handleNewNodePage(ComputerSet computerSet, String name, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    computerSet.checkName(name);
    req.setAttribute("descriptor", this);
    req.getView(computerSet, "_new.jelly").forward(req, rsp);
  }
  
  public String getConfigPage() { return getViewPage(this.clazz, "configure-entries.jelly"); }
  
  public FormValidation doCheckName(@QueryParameter String value) {
    String name = Util.fixEmptyAndTrim(value);
    if (name == null)
      return FormValidation.error(Messages.NodeDescriptor_CheckName_Mandatory()); 
    try {
      Jenkins.checkGoodName(name);
    } catch (Failure f) {
      return FormValidation.error(f.getMessage());
    } 
    return FormValidation.ok();
  }
  
  public static DescriptorExtensionList<Node, NodeDescriptor> all() { return Jenkins.get().getDescriptorList(Node.class); }
  
  @Deprecated
  public static final DescriptorList<Node> ALL = new DescriptorList(Node.class);
  
  public static List<NodeDescriptor> allInstantiable() {
    r = new ArrayList();
    for (NodeDescriptor d : all()) {
      if (d.isInstantiable())
        r.add(d); 
    } 
    return r;
  }
}
