package hudson.scm;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.util.DescriptorList;
import java.util.List;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;

public class SCMS {
  @Deprecated
  public static final List<SCMDescriptor<?>> SCMS = new DescriptorList(SCM.class);
  
  public static SCM parseSCM(StaplerRequest req, AbstractProject target) throws Descriptor.FormException, ServletException {
    NullSCM nullSCM = (SCM)SCM.all().newInstanceFromRadioList(req.getSubmittedForm().getJSONObject("scm"));
    if (nullSCM == null)
      nullSCM = new NullSCM(); 
    nullSCM.getDescriptor().incrementGeneration();
    return nullSCM;
  }
  
  @Deprecated
  public static SCM parseSCM(StaplerRequest req) throws Descriptor.FormException, ServletException { return parseSCM(req, null); }
}
