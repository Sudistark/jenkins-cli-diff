package hudson.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jenkins.model.Jenkins;
import jenkins.util.NonLocalizable;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 2)
public class HealthReport extends Object implements Serializable, Comparable<HealthReport> {
  private static final String HEALTH_OVER_80 = "icon-health-80plus";
  
  private static final String HEALTH_61_TO_80 = "icon-health-60to79";
  
  private static final String HEALTH_41_TO_60 = "icon-health-40to59";
  
  private static final String HEALTH_21_TO_40 = "icon-health-20to39";
  
  private static final String HEALTH_0_TO_20 = "icon-health-00to19";
  
  private static final String HEALTH_OVER_80_IMG = "health-80plus.png";
  
  private static final String HEALTH_61_TO_80_IMG = "health-60to79.png";
  
  private static final String HEALTH_41_TO_60_IMG = "health-40to59.png";
  
  private static final String HEALTH_21_TO_40_IMG = "health-20to39.png";
  
  private static final String HEALTH_0_TO_20_IMG = "health-00to19.png";
  
  private static final String HEALTH_UNKNOWN_IMG = "empty.png";
  
  private static final Map<String, String> iconIMGToClassMap = new HashMap();
  
  private static final long serialVersionUID = 7451361788415642230L;
  
  private int score;
  
  private String iconClassName;
  
  private String iconUrl;
  
  @Deprecated
  private String description;
  
  private Localizable localizibleDescription;
  
  static  {
    iconIMGToClassMap.put("health-80plus.png", "icon-health-80plus");
    iconIMGToClassMap.put("health-60to79.png", "icon-health-60to79");
    iconIMGToClassMap.put("health-40to59.png", "icon-health-40to59");
    iconIMGToClassMap.put("health-20to39.png", "icon-health-20to39");
    iconIMGToClassMap.put("health-00to19.png", "icon-health-00to19");
  }
  
  @Deprecated
  public HealthReport(int score, String iconUrl, String description) { this(score, iconUrl, new NonLocalizable(description)); }
  
  public HealthReport(int score, String iconUrl, Localizable description) {
    this.score = score;
    if (score <= 20) {
      this.iconClassName = "icon-health-00to19";
    } else if (score <= 40) {
      this.iconClassName = "icon-health-20to39";
    } else if (score <= 60) {
      this.iconClassName = "icon-health-40to59";
    } else if (score <= 80) {
      this.iconClassName = "icon-health-60to79";
    } else {
      this.iconClassName = "icon-health-80plus";
    } 
    if (iconUrl == null) {
      if (score <= 20) {
        this.iconUrl = "health-00to19.png";
      } else if (score <= 40) {
        this.iconUrl = "health-20to39.png";
      } else if (score <= 60) {
        this.iconUrl = "health-40to59.png";
      } else if (score <= 80) {
        this.iconUrl = "health-60to79.png";
      } else {
        this.iconUrl = "health-80plus.png";
      } 
    } else {
      this.iconUrl = iconUrl;
    } 
    this.description = null;
    setLocalizibleDescription(description);
  }
  
  @Deprecated
  public HealthReport(int score, String description) { this(score, null, description); }
  
  public HealthReport(int score, Localizable description) { this(score, null, description); }
  
  public HealthReport() { this(100, "empty.png", Messages._HealthReport_EmptyString()); }
  
  @Exported
  public int getScore() { return this.score; }
  
  public void setScore(int score) { this.score = score; }
  
  @Exported
  public String getIconUrl() { return this.iconUrl; }
  
  @Exported
  public String getIconClassName() { return this.iconClassName; }
  
  public String getIconUrl(String size) {
    if (this.iconUrl == null)
      return Jenkins.RESOURCE_PATH + "/images/" + Jenkins.RESOURCE_PATH + "/empty.png"; 
    if (this.iconUrl.startsWith("/"))
      return this.iconUrl.replace("/32x32/", "/" + size + "/"); 
    return Jenkins.RESOURCE_PATH + "/images/" + Jenkins.RESOURCE_PATH + "/" + size;
  }
  
  public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
  
  @Exported
  public String getDescription() { return getLocalizableDescription().toString(); }
  
  public void setDescription(String description) { setLocalizibleDescription(new NonLocalizable(description)); }
  
  public Localizable getLocalizableDescription() { return this.localizibleDescription; }
  
  public void setLocalizibleDescription(Localizable localizibleDescription) { this.localizibleDescription = localizibleDescription; }
  
  public List<HealthReport> getAggregatedReports() { return Collections.emptyList(); }
  
  public boolean isAggregateReport() { return false; }
  
  public int compareTo(HealthReport o) { return Integer.compare(this.score, o.score); }
  
  public static HealthReport min(HealthReport a, HealthReport b) {
    if (a == null && b == null)
      return null; 
    if (a == null)
      return b; 
    if (b == null)
      return a; 
    if (a.compareTo(b) <= 0)
      return a; 
    return b;
  }
  
  public static HealthReport max(HealthReport a, HealthReport b) {
    if (a == null && b == null)
      return null; 
    if (a == null)
      return b; 
    if (b == null)
      return a; 
    if (a.compareTo(b) >= 0)
      return a; 
    return b;
  }
}
