package jenkins;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.util.HttpResponses;
import java.util.Locale;
import jenkins.util.ResourceBundleUtil;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class I18n implements RootAction {
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return null; }
  
  public String getUrlName() { return "i18n"; }
  
  public HttpResponse doResourceBundle(StaplerRequest request) {
    String baseName = request.getParameter("baseName");
    if (baseName == null)
      return HttpResponses.errorJSON("Mandatory parameter 'baseName' not specified."); 
    String language = request.getParameter("language");
    String country = request.getParameter("country");
    String variant = request.getParameter("variant");
    if (language != null) {
      String[] languageTokens = language.split("-|_");
      language = languageTokens[0];
      if (country == null && languageTokens.length > 1) {
        country = languageTokens[1];
        if (variant == null && languageTokens.length > 2)
          variant = languageTokens[2]; 
      } 
    } 
    try {
      Locale locale = request.getLocale();
      if (language != null && country != null && variant != null) {
        locale = new Locale(language, country, variant);
      } else if (language != null && country != null) {
        locale = new Locale(language, country);
      } else if (language != null) {
        locale = new Locale(language);
      } 
      return HttpResponses.okJSON(ResourceBundleUtil.getBundle(baseName, locale));
    } catch (RuntimeException e) {
      return HttpResponses.errorJSON(e.getMessage());
    } 
  }
}
