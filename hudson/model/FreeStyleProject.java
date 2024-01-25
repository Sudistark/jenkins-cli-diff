package hudson.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;

public class FreeStyleProject extends Project<FreeStyleProject, FreeStyleBuild> implements TopLevelItem {
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_PKGPROTECT"}, justification = "for backward compatibility")
  public static DescriptorImpl DESCRIPTOR;
  
  @Deprecated
  public FreeStyleProject(Jenkins parent, String name) { super(parent, name); }
  
  public FreeStyleProject(ItemGroup parent, String name) { super(parent, name); }
  
  protected Class<FreeStyleBuild> getBuildClass() { return FreeStyleBuild.class; }
  
  public DescriptorImpl getDescriptor() { return (DescriptorImpl)Jenkins.get().getDescriptorOrDie(getClass()); }
}
