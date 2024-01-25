package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Util;
import hudson.XmlFile;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import jenkins.model.IdStrategy;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class UserIdMapper {
  private static final XStream2 XSTREAM = new XStream2();
  
  static final String MAPPING_FILE = "users.xml";
  
  private static final Logger LOGGER = Logger.getLogger(UserIdMapper.class.getName());
  
  private static final int PREFIX_MAX = 15;
  
  private static final Pattern PREFIX_PATTERN = Pattern.compile("[^A-Za-z0-9]");
  
  private final int version = 1;
  
  private File usersDirectory;
  
  private Map<String, String> idToDirectoryNameMap = new ConcurrentHashMap();
  
  static UserIdMapper getInstance() { return (UserIdMapper)ExtensionList.lookupSingleton(UserIdMapper.class); }
  
  @Initializer(after = InitMilestone.PLUGINS_STARTED, before = InitMilestone.JOB_LOADED)
  public File init() throws IOException {
    this.usersDirectory = createUsersDirectoryAsNeeded();
    load();
    return this.usersDirectory;
  }
  
  @CheckForNull
  File getDirectory(String userId) {
    String directoryName = (String)this.idToDirectoryNameMap.get(getIdStrategy().keyFor(userId));
    return (directoryName == null) ? null : new File(this.usersDirectory, directoryName);
  }
  
  File putIfAbsent(String userId, boolean saveToDisk) throws IOException {
    String idKey = getIdStrategy().keyFor(userId);
    String directoryName = (String)this.idToDirectoryNameMap.get(idKey);
    File directory = null;
    if (directoryName == null)
      synchronized (this) {
        directoryName = (String)this.idToDirectoryNameMap.get(idKey);
        if (directoryName == null) {
          directory = createDirectoryForNewUser(userId);
          directoryName = directory.getName();
          this.idToDirectoryNameMap.put(idKey, directoryName);
          if (saveToDisk)
            save(); 
        } 
      }  
    return (directory == null) ? new File(this.usersDirectory, directoryName) : directory;
  }
  
  boolean isMapped(String userId) { return this.idToDirectoryNameMap.containsKey(getIdStrategy().keyFor(userId)); }
  
  Set<String> getConvertedUserIds() { return Collections.unmodifiableSet(this.idToDirectoryNameMap.keySet()); }
  
  void remove(String userId) throws IOException {
    this.idToDirectoryNameMap.remove(getIdStrategy().keyFor(userId));
    save();
  }
  
  void clear() { this.idToDirectoryNameMap.clear(); }
  
  void reload() {
    clear();
    load();
  }
  
  protected IdStrategy getIdStrategy() { return User.idStrategy(); }
  
  protected File getUsersDirectory() throws IOException { return User.getRootDir(); }
  
  private XmlFile getXmlConfigFile() {
    File file = getConfigFile(this.usersDirectory);
    return new XmlFile(XSTREAM, file);
  }
  
  static File getConfigFile(File usersDirectory) { return new File(usersDirectory, "users.xml"); }
  
  private File createDirectoryForNewUser(String userId) {
    try {
      Path tempDirectory = Files.createTempDirectory(Util.fileToPath(this.usersDirectory), generatePrefix(userId), new java.nio.file.attribute.FileAttribute[0]);
      return tempDirectory.toFile();
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error creating directory for user: " + userId, e);
      throw e;
    } 
  }
  
  private String generatePrefix(String userId) {
    String fullPrefix = PREFIX_PATTERN.matcher(userId).replaceAll("");
    return (fullPrefix.length() > 14) ? (fullPrefix.substring(0, 14) + "_") : (fullPrefix + "_");
  }
  
  private File createUsersDirectoryAsNeeded() throws IOException {
    File usersDirectory = getUsersDirectory();
    if (!usersDirectory.exists())
      try {
        Files.createDirectory(usersDirectory.toPath(), new java.nio.file.attribute.FileAttribute[0]);
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Unable to create users directory: " + usersDirectory, e);
        throw e;
      }  
    return usersDirectory;
  }
  
  void save() {
    try {
      getXmlConfigFile().write(this);
    } catch (IOException ioe) {
      LOGGER.log(Level.WARNING, "Error saving userId mapping file.", ioe);
      throw ioe;
    } 
  }
  
  private void load() {
    UserIdMigrator migrator = new UserIdMigrator(this.usersDirectory, getIdStrategy());
    if (migrator.needsMigration()) {
      try {
        migrator.migrateUsers(this);
      } catch (IOException ioe) {
        LOGGER.log(Level.SEVERE, "Error migrating users.", ioe);
        throw ioe;
      } 
    } else {
      XmlFile config = getXmlConfigFile();
      try {
        config.unmarshal(this);
      } catch (NoSuchFileException e) {
        LOGGER.log(Level.FINE, "User id mapping file does not exist. It will be created when a user is saved.");
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to load " + config, e);
        throw e;
      } 
    } 
  }
}
