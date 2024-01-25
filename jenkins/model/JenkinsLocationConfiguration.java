package jenkins.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.PersistentDescriptor;
import hudson.util.FormValidation;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import jenkins.util.SystemProperties;
import jenkins.util.UrlHelper;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.QueryParameter;

@Extension(ordinal = 200.0D)
@Symbol({"location"})
public class JenkinsLocationConfiguration extends GlobalConfiguration implements PersistentDescriptor {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean DISABLE_URL_VALIDATION = SystemProperties.getBoolean(JenkinsLocationConfiguration.class.getName() + ".disableUrlValidation");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final int ORDINAL = 200;
  
  @Deprecated
  private String hudsonUrl;
  
  private String adminAddress;
  
  private String jenkinsUrl;
  
  private String charset;
  
  private String useSsl;
  
  @NonNull
  public static JenkinsLocationConfiguration get() { return (JenkinsLocationConfiguration)GlobalConfiguration.all().getInstance(JenkinsLocationConfiguration.class); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public static JenkinsLocationConfiguration getOrDie() {
    config = get();
    if (config == null)
      throw new IllegalStateException("JenkinsLocationConfiguration instance is missing. Probably the Jenkins instance is not fully loaded at this time."); 
    return config;
  }
  
  public void load() {
    XmlFile file = getConfigFile();
    if (!file.exists()) {
      XStream2 xs = new XStream2();
      xs.addCompatibilityAlias("hudson.tasks.Mailer$DescriptorImpl", JenkinsLocationConfiguration.class);
      file = new XmlFile(xs, new File(Jenkins.get().getRootDir(), "hudson.tasks.Mailer.xml"));
      if (file.exists())
        try {
          file.unmarshal(this);
          if (this.jenkinsUrl == null)
            this.jenkinsUrl = this.hudsonUrl; 
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "Failed to load " + file, e);
        }  
    } else {
      super.load();
    } 
    if (!DISABLE_URL_VALIDATION)
      preventRootUrlBeingInvalid(); 
    updateSecureSessionFlag();
  }
  
  @NonNull
  public String getAdminAddress() {
    String v = this.adminAddress;
    if (v == null)
      v = Messages.Mailer_Address_Not_Configured(); 
    return v;
  }
  
  public void setAdminAddress(@CheckForNull String adminAddress) {
    String address = Util.fixEmptyAndTrim(adminAddress);
    if (address != null && address.startsWith("\"") && address.endsWith("\""))
      address = address.substring(1, address.length() - 1); 
    this.adminAddress = address;
    save();
  }
  
  @CheckForNull
  public String getUrl() { return this.jenkinsUrl; }
  
  public void setUrl(@CheckForNull String jenkinsUrl) {
    String url = Util.nullify(jenkinsUrl);
    if (url != null && !url.endsWith("/"))
      url = url + "/"; 
    this.jenkinsUrl = url;
    if (!DISABLE_URL_VALIDATION)
      preventRootUrlBeingInvalid(); 
    save();
    updateSecureSessionFlag();
  }
  
  private void preventRootUrlBeingInvalid() {
    if (this.jenkinsUrl != null && isInvalidRootUrl(this.jenkinsUrl)) {
      LOGGER.log(Level.INFO, "Invalid URL received: {0}, considered as null", this.jenkinsUrl);
      this.jenkinsUrl = null;
    } 
  }
  
  private boolean isInvalidRootUrl(@Nullable String value) { return !UrlHelper.isValidRootUrl(value); }
  
  private void updateSecureSessionFlag() {
    try {
      Method m;
      ServletContext context = (Jenkins.get()).servletContext;
      try {
        m = context.getClass().getMethod("getSessionCookieConfig", new Class[0]);
      } catch (NoSuchMethodException x) {
        LOGGER.log(Level.FINE, "Failed to set secure cookie flag", x);
        return;
      } 
      Object sessionCookieConfig = m.invoke(context, new Object[0]);
      Class scc = Class.forName("javax.servlet.SessionCookieConfig");
      Method setSecure = scc.getMethod("setSecure", new Class[] { boolean.class });
      boolean v = Util.fixNull(this.jenkinsUrl).startsWith("https");
      setSecure.invoke(sessionCookieConfig, new Object[] { Boolean.valueOf(v) });
    } catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof IllegalStateException)
        return; 
      LOGGER.log(Level.WARNING, "Failed to set secure cookie flag", e);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to set secure cookie flag", e);
    } 
  }
  
  public FormValidation doCheckUrl(@QueryParameter String value) {
    if (value.startsWith("http://localhost"))
      return FormValidation.warning(Messages.Mailer_Localhost_Error()); 
    if (!DISABLE_URL_VALIDATION && isInvalidRootUrl(value))
      return FormValidation.error(Messages.Mailer_NotHttp_Error()); 
    return FormValidation.ok();
  }
  
  public FormValidation doCheckAdminAddress(@QueryParameter String value) {
    if (Util.fixNull(value).contains("@"))
      return FormValidation.ok(); 
    return FormValidation.error(Messages.JenkinsLocationConfiguration_does_not_look_like_an_email_address());
  }
  
  private static final Logger LOGGER = Logger.getLogger(JenkinsLocationConfiguration.class.getName());
}
