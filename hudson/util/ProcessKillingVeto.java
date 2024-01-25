package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.Collections;
import java.util.List;
import jenkins.util.JenkinsJVM;

public abstract class ProcessKillingVeto implements ExtensionPoint {
  public static List<ProcessKillingVeto> all() {
    if (JenkinsJVM.isJenkinsJVM())
      return _all(); 
    return Collections.emptyList();
  }
  
  private static List<ProcessKillingVeto> _all() { return ExtensionList.lookup(ProcessKillingVeto.class); }
  
  @CheckForNull
  public abstract VetoCause vetoProcessKilling(@NonNull ProcessTreeRemoting.IOSProcess paramIOSProcess);
}
