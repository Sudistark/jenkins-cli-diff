package jenkins.security;

import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;

public class FIPS140 {
  private static final Logger LOGGER = Logger.getLogger(FIPS140.class.getName());
  
  private static final boolean FIPS_COMPLIANCE_MODE = SystemProperties.getBoolean(FIPS140.class.getName() + ".COMPLIANCE");
  
  static  {
    if (useCompliantAlgorithms())
      LOGGER.log(Level.CONFIG, "System has been asked to run in FIPS-140 compliant mode"); 
  }
  
  public static boolean useCompliantAlgorithms() { return FIPS_COMPLIANCE_MODE; }
}
