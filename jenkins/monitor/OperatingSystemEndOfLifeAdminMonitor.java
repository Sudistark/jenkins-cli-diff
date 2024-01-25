package jenkins.monitor;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.security.Permission;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Symbol({"operatingSystemEndOfLife"})
public class OperatingSystemEndOfLifeAdminMonitor extends AdministrativeMonitor {
  static final Logger LOGGER = Logger.getLogger(OperatingSystemEndOfLifeAdminMonitor.class.getName());
  
  boolean ignoreEndOfLife = false;
  
  private LocalDate warningsStartDate = LocalDate.now().plusYears(10L);
  
  private boolean afterEndOfLifeDate = false;
  
  private String operatingSystemName = System.getProperty("os.name", "Unknown");
  
  private String endOfLifeDate = "2099-12-31";
  
  private String documentationUrl = "https://www.jenkins.io/redirect/operating-system-end-of-life";
  
  private File lastDataFile = null;
  
  private List<String> lastLines = null;
  
  public OperatingSystemEndOfLifeAdminMonitor(String id) throws IOException {
    super(id);
    fillOperatingSystemList();
  }
  
  public OperatingSystemEndOfLifeAdminMonitor() throws IOException { fillOperatingSystemList(); }
  
  private void fillOperatingSystemList() throws IOException {
    if (Jenkins.getInstanceOrNull() != null && !isEnabled()) {
      LOGGER.log(Level.FINEST, "Operating system end of life monitor is not enabled, reading no data");
      return;
    } 
    ClassLoader cl = getClass().getClassLoader();
    URL localOperatingSystemData = cl.getResource("jenkins/monitor/OperatingSystemEndOfLifeAdminMonitor/end-of-life-data.json");
    String initialOperatingSystemJson = IOUtils.toString(localOperatingSystemData.openStream(), StandardCharsets.UTF_8);
    readOperatingSystemList(initialOperatingSystemJson);
  }
  
  void readOperatingSystemList(String initialOperatingSystemJson) throws IOException {
    JSONArray systems = JSONArray.fromObject(initialOperatingSystemJson);
    if (systems.isEmpty())
      throw new IOException("Empty data set"); 
    for (Object systemObj : systems) {
      if (!(systemObj instanceof JSONObject))
        throw new IOException("Wrong object type in data file"); 
      JSONObject system = (JSONObject)systemObj;
      if (!system.has("pattern"))
        throw new IOException("Missing pattern in definition file"); 
      String pattern = system.getString("pattern");
      if (!system.has("endOfLife"))
        throw new IOException("No end of life date for " + pattern); 
      LocalDate endOfLife = LocalDate.parse(system.getString("endOfLife"));
      LocalDate startDate = system.has("start") ? LocalDate.parse(system.getString("start")) : endOfLife.minusMonths(6L);
      File dataFile = getDataFile(system);
      LOGGER.log(Level.FINEST, "Pattern {0} starts {1} and reaches end of life {2} from file {3}", new Object[] { pattern, startDate, endOfLife, dataFile });
      String name = readOperatingSystemName(dataFile, pattern);
      if (name.isEmpty()) {
        LOGGER.log(Level.FINE, "Pattern {0} did not match from file {1}", new Object[] { pattern, dataFile });
        continue;
      } 
      if (startDate.isBefore(this.warningsStartDate)) {
        this.warningsStartDate = startDate;
        LOGGER.log(Level.FINE, "Warnings start date is now {0}", this.warningsStartDate);
      } 
      LOGGER.log(Level.FINE, "Matched operating system {0}", name);
      if (startDate.isBefore(LocalDate.now())) {
        this.operatingSystemName = name;
        this.documentationUrl = buildDocumentationUrl(this.operatingSystemName);
        this.endOfLifeDate = endOfLife.toString();
        if (endOfLife.isBefore(LocalDate.now())) {
          LOGGER.log(Level.FINE, "Operating system {0} is after end of life {1}", new Object[] { name, endOfLife });
          this.afterEndOfLifeDate = true;
          continue;
        } 
        LOGGER.log(Level.FINE, "Operating system {0} started warnings {1} and reaches end of life {2}", new Object[] { name, startDate, endOfLife });
      } 
    } 
    if (this.lastLines != null)
      this.lastLines.clear(); 
  }
  
  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"}, justification = "File path defined in war file, not by user")
  @CheckForNull
  private File getDataFile(@NonNull JSONObject system) {
    String fileName = "/etc/os-release";
    if (system.has("file"))
      fileName = system.getString("file"); 
    return new File(fileName);
  }
  
  @NonNull
  String readOperatingSystemName(File dataFile, @NonNull String patternStr) {
    if (dataFile == null || !dataFile.exists())
      return ""; 
    Pattern pattern = Pattern.compile("^PRETTY_NAME=[\"](" + patternStr + ".*)[\"]");
    String name = "";
    try {
      List<String> lines = dataFile.equals(this.lastDataFile) ? this.lastLines : Files.readAllLines(dataFile.toPath());
      for (String line : lines) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches())
          name = matcher.group(1); 
      } 
      if (!dataFile.equals(this.lastDataFile)) {
        this.lastDataFile = dataFile;
        this.lastLines = new ArrayList(lines);
      } 
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "File read exception", ioe);
    } 
    return name;
  }
  
  @NonNull
  public String getOperatingSystemName() { return this.operatingSystemName; }
  
  @NonNull
  public String getEndOfLifeDate() { return this.endOfLifeDate; }
  
  public boolean getAfterEndOfLifeDate() { return this.afterEndOfLifeDate; }
  
  @NonNull
  public String getDocumentationUrl() { return this.documentationUrl; }
  
  @NonNull
  String readDocumentationUrl(File dataFile, @NonNull String patternStr) {
    if (dataFile == null || !dataFile.exists())
      return ""; 
    String operatingSystemName = readOperatingSystemName(dataFile, patternStr);
    return buildDocumentationUrl(operatingSystemName);
  }
  
  private String buildDocumentationUrl(String operatingSystemName) {
    String scheme = "https";
    String hostName = "www.jenkins.io";
    String path = "/redirect/operating-system-end-of-life";
    String query = "q=" + operatingSystemName.replace(" ", "-").replace("/", "-").replace("(", "").replace(")", "");
    String url = this.documentationUrl;
    try {
      URI documentationURI = new URI(scheme, hostName, path, query, null);
      url = documentationURI.toString();
    } catch (URISyntaxException e) {
      url = scheme + "://" + scheme + hostName;
    } 
    return url;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RequirePOST
  public HttpResponse doAct(@QueryParameter String no) throws IOException {
    if (no != null) {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      disable(true);
      LOGGER.log(Level.FINE, "Disabled operating system end of life monitor");
      return HttpResponses.forwardToPreviousPage();
    } 
    LOGGER.log(Level.FINE, "Enabled operating system end of life monitor");
    return new HttpRedirect(this.documentationUrl);
  }
  
  public boolean isActivated() {
    if (this.ignoreEndOfLife) {
      LOGGER.log(Level.FINE, "Not activated because ignoring end of life monitor");
      return false;
    } 
    if (LocalDate.now().isBefore(this.warningsStartDate)) {
      LOGGER.log(Level.FINE, "Not activated because it is before the start date {0}", this.warningsStartDate);
      return false;
    } 
    LOGGER.log(Level.FINEST, "Activated because it is after the warnings start date {0}", this.warningsStartDate);
    return true;
  }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  public String getDisplayName() { return "Operating system end of life monitor"; }
}
