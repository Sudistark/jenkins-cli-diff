package jenkins.scm;

import hudson.DescriptorExtensionList;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;

public abstract class SCMCheckoutStrategyDescriptor extends Descriptor<SCMCheckoutStrategy> {
  protected SCMCheckoutStrategyDescriptor(Class<? extends SCMCheckoutStrategy> clazz) { super(clazz); }
  
  protected SCMCheckoutStrategyDescriptor() {}
  
  public abstract boolean isApplicable(AbstractProject paramAbstractProject);
  
  public static DescriptorExtensionList<SCMCheckoutStrategy, SCMCheckoutStrategyDescriptor> all() { return Jenkins.get().getDescriptorList(SCMCheckoutStrategy.class); }
  
  public static List<SCMCheckoutStrategyDescriptor> _for(AbstractProject p) {
    List<SCMCheckoutStrategyDescriptor> r = new ArrayList<SCMCheckoutStrategyDescriptor>();
    for (SCMCheckoutStrategyDescriptor d : all()) {
      if (d.isApplicable(p))
        r.add(d); 
    } 
    return r;
  }
}
