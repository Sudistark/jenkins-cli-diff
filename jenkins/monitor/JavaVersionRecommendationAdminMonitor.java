package jenkins.monitor;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import hudson.security.Permission;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.TreeMap;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Symbol({"javaVersionRecommendation"})
public class JavaVersionRecommendationAdminMonitor extends AdministrativeMonitor {
  private static final NavigableMap<Integer, LocalDate> SUPPORTED_JAVA_VERSIONS;
  
  private static Boolean disabled;
  
  static  {
    supportedVersions = new TreeMap();
    supportedVersions.put(Integer.valueOf(11), LocalDate.of(2024, 9, 30));
    supportedVersions.put(Integer.valueOf(17), LocalDate.of(2026, 3, 31));
    supportedVersions.put(Integer.valueOf(21), LocalDate.of(2027, 9, 30));
    SUPPORTED_JAVA_VERSIONS = Collections.unmodifiableNavigableMap(supportedVersions);
    disabled = Boolean.valueOf(SystemProperties.getBoolean(JavaVersionRecommendationAdminMonitor.class.getName() + ".disabled", false));
  }
  
  public JavaVersionRecommendationAdminMonitor() { super(getId()); }
  
  private static String getId() {
    id = new StringBuilder();
    id.append(JavaVersionRecommendationAdminMonitor.class.getName());
    LocalDate endOfLife = getEndOfLife();
    if (endOfLife.isBefore(LocalDate.MAX)) {
      id.append('-');
      id.append(Runtime.version().feature());
      id.append('-');
      id.append(endOfLife);
      id.append('-');
      id.append(getSeverity());
    } 
    return id.toString();
  }
  
  public boolean isActivated() { return (!disabled.booleanValue() && getDeprecationPeriod().toTotalMonths() < 12L); }
  
  public String getDisplayName() { return Messages.JavaLevelAdminMonitor_DisplayName(); }
  
  public Permission getRequiredPermission() { return Jenkins.SYSTEM_READ; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @RequirePOST
  public HttpResponse doAct(@QueryParameter String no) throws IOException {
    if (no != null) {
      Jenkins.get().checkPermission(Jenkins.ADMINISTER);
      disable(true);
      return HttpResponses.forwardToPreviousPage();
    } 
    return new HttpRedirect("https://jenkins.io/redirect/java-support/");
  }
  
  @NonNull
  private static LocalDate getEndOfLife() {
    endOfLife = (LocalDate)SUPPORTED_JAVA_VERSIONS.get(Integer.valueOf(Runtime.version().feature()));
    return (endOfLife != null) ? endOfLife : LocalDate.MAX;
  }
  
  @NonNull
  private static Period getDeprecationPeriod() { return Period.between(LocalDate.now(), getEndOfLife()); }
  
  @NonNull
  private static Severity getSeverity() { return (getDeprecationPeriod().toTotalMonths() < 3L) ? Severity.DANGER : Severity.WARNING; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public int getJavaVersion() { return Runtime.version().feature(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public Date getEndOfLifeAsDate() { return Date.from(getEndOfLife().atStartOfDay(ZoneId.systemDefault()).toInstant()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public String getSeverityAsString() { return getSeverity().toString().toLowerCase(Locale.US); }
}
