package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.PluginWrapper;
import hudson.util.FormValidation;
import hudson.util.HttpResponses;
import hudson.util.TextFile;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.JSONSignatureValidator;
import jenkins.util.SystemProperties;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.interceptor.RequirePOST;

@ExportedBean
public class UpdateSite {
  private Data data;
  
  private final String id;
  
  private final String url;
  
  private static final String signatureValidatorPrefix = "update site";
  
  private static final Set<String> warnedMissing = Collections.synchronizedSet(new HashSet());
  
  public UpdateSite(String id, String url) {
    this.id = id;
    this.url = url;
  }
  
  @Exported
  public String getId() { return this.id; }
  
  @Exported
  public long getDataTimestamp() {
    assert this.dataTimestamp >= 0L;
    return this.dataTimestamp;
  }
  
  @CheckForNull
  public Future<FormValidation> updateDirectly() { return updateDirectly(DownloadService.signatureCheck); }
  
  @Deprecated
  @CheckForNull
  public Future<FormValidation> updateDirectly(boolean signatureCheck) {
    if (!getDataFile().exists() || isDue())
      return (Jenkins.get().getUpdateCenter()).updateService.submit(new Object(this, signatureCheck)); 
    return null;
  }
  
  @NonNull
  public FormValidation updateDirectlyNow() throws IOException { return updateDirectlyNow(DownloadService.signatureCheck); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public FormValidation updateDirectlyNow(boolean signatureCheck) throws IOException { return updateData(DownloadService.loadJSON(new URL(getUrl() + "?id=" + getUrl() + "&version=" + URLEncoder.encode(getId(), StandardCharsets.UTF_8))), signatureCheck); }
  
  private FormValidation updateData(String json, boolean signatureCheck) throws IOException {
    this.dataTimestamp = System.currentTimeMillis();
    JSONObject o = JSONObject.fromObject(json);
    try {
      int v = o.getInt("updateCenterVersion");
      if (v != 1)
        throw new IllegalArgumentException("Unrecognized update center version: " + v); 
    } catch (JSONException x) {
      throw new IllegalArgumentException("Could not find (numeric) updateCenterVersion in " + json, x);
    } 
    if (signatureCheck) {
      FormValidation e = verifySignatureInternal(o);
      if (e.kind != FormValidation.Kind.OK) {
        LOGGER.severe(e.toString());
        return e;
      } 
    } 
    LOGGER.fine(() -> "Obtained the latest update center data file for UpdateSource " + this.id);
    this.retryWindow = 0L;
    getDataFile().write(json);
    this.data = new Data(this, o);
    return FormValidation.ok();
  }
  
  public FormValidation doVerifySignature() throws IOException { return verifySignatureInternal(getJSONObject()); }
  
  protected UpdateCenter.InstallationJob createInstallationJob(Plugin plugin, UpdateCenter uc, boolean dynamicLoad) { Objects.requireNonNull(uc);
    return new UpdateCenter.InstallationJob(uc, plugin, this, Jenkins.getAuthentication2(), dynamicLoad); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public final FormValidation verifySignatureInternal(JSONObject o) throws IOException { return getJsonSignatureValidator().verifySignature(o); }
  
  @Deprecated
  @NonNull
  protected JSONSignatureValidator getJsonSignatureValidator() { return getJsonSignatureValidator(null); }
  
  @NonNull
  protected JSONSignatureValidator getJsonSignatureValidator(@CheckForNull String name) {
    if (name == null)
      name = "update site '" + this.id + "'"; 
    return new JSONSignatureValidator(name);
  }
  
  public boolean isDue() {
    if (neverUpdate)
      return false; 
    if (this.dataTimestamp == 0L)
      this.dataTimestamp = (getDataFile()).file.lastModified(); 
    long now = System.currentTimeMillis();
    this.retryWindow = Math.max(this.retryWindow, TimeUnit.SECONDS.toMillis(15L));
    boolean due = (now - this.dataTimestamp > DAY && now - this.lastAttempt > this.retryWindow);
    if (due) {
      this.lastAttempt = now;
      this.retryWindow = Math.min(this.retryWindow * 2L, TimeUnit.HOURS.toMillis(1L));
    } 
    return due;
  }
  
  @RequirePOST
  public HttpResponse doInvalidateData() {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    this.dataTimestamp = 0L;
    this.data = null;
    return HttpResponses.ok();
  }
  
  @CheckForNull
  public Data getData() {
    if (this.data == null) {
      JSONObject o = getJSONObject();
      if (o != null)
        this.data = new Data(this, o); 
    } 
    return this.data;
  }
  
  boolean hasUnparsedData() { return (this.data == null && getDataFile().exists()); }
  
  public JSONObject getJSONObject() {
    TextFile df = getDataFile();
    if (df.exists()) {
      long start = System.nanoTime();
      try {
        JSONObject o = JSONObject.fromObject(df.read());
        LOGGER.fine(() -> String.format("Loaded and parsed %s in %.01fs", new Object[] { df, Double.valueOf((System.nanoTime() - start) / 1.0E9D) }));
        return o;
      } catch (JSONException|IOException e) {
        LOGGER.log(Level.SEVERE, "Failed to parse " + df, e);
        try {
          df.delete();
        } catch (IOException e2) {
          LOGGER.log(Level.SEVERE, "Failed to delete " + df, e2);
        } 
        return null;
      } 
    } 
    return null;
  }
  
  @Exported
  public List<Plugin> getAvailables() {
    List<Plugin> r = new ArrayList<Plugin>();
    Data data = getData();
    if (data == null)
      return Collections.emptyList(); 
    for (Plugin p : data.plugins.values()) {
      if (p.getInstalled() == null)
        r.add(p); 
    } 
    r.sort((plugin, t1) -> {
          int pop = t1.popularity.compareTo(plugin.popularity);
          if (pop != 0)
            return pop; 
          return plugin.getDisplayName().compareTo(t1.getDisplayName());
        });
    return r;
  }
  
  @CheckForNull
  public Plugin getPlugin(String artifactId) {
    Data dt = getData();
    if (dt == null)
      return null; 
    return (Plugin)dt.plugins.get(artifactId);
  }
  
  public Api getApi() { return new Api(this); }
  
  @Exported
  @CheckForNull
  public String getConnectionCheckUrl() {
    Data dt = getData();
    if (dt == null)
      return "http://www.google.com/"; 
    return dt.connectionCheckUrl;
  }
  
  private TextFile getDataFile() {
    return new TextFile(new File(Jenkins.get().getRootDir(), "updates/" + 
          getId() + ".json"));
  }
  
  @Exported
  public List<Plugin> getUpdates() {
    Data data = getData();
    if (data == null)
      return Collections.emptyList(); 
    List<Plugin> r = new ArrayList<Plugin>();
    for (PluginWrapper pw : Jenkins.get().getPluginManager().getPlugins()) {
      Plugin p = pw.getUpdateInfo();
      if (p != null)
        r.add(p); 
    } 
    return r;
  }
  
  @Exported
  public boolean hasUpdates() {
    Data data = getData();
    if (data == null)
      return false; 
    for (PluginWrapper pw : Jenkins.get().getPluginManager().getPlugins()) {
      if (!pw.isBundled() && pw.getUpdateInfo() != null)
        return true; 
    } 
    return false;
  }
  
  @Exported
  public String getUrl() { return this.url; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @CheckForNull
  public String getMetadataUrlForDownloadable(String downloadable) {
    String siteUrl = getUrl();
    String updateSiteMetadataUrl = null;
    int baseUrlEnd = siteUrl.indexOf("update-center.json");
    if (baseUrlEnd != -1) {
      String siteBaseUrl = siteUrl.substring(0, baseUrlEnd);
      updateSiteMetadataUrl = siteBaseUrl + "updates/" + siteBaseUrl;
    } else {
      LOGGER.log(Level.WARNING, "Url {0} does not look like an update center:", siteUrl);
    } 
    return updateSiteMetadataUrl;
  }
  
  @Deprecated
  public String getDownloadUrl() { return this.url; }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isLegacyDefault() { return false; }
  
  private static String get(JSONObject o, String prop) {
    if (o.has(prop))
      return o.getString(prop); 
    return null;
  }
  
  static final Predicate<Object> IS_DEP_PREDICATE = x -> (x instanceof JSONObject && get((JSONObject)x, "name") != null);
  
  static final Predicate<Object> IS_NOT_OPTIONAL = x -> "false".equals(get((JSONObject)x, "optional"));
  
  private static final long DAY = TimeUnit.DAYS.toMillis(1L);
  
  private static final Logger LOGGER = Logger.getLogger(UpdateSite.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean neverUpdate = SystemProperties.getBoolean(UpdateCenter.class.getName() + ".never");
}
