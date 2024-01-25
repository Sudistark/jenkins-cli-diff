package hudson.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.User;
import hudson.model.View;
import hudson.slaves.Cloud;
import hudson.util.DescriptorList;
import java.util.Collection;
import jenkins.model.Jenkins;
import jenkins.security.stapler.StaplerAccessibleType;
import org.springframework.security.core.Authentication;

@StaplerAccessibleType
public abstract class AuthorizationStrategy extends AbstractDescribableImpl<AuthorizationStrategy> implements ExtensionPoint {
  @Deprecated
  @NonNull
  public ACL getACL(@NonNull AbstractProject<?, ?> project) { return getACL(project); }
  
  @NonNull
  public ACL getACL(@NonNull Job<?, ?> project) { return getRootACL(); }
  
  @NonNull
  public ACL getACL(@NonNull View item) {
    return ACL.lambda2((a, permission) -> {
          ACL base = item.getOwner().getACL();
          boolean hasPermission = base.hasPermission2(a, permission);
          if (!hasPermission && permission == View.READ)
            return Boolean.valueOf((base.hasPermission2(a, View.CONFIGURE) || !item.getItems().isEmpty())); 
          return Boolean.valueOf(hasPermission);
        });
  }
  
  @NonNull
  public ACL getACL(@NonNull AbstractItem item) { return getRootACL(); }
  
  @NonNull
  public ACL getACL(@NonNull User user) { return getRootACL(); }
  
  @NonNull
  public ACL getACL(@NonNull Computer computer) { return getACL(computer.getNode()); }
  
  @NonNull
  public ACL getACL(@NonNull Cloud cloud) { return getRootACL(); }
  
  @NonNull
  public ACL getACL(@NonNull Node node) { return getRootACL(); }
  
  @NonNull
  public static DescriptorExtensionList<AuthorizationStrategy, Descriptor<AuthorizationStrategy>> all() { return Jenkins.get().getDescriptorList(AuthorizationStrategy.class); }
  
  @Deprecated
  public static final DescriptorList<AuthorizationStrategy> LIST = new DescriptorList(AuthorizationStrategy.class);
  
  public static final AuthorizationStrategy UNSECURED = new Unsecured();
  
  @NonNull
  public abstract ACL getRootACL();
  
  @NonNull
  public abstract Collection<String> getGroups();
}
