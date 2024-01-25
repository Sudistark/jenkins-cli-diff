package hudson.security;

import hudson.model.UserProperty;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FederatedLoginServiceUserProperty extends UserProperty {
  protected final Set<String> identifiers;
  
  protected FederatedLoginServiceUserProperty(Collection<String> identifiers) { this.identifiers = new HashSet(identifiers); }
  
  public boolean has(String identifier) { return this.identifiers.contains(identifier); }
  
  public Collection<String> getIdentifiers() { return Collections.unmodifiableSet(this.identifiers); }
  
  public void addIdentifier(String id) throws IOException {
    this.identifiers.add(id);
    this.user.save();
  }
}
