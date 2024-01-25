package jenkins.install;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.Extension;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.model.DownloadService;
import hudson.model.PageDecorator;
import hudson.model.UpdateCenter;
import hudson.model.UpdateSite;
import hudson.model.User;
import hudson.security.AccountCreationFailedException;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.HudsonPrivateSecurityRealm;
import hudson.security.SecurityRealm;
import hudson.security.csrf.CrumbIssuer;
import hudson.security.csrf.GlobalCrumbIssuerConfiguration;
import hudson.util.FormValidation;
import hudson.util.HttpResponses;
import hudson.util.PluginServletFilter;
import hudson.util.VersionNumber;
import java.io.File;
import java.io.IOException;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.security.ApiTokenProperty;
import jenkins.security.apitoken.TokenUuidAndPlainValue;
import jenkins.security.seed.UserSeedProperty;
import jenkins.util.SystemProperties;
import jenkins.util.UrlHelper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class SetupWizard extends PageDecorator {
  public SetupWizard() {
    this.FORCE_SETUP_WIZARD_FILTER = new Object(this);
    checkFilter();
  }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "used in several plugins")
  public static String initialSetupAdminUserName = "admin";
  
  private static final Logger LOGGER = Logger.getLogger(SetupWizard.class.getName());
  
  private static final String ADMIN_INITIAL_API_TOKEN_PROPERTY_NAME = SetupWizard.class.getName() + ".adminInitialApiToken";
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  private static String ADMIN_INITIAL_API_TOKEN = SystemProperties.getString(ADMIN_INITIAL_API_TOKEN_PROPERTY_NAME);
  
  private final Filter FORCE_SETUP_WIZARD_FILTER;
  
  @NonNull
  public String getDisplayName() { return Messages.SetupWizard_DisplayName(); }
  
  void init(boolean newInstall) throws IOException, InterruptedException {
    Jenkins jenkins = Jenkins.get();
    if (newInstall) {
      FilePath iapf = getInitialAdminPasswordFile();
      if (jenkins.getSecurityRealm() == null || jenkins.getSecurityRealm() == SecurityRealm.NO_AUTHENTICATION) {
        BulkChange bc = new BulkChange(jenkins);
        try {
          HudsonPrivateSecurityRealm securityRealm = new HudsonPrivateSecurityRealm(false, false, null);
          jenkins.setSecurityRealm(securityRealm);
          String randomUUID = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ENGLISH);
          User initialAdmin = securityRealm.createAccount(initialSetupAdminUserName, randomUUID);
          if (ADMIN_INITIAL_API_TOKEN != null)
            createInitialApiToken(initialAdmin); 
          iapf.touch(System.currentTimeMillis());
          iapf.chmod(416);
          iapf.write(randomUUID + randomUUID, "UTF-8");
          FullControlOnceLoggedInAuthorizationStrategy authStrategy = new FullControlOnceLoggedInAuthorizationStrategy();
          authStrategy.setAllowAnonymousRead(false);
          jenkins.setAuthorizationStrategy(authStrategy);
          jenkins.setSlaveAgentPort(SystemProperties.getInteger(Jenkins.class.getName() + ".slaveAgentPort", Integer.valueOf(-1)).intValue());
          jenkins.setCrumbIssuer(GlobalCrumbIssuerConfiguration.createDefaultCrumbIssuer());
          jenkins.save();
          bc.commit();
          bc.close();
        } catch (Throwable throwable) {
          try {
            bc.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } 
      if (iapf.exists()) {
        String setupKey = iapf.readToString().trim();
        String ls = System.lineSeparator();
        LOGGER.info(ls + ls + "*************************************************************" + ls + "*************************************************************" + ls + "*************************************************************" + ls + ls + "Jenkins initial setup is required. An admin user has been created and a password generated." + ls + "Please use the following password to proceed to installation:" + ls + ls + ls + setupKey + ls + "This may also be found at: " + ls + iapf.getRemote() + ls + "*************************************************************" + ls + "*************************************************************" + ls + "*************************************************************" + ls);
      } 
    } 
    try {
      UpdateCenter.updateDefaultSite();
    } catch (RuntimeException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
    } 
  }
  
  @SuppressFBWarnings(value = {"UNSAFE_HASH_EQUALS"}, justification = "only checked against true")
  private void createInitialApiToken(User user) throws IOException, InterruptedException {
    ApiTokenProperty apiTokenProperty = (ApiTokenProperty)user.getProperty(ApiTokenProperty.class);
    String sysProp = ADMIN_INITIAL_API_TOKEN;
    if (sysProp.equals("true")) {
      TokenUuidAndPlainValue tokenUuidAndPlainValue = apiTokenProperty.generateNewToken("random-generation-during-setup-wizard");
      FilePath fp = getInitialAdminApiTokenFile();
      fp.touch(System.currentTimeMillis());
      fp.chmod(416);
      fp.write(tokenUuidAndPlainValue.plainValue, StandardCharsets.UTF_8.name());
      LOGGER.log(Level.INFO, "The API Token was randomly generated and the information was put in {0}", fp.getRemote());
    } else {
      String plainText;
      if (sysProp.startsWith("@")) {
        Path apiTokenFile;
        String apiTokenStr = sysProp.substring(1);
        try {
          apiTokenFile = Paths.get(apiTokenStr, new String[0]);
        } catch (InvalidPathException e) {
          LOGGER.log(Level.WARNING, "The API Token cannot be retrieved from an invalid path: {0}", apiTokenStr);
          return;
        } 
        if (!Files.exists(apiTokenFile, new java.nio.file.LinkOption[0])) {
          LOGGER.log(Level.WARNING, "The API Token cannot be retrieved from a non-existing file: {0}", apiTokenFile);
          return;
        } 
        try {
          plainText = Files.readString(apiTokenFile, StandardCharsets.UTF_8);
          LOGGER.log(Level.INFO, "API Token generated using contents of file: {0}", apiTokenFile.toAbsolutePath());
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, String.format("The API Token cannot be retrieved from the file: %s", new Object[] { apiTokenFile }), e);
          return;
        } 
      } else {
        LOGGER.log(Level.INFO, "API Token generated using system property: {0}", ADMIN_INITIAL_API_TOKEN_PROPERTY_NAME);
        plainText = sysProp;
      } 
      try {
        apiTokenProperty.addFixedNewToken("fix-generation-during-setup-wizard", plainText);
      } catch (IllegalArgumentException e) {
        String constraintFailureMessage = e.getMessage();
        LOGGER.log(Level.WARNING, "The API Token cannot be generated using the provided value due to: {0}", constraintFailureMessage);
      } 
    } 
  }
  
  private void setUpFilter() {
    try {
      if (!PluginServletFilter.hasFilter(this.FORCE_SETUP_WIZARD_FILTER))
        PluginServletFilter.addFilter(this.FORCE_SETUP_WIZARD_FILTER); 
    } catch (ServletException e) {
      throw new RuntimeException("Unable to add PluginServletFilter for the SetupWizard", e);
    } 
  }
  
  private void tearDownFilter() {
    try {
      if (PluginServletFilter.hasFilter(this.FORCE_SETUP_WIZARD_FILTER))
        PluginServletFilter.removeFilter(this.FORCE_SETUP_WIZARD_FILTER); 
    } catch (ServletException e) {
      throw new RuntimeException("Unable to remove PluginServletFilter for the SetupWizard", e);
    } 
  }
  
  public boolean isUsingSecurityToken() {
    try {
      return (!Jenkins.get().getInstallState().isSetupComplete() && isUsingSecurityDefaults());
    } catch (RuntimeException runtimeException) {
      return false;
    } 
  }
  
  boolean isUsingSecurityDefaults() {
    Jenkins j = Jenkins.get();
    if (j.getSecurityRealm() instanceof HudsonPrivateSecurityRealm) {
      HudsonPrivateSecurityRealm securityRealm = (HudsonPrivateSecurityRealm)j.getSecurityRealm();
      try {
        if (securityRealm.getAllUsers().size() == 1) {
          HudsonPrivateSecurityRealm.Details details = securityRealm.load(initialSetupAdminUserName);
          FilePath iapf = getInitialAdminPasswordFile();
          if (iapf.exists() && details.isPasswordCorrect(iapf.readToString().trim()))
            return true; 
        } 
      } catch (UsernameNotFoundException|IOException|InterruptedException e) {
        return false;
      } 
    } 
    return false;
  }
  
  @POST
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public HttpResponse doCreateAdminUser(StaplerRequest req, StaplerResponse rsp) throws IOException {
    Jenkins j = Jenkins.get();
    j.checkPermission(Jenkins.ADMINISTER);
    HudsonPrivateSecurityRealm securityRealm = (HudsonPrivateSecurityRealm)j.getSecurityRealm();
    admin = securityRealm.getUser(initialSetupAdminUserName);
    try {
      ApiTokenProperty initialApiTokenProperty = null;
      if (admin != null) {
        initialApiTokenProperty = (ApiTokenProperty)admin.getProperty(ApiTokenProperty.class);
        admin.delete();
      } 
      User newUser = securityRealm.createAccountFromSetupWizard(req);
      if (admin != null)
        admin = null; 
      if (initialApiTokenProperty != null)
        newUser.addProperty(initialApiTokenProperty); 
      try {
        getInitialAdminPasswordFile().delete();
      } catch (InterruptedException e) {
        throw new IOException(e);
      } 
      try {
        FilePath fp = getInitialAdminApiTokenFile();
        if (fp.exists())
          fp.delete(); 
      } catch (InterruptedException e) {
        throw new IOException(e);
      } 
      InstallUtil.proceedToNextStateFrom(InstallState.CREATE_ADMIN_USER);
      UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(newUser.getId(), req.getParameter("password1"));
      Authentication authentication = (securityRealm.getSecurityComponents()).manager2.authenticate(usernamePasswordAuthenticationToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      HttpSession session = req.getSession(false);
      if (session != null)
        session.invalidate(); 
      HttpSession newSession = req.getSession(true);
      UserSeedProperty userSeed = (UserSeedProperty)newUser.getProperty(UserSeedProperty.class);
      String sessionSeed = userSeed.getSeed();
      newSession.setAttribute("_JENKINS_SESSION_SEED", sessionSeed);
      CrumbIssuer crumbIssuer = Jenkins.get().getCrumbIssuer();
      JSONObject data = new JSONObject();
      if (crumbIssuer != null)
        data.accumulate("crumbRequestField", crumbIssuer.getCrumbRequestField()).accumulate("crumb", crumbIssuer.getCrumb(req)); 
      return HttpResponses.okJSON(data);
    } catch (AccountCreationFailedException e) {
      rsp.setStatus(422);
      return HttpResponses.forwardToView(securityRealm, "/jenkins/install/SetupWizard/setupWizardFirstUser.jelly");
    } finally {
      if (admin != null)
        admin.save(); 
    } 
  }
  
  @POST
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public HttpResponse doConfigureInstance(StaplerRequest req, @QueryParameter String rootUrl) {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    Map<String, String> errors = new HashMap<String, String>();
    checkRootUrl(errors, rootUrl);
    if (!errors.isEmpty())
      return HttpResponses.errorJSON(Messages.SetupWizard_ConfigureInstance_ValidationErrors(), errors); 
    useRootUrl(errors, rootUrl);
    if (!errors.isEmpty())
      return HttpResponses.errorJSON(Messages.SetupWizard_ConfigureInstance_ValidationErrors(), errors); 
    InstallUtil.proceedToNextStateFrom(InstallState.CONFIGURE_INSTANCE);
    CrumbIssuer crumbIssuer = Jenkins.get().getCrumbIssuer();
    JSONObject data = new JSONObject();
    if (crumbIssuer != null)
      data.accumulate("crumbRequestField", crumbIssuer.getCrumbRequestField()).accumulate("crumb", crumbIssuer.getCrumb(req)); 
    return HttpResponses.okJSON(data);
  }
  
  private void checkRootUrl(Map<String, String> errors, @CheckForNull String rootUrl) {
    if (rootUrl == null) {
      errors.put("rootUrl", Messages.SetupWizard_ConfigureInstance_RootUrl_Empty());
      return;
    } 
    if (!UrlHelper.isValidRootUrl(rootUrl))
      errors.put("rootUrl", Messages.SetupWizard_ConfigureInstance_RootUrl_Invalid()); 
  }
  
  private void useRootUrl(Map<String, String> errors, @CheckForNull String rootUrl) {
    LOGGER.log(Level.FINE, "Root URL set during SetupWizard to {0}", new Object[] { rootUrl });
    JenkinsLocationConfiguration.getOrDie().setUrl(rootUrl);
  }
  
  void setCurrentLevel(VersionNumber v) throws IOException { Files.writeString(Util.fileToPath(getUpdateStateFile()), v.toString(), StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]); }
  
  static File getUpdateStateFile() { return new File(Jenkins.get().getRootDir(), "jenkins.install.UpgradeWizard.state"); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @CheckForNull
  public VersionNumber getCurrentLevel() {
    VersionNumber from = new VersionNumber("1.0");
    File state = getUpdateStateFile();
    if (state.exists())
      try {
        from = new VersionNumber(StringUtils.defaultIfBlank(Files.readString(Util.fileToPath(state), StandardCharsets.UTF_8), "1.0").trim());
      } catch (IOException ex) {
        LOGGER.log(Level.SEVERE, "Cannot read the current version file", ex);
        return null;
      }  
    return from;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public HttpResponse doPlatformPluginList() throws IOException {
    SetupWizard setupWizard = Jenkins.get().getSetupWizard();
    if (setupWizard != null)
      if (InstallState.UPGRADE.equals(Jenkins.get().getInstallState())) {
        JSONArray initialPluginData = getPlatformPluginUpdates();
        if (initialPluginData != null)
          return HttpResponses.okJSON(initialPluginData); 
      } else {
        JSONArray initialPluginData = getPlatformPluginList();
        if (initialPluginData != null)
          return HttpResponses.okJSON(initialPluginData); 
      }  
    return HttpResponses.okJSON();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public HttpResponse doRestartStatus() throws IOException {
    JSONObject response = new JSONObject();
    Jenkins jenkins = Jenkins.get();
    response.put("restartRequired", Boolean.valueOf(jenkins.getUpdateCenter().isRestartRequiredForCompletion()));
    response.put("restartSupported", Boolean.valueOf(jenkins.getLifecycle().canRestart()));
    return HttpResponses.okJSON(response);
  }
  
  @CheckForNull
  public JSONArray getPlatformPluginUpdates() {
    VersionNumber version = getCurrentLevel();
    if (version == null)
      return null; 
    return getPlatformPluginsForUpdate(version, Jenkins.getVersion());
  }
  
  @CheckForNull
  JSONArray getPlatformPluginList() {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    JSONArray initialPluginList = null;
    for (UpdateSite updateSite : Jenkins.get().getUpdateCenter().getSiteList()) {
      String updateCenterJsonUrl = updateSite.getUrl();
      String suggestedPluginUrl = updateCenterJsonUrl.replace("/update-center.json", "/platform-plugins.json");
      VersionNumber version = Jenkins.getVersion();
      if (version != null && (suggestedPluginUrl.startsWith("https://") || suggestedPluginUrl.startsWith("http://")))
        suggestedPluginUrl = suggestedPluginUrl + suggestedPluginUrl + "version=" + (suggestedPluginUrl.contains("?") ? "&" : "?"); 
      try {
        URLConnection connection = ProxyConfiguration.open(new URL(suggestedPluginUrl));
        try {
          String initialPluginJson = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
          JSONObject initialPluginObject = null;
          if (connection instanceof HttpURLConnection) {
            int responseCode = ((HttpURLConnection)connection).getResponseCode();
            if (200 != responseCode)
              throw new HttpRetryException("Invalid response code (" + responseCode + ") from URL: " + suggestedPluginUrl, responseCode); 
            if (DownloadService.signatureCheck) {
              initialPluginObject = JSONObject.fromObject(initialPluginJson);
              FormValidation result = updateSite.verifySignatureInternal(initialPluginObject);
              if (result.kind != FormValidation.Kind.OK) {
                LOGGER.log(Level.WARNING, "Ignoring remote platform-plugins.json: " + result.getMessage());
                throw result;
              } 
            } 
          } 
          if (initialPluginObject != null) {
            initialPluginList = initialPluginObject.getJSONArray("categories");
            break;
          } 
          try {
            initialPluginList = JSONArray.fromObject(initialPluginJson);
            break;
          } catch (RuntimeException ex) {
            initialPluginList = JSONObject.fromObject(initialPluginJson).getJSONArray("categories");
            break;
          } 
        } catch (Exception e) {
          LOGGER.log(Level.FINE, e.getMessage(), e);
        } 
      } catch (Exception e) {
        LOGGER.log(Level.FINE, e.getMessage(), e);
      } 
    } 
    if (initialPluginList == null)
      try {
        ClassLoader cl = getClass().getClassLoader();
        URL localPluginData = cl.getResource("jenkins/install/platform-plugins.json");
        String initialPluginJson = IOUtils.toString(localPluginData.openStream(), StandardCharsets.UTF_8);
        initialPluginList = JSONArray.fromObject(initialPluginJson);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
      }  
    return initialPluginList;
  }
  
  JSONArray getPlatformPluginsForUpdate(VersionNumber from, VersionNumber to) {
    Jenkins jenkins = Jenkins.get();
    JSONArray pluginCategories = JSONArray.fromObject(getPlatformPluginList().toString());
    for (Iterator<?> categoryIterator = pluginCategories.iterator(); categoryIterator.hasNext(); ) {
      Object category = categoryIterator.next();
      if (category instanceof JSONObject) {
        JSONObject cat = (JSONObject)category;
        JSONArray plugins = cat.getJSONArray("plugins");
        for (Iterator<?> pluginIterator = plugins.iterator(); pluginIterator.hasNext(); ) {
          Object pluginData = pluginIterator.next();
          if (pluginData instanceof JSONObject) {
            JSONObject plugin = (JSONObject)pluginData;
            if (plugin.has("added")) {
              String sinceVersion = plugin.getString("added");
              if (sinceVersion != null) {
                VersionNumber v = new VersionNumber(sinceVersion);
                if (v.compareTo(to) <= 0 && v.compareTo(from) > 0) {
                  String pluginName = plugin.getString("name");
                  if (null == jenkins.getPluginManager().getPlugin(pluginName)) {
                    boolean foundCompatibleVersion = false;
                    for (UpdateSite site : jenkins.getUpdateCenter().getSiteList()) {
                      UpdateSite.Plugin sitePlug = site.getPlugin(pluginName);
                      if (sitePlug != null && !sitePlug.isForNewerHudson() && !sitePlug.isNeededDependenciesForNewerJenkins()) {
                        foundCompatibleVersion = true;
                        break;
                      } 
                    } 
                    if (foundCompatibleVersion)
                      continue; 
                  } 
                } 
              } 
            } 
          } 
          pluginIterator.remove();
        } 
        if (plugins.isEmpty())
          categoryIterator.remove(); 
      } 
    } 
    return pluginCategories;
  }
  
  public FilePath getInitialAdminPasswordFile() { return Jenkins.get().getRootPath().child("secrets/initialAdminPassword"); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public FilePath getInitialAdminApiTokenFile() { return Jenkins.get().getRootPath().child("secrets/initialAdminApiToken"); }
  
  @RequirePOST
  public HttpResponse doCompleteInstall() throws IOException {
    completeSetup();
    return HttpResponses.okJSON();
  }
  
  void completeSetup() {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    InstallUtil.saveLastExecVersion();
    setCurrentLevel(Jenkins.getVersion());
    InstallUtil.proceedToNextStateFrom(InstallState.INITIAL_SETUP_COMPLETED);
  }
  
  public List<InstallState> getInstallStates() { return InstallState.all(); }
  
  public InstallState getInstallState(String name) {
    if (name == null)
      return null; 
    return InstallState.valueOf(name);
  }
  
  public void onInstallStateUpdate(InstallState state) {
    if (state.isSetupComplete()) {
      tearDownFilter();
    } else {
      setUpFilter();
    } 
  }
  
  public boolean hasSetupWizardFilter() { return PluginServletFilter.hasFilter(this.FORCE_SETUP_WIZARD_FILTER); }
  
  private void checkFilter() {
    if (!Jenkins.get().getInstallState().isSetupComplete())
      setUpFilter(); 
  }
}
