package hudson.tasks;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.util.DescriptorList;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;

public class BuildWrappers {
  @Deprecated
  public static final List<Descriptor<BuildWrapper>> WRAPPERS = new DescriptorList(BuildWrapper.class);
  
  public static List<Descriptor<BuildWrapper>> getFor(AbstractProject<?, ?> project) {
    List<Descriptor<BuildWrapper>> result = new ArrayList<Descriptor<BuildWrapper>>();
    Descriptor pd = Jenkins.get().getDescriptor(project.getClass());
    for (Descriptor<BuildWrapper> w : BuildWrapper.all()) {
      if (pd instanceof AbstractProject.AbstractProjectDescriptor && !((AbstractProject.AbstractProjectDescriptor)pd).isApplicable(w))
        continue; 
      if (w instanceof BuildWrapperDescriptor) {
        BuildWrapperDescriptor bwd = (BuildWrapperDescriptor)w;
        if (bwd.isApplicable(project))
          result.add(bwd); 
        continue;
      } 
      result.add(w);
    } 
    return result;
  }
}
