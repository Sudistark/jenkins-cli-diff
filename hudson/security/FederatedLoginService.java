package hudson.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;

public abstract class FederatedLoginService implements ExtensionPoint {
  @NonNull
  public abstract String getUrlName();
  
  @NonNull
  public abstract Class<? extends FederatedLoginServiceUserProperty> getUserPropertyClass();
  
  public static ExtensionList<FederatedLoginService> all() { return ExtensionList.lookup(FederatedLoginService.class); }
}
