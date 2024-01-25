package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Lookup;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public abstract class ConfidentialStore {
  protected abstract void store(ConfidentialKey paramConfidentialKey, byte[] paramArrayOfByte) throws IOException;
  
  @CheckForNull
  protected abstract byte[] load(ConfidentialKey paramConfidentialKey) throws IOException;
  
  abstract SecureRandom secureRandom();
  
  public abstract byte[] randomBytes(int paramInt);
  
  @NonNull
  public static ConfidentialStore get() {
    j = Jenkins.getInstanceOrNull();
    if (j == null)
      return Mock.INSTANCE; 
    Lookup lookup = j.lookup;
    ConfidentialStore confidentialStore = (ConfidentialStore)lookup.get(ConfidentialStore.class);
    if (confidentialStore == null) {
      DefaultConfidentialStore defaultConfidentialStore;
      try {
        Iterator<ConfidentialStore> it = ServiceLoader.load(ConfidentialStore.class, ConfidentialStore.class.getClassLoader()).iterator();
        if (it.hasNext())
          confidentialStore = (ConfidentialStore)it.next(); 
      } catch (ServiceConfigurationError e) {
        LOGGER.log(Level.WARNING, "Failed to list up ConfidentialStore implementations", e);
      } 
      if (confidentialStore == null)
        try {
          defaultConfidentialStore = new DefaultConfidentialStore();
        } catch (Exception e) {
          throw new Error(e);
        }  
      confidentialStore = (ConfidentialStore)lookup.setIfNull(ConfidentialStore.class, defaultConfidentialStore);
    } 
    return confidentialStore;
  }
  
  private static final Logger LOGGER = Logger.getLogger(ConfidentialStore.class.getName());
}
