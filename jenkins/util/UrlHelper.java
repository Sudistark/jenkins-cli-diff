package jenkins.util;

import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class UrlHelper {
  private static String DOMAIN_REGEX = System.getProperty(UrlHelper.class
      .getName() + ".DOMAIN_REGEX", "^\\w(-*(\\.|\\w))*\\.*(:\\d{1,5})?$");
  
  public static boolean isValidRootUrl(String url) {
    CustomUrlValidator customUrlValidator = new CustomUrlValidator();
    return customUrlValidator.isValid(url);
  }
}
