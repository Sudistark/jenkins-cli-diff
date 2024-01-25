package jenkins.security.stapler;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.model.Saveable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.apache.commons.io.IOUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.WebApp;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class StaticRoutingDecisionProvider extends RoutingDecisionProvider implements Saveable {
  private static final Logger LOGGER = Logger.getLogger(StaticRoutingDecisionProvider.class.getName());
  
  private Set<String> whitelistSignaturesFromFixedList;
  
  private Set<String> whitelistSignaturesFromUserControlledList;
  
  private Set<String> blacklistSignaturesFromFixedList;
  
  private Set<String> blacklistSignaturesFromUserControlledList;
  
  public StaticRoutingDecisionProvider() { reload(); }
  
  public static StaticRoutingDecisionProvider get() { return (StaticRoutingDecisionProvider)ExtensionList.lookupSingleton(StaticRoutingDecisionProvider.class); }
  
  @NonNull
  public RoutingDecisionProvider.Decision decide(@NonNull String signature) {
    if (this.whitelistSignaturesFromFixedList == null || this.whitelistSignaturesFromUserControlledList == null || this.blacklistSignaturesFromFixedList == null || this.blacklistSignaturesFromUserControlledList == null)
      reload(); 
    LOGGER.log(Level.CONFIG, "Checking whitelist for " + signature);
    if (this.blacklistSignaturesFromFixedList.contains(signature) || this.blacklistSignaturesFromUserControlledList.contains(signature))
      return RoutingDecisionProvider.Decision.REJECTED; 
    if (this.whitelistSignaturesFromFixedList.contains(signature) || this.whitelistSignaturesFromUserControlledList.contains(signature))
      return RoutingDecisionProvider.Decision.ACCEPTED; 
    return RoutingDecisionProvider.Decision.UNKNOWN;
  }
  
  public void reload() {
    reloadFromDefault();
    reloadFromUserControlledList();
    resetMetaClassCache();
  }
  
  @VisibleForTesting
  void resetAndSave() {
    this.whitelistSignaturesFromFixedList = new HashSet();
    this.whitelistSignaturesFromUserControlledList = new HashSet();
    this.blacklistSignaturesFromFixedList = new HashSet();
    this.blacklistSignaturesFromUserControlledList = new HashSet();
    save();
  }
  
  private void resetMetaClassCache() { WebApp.get((Jenkins.get()).servletContext).clearMetaClassCache(); }
  
  private void reloadFromDefault() {
    try {
      InputStream is = StaticRoutingDecisionProvider.class.getResourceAsStream("default-whitelist.txt");
      try {
        this.whitelistSignaturesFromFixedList = new HashSet();
        this.blacklistSignaturesFromFixedList = new HashSet();
        parseFileIntoList(
            IOUtils.readLines(is, StandardCharsets.UTF_8), this.whitelistSignaturesFromFixedList, this.blacklistSignaturesFromFixedList);
        if (is != null)
          is.close(); 
      } catch (Throwable throwable) {
        if (is != null)
          try {
            is.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    } 
    LOGGER.log(Level.FINE, "Found {0} getter in the standard whitelist", Integer.valueOf(this.whitelistSignaturesFromFixedList.size()));
  }
  
  public StaticRoutingDecisionProvider add(@NonNull String signature) {
    if (this.whitelistSignaturesFromUserControlledList.add(signature)) {
      LOGGER.log(Level.INFO, "Signature [{0}] added to the whitelist", signature);
      save();
      resetMetaClassCache();
    } else {
      LOGGER.log(Level.INFO, "Signature [{0}] was already present in the whitelist", signature);
    } 
    return this;
  }
  
  public StaticRoutingDecisionProvider addBlacklistSignature(@NonNull String signature) {
    if (this.blacklistSignaturesFromUserControlledList.add(signature)) {
      LOGGER.log(Level.INFO, "Signature [{0}] added to the blacklist", signature);
      save();
      resetMetaClassCache();
    } else {
      LOGGER.log(Level.INFO, "Signature [{0}] was already present in the blacklist", signature);
    } 
    return this;
  }
  
  public StaticRoutingDecisionProvider remove(@NonNull String signature) {
    if (this.whitelistSignaturesFromUserControlledList.remove(signature)) {
      LOGGER.log(Level.INFO, "Signature [{0}] removed from the whitelist", signature);
      save();
      resetMetaClassCache();
    } else {
      LOGGER.log(Level.INFO, "Signature [{0}] was not present in the whitelist", signature);
    } 
    return this;
  }
  
  public StaticRoutingDecisionProvider removeBlacklistSignature(@NonNull String signature) {
    if (this.blacklistSignaturesFromUserControlledList.remove(signature)) {
      LOGGER.log(Level.INFO, "Signature [{0}] removed from the blacklist", signature);
      save();
      resetMetaClassCache();
    } else {
      LOGGER.log(Level.INFO, "Signature [{0}] was not present in the blacklist", signature);
    } 
    return this;
  }
  
  public void save() {
    if (BulkChange.contains(this))
      return; 
    File file = getConfigFile();
    try {
      List<String> allSignatures = new ArrayList<String>(this.whitelistSignaturesFromUserControlledList);
      Objects.requireNonNull(allSignatures);
      this.blacklistSignaturesFromUserControlledList.stream().map(signature -> "!" + signature).forEach(allSignatures::add);
      Files.write(Util.fileToPath(file), allSignatures, StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to save " + file.getAbsolutePath(), e);
    } 
  }
  
  private void reloadFromUserControlledList() {
    File file = getConfigFile();
    if (!file.exists()) {
      if ((this.whitelistSignaturesFromUserControlledList != null && this.whitelistSignaturesFromUserControlledList.isEmpty()) || (this.blacklistSignaturesFromUserControlledList != null && this.blacklistSignaturesFromUserControlledList
        .isEmpty()))
        LOGGER.log(Level.INFO, "No whitelist source file found at " + file + " so resetting user-controlled whitelist"); 
      this.whitelistSignaturesFromUserControlledList = new HashSet();
      this.blacklistSignaturesFromUserControlledList = new HashSet();
      return;
    } 
    LOGGER.log(Level.INFO, "Whitelist source file found at " + file);
    try {
      this.whitelistSignaturesFromUserControlledList = new HashSet();
      this.blacklistSignaturesFromUserControlledList = new HashSet();
      parseFileIntoList(
          Files.readAllLines(Util.fileToPath(file), StandardCharsets.UTF_8), this.whitelistSignaturesFromUserControlledList, this.blacklistSignaturesFromUserControlledList);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to load " + file.getAbsolutePath(), e);
    } 
  }
  
  private File getConfigFile() { return new File((WHITELIST_PATH == null) ? (new File(Jenkins.get().getRootDir(), "stapler-whitelist.txt")).toString() : WHITELIST_PATH); }
  
  private void parseFileIntoList(List<String> lines, Set<String> whitelist, Set<String> blacklist) {
    lines.stream()
      .filter(line -> !line.matches("#.*|\\s*"))
      .forEach(line -> {
          if (line.startsWith("!")) {
            String withoutExclamation = line.substring(1);
            if (!withoutExclamation.isEmpty())
              blacklist.add(withoutExclamation); 
          } else {
            whitelist.add(line);
          } 
        });
  }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static String WHITELIST_PATH = SystemProperties.getString(StaticRoutingDecisionProvider.class.getName() + ".whitelist");
}
