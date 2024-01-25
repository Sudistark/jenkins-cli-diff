package jenkins.model;

import hudson.Extension;
import hudson.Functions;
import hudson.model.Descriptor;
import hudson.model.User;
import java.util.Map;

@Extension
public class DefaultUserCanonicalIdResolver extends User.CanonicalIdResolver {
  public String resolveCanonicalId(String idOrFullName, Map<String, ?> context) {
    String id = idOrFullName.replace('\\', '_').replace('/', '_').replace('<', '_').replace('>', '_');
    if (Functions.isWindows())
      id = id.replace(':', '_'); 
    return id;
  }
  
  public int getPriority() { return Integer.MIN_VALUE; }
  
  public Descriptor<User.CanonicalIdResolver> getDescriptor() { return DESCRIPTOR; }
  
  public static final Descriptor<User.CanonicalIdResolver> DESCRIPTOR = new Object();
}
