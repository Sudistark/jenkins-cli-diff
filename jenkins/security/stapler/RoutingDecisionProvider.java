package jenkins.security.stapler;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;

public abstract class RoutingDecisionProvider implements ExtensionPoint {
  @NonNull
  public abstract Decision decide(@NonNull String paramString);
}
