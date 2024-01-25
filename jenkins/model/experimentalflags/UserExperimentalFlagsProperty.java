package jenkins.model.experimentalflags;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.UserProperty;
import java.util.HashMap;
import java.util.Map;
import org.kohsuke.stapler.DataBoundConstructor;

public class UserExperimentalFlagsProperty extends UserProperty {
  private Map<String, String> flags = new HashMap();
  
  @DataBoundConstructor
  public UserExperimentalFlagsProperty() {}
  
  public UserExperimentalFlagsProperty(Map<String, String> flags) { this.flags = new HashMap(flags); }
  
  @CheckForNull
  public Object getFlagValue(String flagKey) { return this.flags.get(flagKey); }
}
