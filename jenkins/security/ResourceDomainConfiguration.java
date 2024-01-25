package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.util.FormValidation;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import jenkins.diagnostics.RootUrlNotSetMonitor;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.model.identity.InstanceIdentityProvider;
import jenkins.util.UrlHelper;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

@Extension(ordinal = 199.0D)
@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
@Symbol({"resourceRoot"})
public final class ResourceDomainConfiguration extends GlobalConfiguration {
  private static final Logger LOGGER = Logger.getLogger(ResourceDomainConfiguration.class.getName());
  
  private String url;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public ResourceDomainConfiguration() { load(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @POST
  public FormValidation doCheckUrl(@QueryParameter("url") String resourceRootUrlString) {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    return checkUrl(resourceRootUrlString, true);
  }
  
  private FormValidation checkUrl(String resourceRootUrlString, boolean allowOnlineIdentityCheck) {
    URL resourceRootUrl;
    String jenkinsRootUrlString = JenkinsLocationConfiguration.get().getUrl();
    if (((RootUrlNotSetMonitor)ExtensionList.lookupSingleton(RootUrlNotSetMonitor.class)).isActivated() || jenkinsRootUrlString == null)
      return FormValidation.warning(Messages.ResourceDomainConfiguration_NeedsRootURL()); 
    resourceRootUrlString = Util.fixEmptyAndTrim(resourceRootUrlString);
    if (resourceRootUrlString == null)
      return FormValidation.ok(Messages.ResourceDomainConfiguration_Empty()); 
    if (!UrlHelper.isValidRootUrl(resourceRootUrlString))
      return FormValidation.error(Messages.ResourceDomainConfiguration_Invalid()); 
    if (!resourceRootUrlString.endsWith("/"))
      resourceRootUrlString = resourceRootUrlString + "/"; 
    try {
      resourceRootUrl = new URL(resourceRootUrlString);
    } catch (MalformedURLException ex) {
      return FormValidation.error(Messages.ResourceDomainConfiguration_Invalid());
    } 
    String resourceRootUrlHost = resourceRootUrl.getHost();
    try {
      String jenkinsRootUrlHost = (new URL(jenkinsRootUrlString)).getHost();
      if (jenkinsRootUrlHost.equals(resourceRootUrlHost))
        return FormValidation.error(Messages.ResourceDomainConfiguration_SameAsJenkinsRoot()); 
    } catch (Exception ex) {
      LOGGER.log(Level.CONFIG, "Failed to create URL from the existing Jenkins URL", ex);
      return FormValidation.error(Messages.ResourceDomainConfiguration_InvalidRootURL(ex.getMessage()));
    } 
    StaplerRequest currentRequest = Stapler.getCurrentRequest();
    if (currentRequest != null) {
      String currentRequestHost = currentRequest.getServerName();
      if (currentRequestHost.equals(resourceRootUrlHost))
        return FormValidation.error(Messages.ResourceDomainConfiguration_SameAsCurrent()); 
    } 
    if (!allowOnlineIdentityCheck)
      return FormValidation.ok(); 
    try {
      URLConnection urlConnection = (new URL(resourceRootUrlString + "instance-identity/")).openConnection();
      if (urlConnection instanceof HttpURLConnection) {
        HttpURLConnection httpURLConnection = (HttpURLConnection)urlConnection;
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == 200) {
          String identityHeader = urlConnection.getHeaderField("X-Instance-Identity");
          if (identityHeader == null)
            return FormValidation.warning(Messages.ResourceDomainConfiguration_NotJenkins()); 
          RSAPublicKey publicKey = (RSAPublicKey)InstanceIdentityProvider.RSA.getPublicKey();
          if (publicKey != null) {
            String identity = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            if (identity.equals(identityHeader))
              return FormValidation.ok(Messages.ResourceDomainConfiguration_ThisJenkins()); 
            return FormValidation.warning(Messages.ResourceDomainConfiguration_OtherJenkins());
          } 
          return FormValidation.warning(Messages.ResourceDomainConfiguration_SomeJenkins());
        } 
        String responseMessage = httpURLConnection.getResponseMessage();
        if (responseCode == 404) {
          String responseBody = String.join("", IOUtils.readLines(httpURLConnection.getErrorStream(), StandardCharsets.UTF_8));
          if (responseMessage.contains("Jenkins serves only static files on this domain.") || responseBody.contains("Jenkins serves only static files on this domain."))
            return FormValidation.ok(Messages.ResourceDomainConfiguration_ResourceResponse()); 
        } 
        return FormValidation.error(Messages.ResourceDomainConfiguration_FailedIdentityCheck(Integer.valueOf(responseCode), responseMessage));
      } 
      return FormValidation.error(Messages.ResourceDomainConfiguration_Invalid());
    } catch (MalformedURLException ex) {
      LOGGER.log(Level.FINE, "MalformedURLException occurred during instance identity check for " + resourceRootUrlString, ex);
      return FormValidation.error(Messages.ResourceDomainConfiguration_Exception(ex.getMessage()));
    } catch (IOException ex) {
      LOGGER.log(Level.FINE, "IOException occurred during instance identity check for " + resourceRootUrlString, ex);
      return FormValidation.warning(Messages.ResourceDomainConfiguration_IOException(ex.getMessage()));
    } 
  }
  
  @CheckForNull
  public String getUrl() { return this.url; }
  
  public void setUrl(@CheckForNull String url) {
    if ((checkUrl(url, false)).kind == FormValidation.Kind.OK) {
      url = Util.fixEmpty(url);
      if (url != null && !url.endsWith("/"))
        url = url + "/"; 
      this.url = url;
      save();
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isResourceRequest(HttpServletRequest req) {
    if (!isResourceDomainConfigured())
      return false; 
    String resourceRootUrl = get().getUrl();
    try {
      URL url = new URL(resourceRootUrl);
      String resourceRootHost = url.getHost();
      if (!resourceRootHost.equalsIgnoreCase(req.getServerName()))
        return false; 
      int resourceRootPort = url.getPort();
      if (resourceRootPort == -1)
        resourceRootPort = url.getDefaultPort(); 
      int requestedPort = req.getServerPort();
      if (requestedPort != resourceRootPort)
        return false; 
    } catch (MalformedURLException ex) {
      return false;
    } 
    return true;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isResourceDomainConfigured() {
    resourceRootUrl = get().getUrl();
    if (resourceRootUrl == null || resourceRootUrl.isEmpty())
      return false; 
    return (Util.nullify(JenkinsLocationConfiguration.get().getUrl()) != null);
  }
  
  public static ResourceDomainConfiguration get() { return (ResourceDomainConfiguration)ExtensionList.lookupSingleton(ResourceDomainConfiguration.class); }
}
