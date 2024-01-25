package jenkins.security.apitoken;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.User;
import hudson.model.listeners.SaveableListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class ApiTokenStats implements Saveable {
  private static final Logger LOGGER = Logger.getLogger(ApiTokenStats.class.getName());
  
  private List<SingleTokenStats> tokenStats;
  
  private User user;
  
  @VisibleForTesting
  File parent;
  
  @VisibleForTesting
  ApiTokenStats() { init(); }
  
  private Object readResolve() {
    init();
    return this;
  }
  
  private void init() {
    if (this.tokenStats == null) {
      this.tokenStats = new ArrayList();
    } else {
      keepLastUpdatedUnique();
    } 
  }
  
  private void keepLastUpdatedUnique() {
    Map<String, SingleTokenStats> temp = new HashMap<String, SingleTokenStats>();
    this.tokenStats.forEach(candidate -> {
          SingleTokenStats current = (SingleTokenStats)temp.get(candidate.tokenUuid);
          if (current == null) {
            temp.put(candidate.tokenUuid, candidate);
          } else {
            int comparison = SingleTokenStats.COMP_BY_LAST_USE_THEN_COUNTER.compare(current, candidate);
            if (comparison < 0)
              temp.put(candidate.tokenUuid, candidate); 
          } 
        });
    this.tokenStats = new ArrayList(temp.values());
  }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  void setParent(@NonNull File parent) { this.parent = parent; }
  
  private boolean areStatsDisabled() { return !ApiTokenPropertyConfiguration.get().isUsageStatisticsEnabled(); }
  
  public void removeId(@NonNull String tokenUuid) {
    if (areStatsDisabled())
      return; 
    boolean tokenRemoved = this.tokenStats.removeIf(s -> s.tokenUuid.equals(tokenUuid));
    if (tokenRemoved)
      save(); 
  }
  
  public void removeAll() {
    int size = this.tokenStats.size();
    this.tokenStats.clear();
    if (size > 0)
      save(); 
  }
  
  public void removeAllExcept(@NonNull String tokenUuid) {
    int sizeBefore = this.tokenStats.size();
    this.tokenStats.removeIf(s -> !s.tokenUuid.equals(tokenUuid));
    int sizeAfter = this.tokenStats.size();
    if (sizeBefore != sizeAfter)
      save(); 
  }
  
  @NonNull
  public SingleTokenStats updateUsageForId(@NonNull String tokenUuid) {
    if (areStatsDisabled())
      return new SingleTokenStats(tokenUuid); 
    return updateUsageForIdIfNeeded(tokenUuid);
  }
  
  @SuppressFBWarnings(value = {"IS2_INCONSISTENT_SYNC"}, justification = "access is in fact synchronized")
  private SingleTokenStats updateUsageForIdIfNeeded(@NonNull String tokenUuid) {
    SingleTokenStats stats = (SingleTokenStats)findById(tokenUuid).orElseGet(() -> {
          SingleTokenStats result = new SingleTokenStats(tokenUuid);
          this.tokenStats.add(result);
          return result;
        });
    stats.notifyUse();
    save();
    return stats;
  }
  
  @NonNull
  public SingleTokenStats findTokenStatsById(@NonNull String tokenUuid) {
    if (areStatsDisabled())
      return new SingleTokenStats(tokenUuid); 
    return (SingleTokenStats)findById(tokenUuid)
      .orElse(new SingleTokenStats(tokenUuid));
  }
  
  @NonNull
  private Optional<SingleTokenStats> findById(@NonNull String tokenUuid) { return this.tokenStats.stream()
      .filter(s -> s.tokenUuid.equals(tokenUuid))
      .findFirst(); }
  
  public void save() {
    if (areStatsDisabled())
      return; 
    if (BulkChange.contains(this))
      return; 
    File userFolder = getUserFolder();
    if (userFolder == null)
      return; 
    XmlFile configFile = getConfigFile(userFolder);
    try {
      configFile.write(this);
      SaveableListener.fireOnChange(this, configFile);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to save " + configFile, e);
    } 
  }
  
  @CheckForNull
  private File getUserFolder() {
    File userFolder = this.parent;
    if (userFolder == null && this.user != null) {
      userFolder = this.user.getUserFolder();
      if (userFolder == null) {
        LOGGER.log(Level.INFO, "No user folder yet for user {0}", this.user.getId());
        return null;
      } 
      this.parent = userFolder;
    } 
    return userFolder;
  }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public static ApiTokenStats load(@CheckForNull File parent) {
    if (parent == null)
      return new ApiTokenStats(); 
    ApiTokenStats apiTokenStats = internalLoad(parent);
    if (apiTokenStats == null)
      apiTokenStats = new ApiTokenStats(); 
    apiTokenStats.setParent(parent);
    return apiTokenStats;
  }
  
  @NonNull
  public static ApiTokenStats load(@NonNull User user) {
    ApiTokenStats apiTokenStats = null;
    File userFolder = user.getUserFolder();
    if (userFolder != null)
      apiTokenStats = internalLoad(userFolder); 
    if (apiTokenStats == null)
      apiTokenStats = new ApiTokenStats(); 
    apiTokenStats.user = user;
    return apiTokenStats;
  }
  
  @VisibleForTesting
  @CheckForNull
  static ApiTokenStats internalLoad(@NonNull File userFolder) {
    ApiTokenStats apiTokenStats = null;
    XmlFile statsFile = getConfigFile(userFolder);
    if (statsFile.exists())
      try {
        apiTokenStats = (ApiTokenStats)statsFile.unmarshal(ApiTokenStats.class);
        apiTokenStats.parent = userFolder;
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to load " + statsFile, e);
      }  
    return apiTokenStats;
  }
  
  @NonNull
  protected static XmlFile getConfigFile(@NonNull File parent) { return new XmlFile(new File(parent, "apiTokenStats.xml")); }
}
