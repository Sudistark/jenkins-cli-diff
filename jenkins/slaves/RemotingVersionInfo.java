package jenkins.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.VersionNumber;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemotingVersionInfo {
  private static final Logger LOGGER = Logger.getLogger(RemotingVersionInfo.class.getName());
  
  private static final String RESOURCE_NAME = "remoting-info.properties";
  
  @NonNull
  private static VersionNumber EMBEDDED_VERSION;
  
  @NonNull
  private static VersionNumber MINIMUM_SUPPORTED_VERSION;
  
  static  {
    props = new Properties();
    try {
      InputStream is = RemotingVersionInfo.class.getResourceAsStream("remoting-info.properties");
      try {
        if (is != null)
          props.load(is); 
        if (is != null)
          is.close(); 
      } catch (Throwable throwable) {
        if (is != null)
          try {
            is.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to load Remoting Info from remoting-info.properties", e);
    } 
    EMBEDDED_VERSION = extractVersion(props, "remoting.embedded.version");
    MINIMUM_SUPPORTED_VERSION = extractVersion(props, "remoting.minimum.supported.version");
  }
  
  @NonNull
  private static VersionNumber extractVersion(@NonNull Properties props, @NonNull String propertyName) throws ExceptionInInitializerError {
    String prop = props.getProperty(propertyName);
    if (prop == null)
      throw new ExceptionInInitializerError(String.format("Property %s is not defined in %s", new Object[] { propertyName, "remoting-info.properties" })); 
    if (prop.contains("${"))
      throw new ExceptionInInitializerError(String.format("Property %s in %s has unresolved variable(s). Raw value: %s", new Object[] { propertyName, "remoting-info.properties", prop })); 
    try {
      return new VersionNumber(prop);
    } catch (RuntimeException ex) {
      throw new ExceptionInInitializerError(new IOException(
            String.format("Failed to parse version for for property %s in %s. Raw Value: %s", new Object[] { propertyName, "remoting-info.properties", prop }), ex));
    } 
  }
  
  @NonNull
  public static VersionNumber getEmbeddedVersion() { return EMBEDDED_VERSION; }
  
  @NonNull
  public static VersionNumber getMinimumSupportedVersion() { return MINIMUM_SUPPORTED_VERSION; }
}
