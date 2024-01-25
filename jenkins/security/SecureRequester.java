package jenkins.security;

import hudson.ExtensionPoint;
import org.kohsuke.stapler.StaplerRequest;

public interface SecureRequester extends ExtensionPoint {
  boolean permit(StaplerRequest paramStaplerRequest, Object paramObject);
}
