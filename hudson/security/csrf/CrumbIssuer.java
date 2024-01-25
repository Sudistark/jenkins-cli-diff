package hudson.security.csrf;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.init.Initializer;
import hudson.model.Api;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.MultipartFormDataParser;
import javax.servlet.ServletRequest;
import jenkins.model.Jenkins;
import jenkins.security.stapler.StaplerAccessibleType;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
@StaplerAccessibleType
public abstract class CrumbIssuer extends Object implements Describable<CrumbIssuer>, ExtensionPoint {
  private static final String CRUMB_ATTRIBUTE = CrumbIssuer.class.getName() + "_crumb";
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String DEFAULT_CRUMB_NAME = "Jenkins-Crumb";
  
  @Exported
  public String getCrumbRequestField() { return getDescriptor().getCrumbRequestField(); }
  
  @Exported
  public String getCrumb() { return getCrumb(Stapler.getCurrentRequest()); }
  
  public String getCrumb(ServletRequest request) {
    String crumb = null;
    if (request != null)
      crumb = (String)request.getAttribute(CRUMB_ATTRIBUTE); 
    if (crumb == null) {
      crumb = issueCrumb(request, getDescriptor().getCrumbSalt());
      if (request != null)
        if (crumb != null && crumb.length() > 0) {
          request.setAttribute(CRUMB_ATTRIBUTE, crumb);
        } else {
          request.removeAttribute(CRUMB_ATTRIBUTE);
        }  
    } 
    return crumb;
  }
  
  public boolean validateCrumb(ServletRequest request) {
    CrumbIssuerDescriptor<CrumbIssuer> desc = getDescriptor();
    String crumbField = desc.getCrumbRequestField();
    String crumbSalt = desc.getCrumbSalt();
    return validateCrumb(request, crumbSalt, request.getParameter(crumbField));
  }
  
  public boolean validateCrumb(ServletRequest request, MultipartFormDataParser parser) {
    CrumbIssuerDescriptor<CrumbIssuer> desc = getDescriptor();
    String crumbField = desc.getCrumbRequestField();
    String crumbSalt = desc.getCrumbSalt();
    return validateCrumb(request, crumbSalt, parser.get(crumbField));
  }
  
  public CrumbIssuerDescriptor<CrumbIssuer> getDescriptor() { return (CrumbIssuerDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public static DescriptorExtensionList<CrumbIssuer, Descriptor<CrumbIssuer>> all() { return Jenkins.get().getDescriptorList(CrumbIssuer.class); }
  
  public Api getApi() { return new RestrictedApi(this); }
  
  @Initializer
  public static void initStaplerCrumbIssuer() { WebApp.get((Jenkins.get()).servletContext).setCrumbIssuer(new Object()); }
  
  protected abstract String issueCrumb(ServletRequest paramServletRequest, String paramString);
  
  public abstract boolean validateCrumb(ServletRequest paramServletRequest, String paramString1, String paramString2);
}
