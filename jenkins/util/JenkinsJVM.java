package jenkins.util;

import org.kohsuke.accmod.Restricted;

public class JenkinsJVM {
  private static boolean jenkinsJVM;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  protected JenkinsJVM() { throw new IllegalAccessError("Utility class"); }
  
  public static boolean isJenkinsJVM() { return jenkinsJVM; }
  
  public static void checkJenkinsJVM() {
    if (!isJenkinsJVM())
      throw new IllegalStateException("Not running on the Jenkins controller JVM"); 
  }
  
  public static void checkNotJenkinsJVM() {
    if (isJenkinsJVM())
      throw new IllegalStateException("Running on the Jenkins controller JVM"); 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  protected static void setJenkinsJVM(boolean jenkinsJVM) { JenkinsJVM.jenkinsJVM = jenkinsJVM; }
}
