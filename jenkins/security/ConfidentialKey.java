package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.IOException;

public abstract class ConfidentialKey {
  private final String id;
  
  protected ConfidentialKey(String id) { this.id = id; }
  
  @CheckForNull
  protected byte[] load() throws IOException { return ConfidentialStore.get().load(this); }
  
  protected void store(byte[] payload) throws IOException { ConfidentialStore.get().store(this, payload); }
  
  public String getId() { return this.id; }
}
