package jenkins.model;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Failure;

public abstract class ProjectNamingStrategy extends Object implements Describable<ProjectNamingStrategy>, ExtensionPoint {
  public ProjectNamingStrategyDescriptor getDescriptor() { return (ProjectNamingStrategyDescriptor)Jenkins.get().getDescriptor(getClass()); }
  
  public static DescriptorExtensionList<ProjectNamingStrategy, ProjectNamingStrategyDescriptor> all() { return Jenkins.get().getDescriptorList(ProjectNamingStrategy.class); }
  
  @Deprecated
  public void checkName(String name) throws Failure {}
  
  public void checkName(String parentName, String name) throws Failure { checkName(name); }
  
  public boolean isForceExistingJobs() { return false; }
  
  public static final ProjectNamingStrategy DEFAULT_NAMING_STRATEGY = new DefaultProjectNamingStrategy();
}
