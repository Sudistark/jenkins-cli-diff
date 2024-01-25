package jenkins.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import java.util.List;

public abstract class QueueItemAuthenticatorProvider implements ExtensionPoint {
  @NonNull
  public abstract List<QueueItemAuthenticator> getAuthenticators();
  
  public static Iterable<QueueItemAuthenticator> authenticators() { return new IterableImpl(); }
}
