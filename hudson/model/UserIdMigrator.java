package hudson.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.IdStrategy;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
class UserIdMigrator {
  private static final Logger LOGGER = Logger.getLogger(UserIdMigrator.class.getName());
  
  private static final String EMPTY_USERNAME_DIRECTORY_NAME = "emptyUsername";
  
  private final File usersDirectory;
  
  private final IdStrategy idStrategy;
  
  UserIdMigrator(File usersDirectory, IdStrategy idStrategy) {
    this.usersDirectory = usersDirectory;
    this.idStrategy = idStrategy;
  }
  
  boolean needsMigration() {
    File mappingFile = UserIdMapper.getConfigFile(this.usersDirectory);
    if (mappingFile.exists() && mappingFile.isFile()) {
      LOGGER.finest("User mapping file already exists. No migration needed.");
      return false;
    } 
    File[] userDirectories = listUserDirectories();
    return (userDirectories != null && userDirectories.length > 0);
  }
  
  private File[] listUserDirectories() { return this.usersDirectory.listFiles(file -> (file.isDirectory() && (new File(file, "config.xml")).exists())); }
  
  Map<String, File> scanExistingUsers() throws IOException {
    Map<String, File> users = new HashMap<String, File>();
    File[] userDirectories = listUserDirectories();
    if (userDirectories != null)
      for (File directory : userDirectories) {
        String userId = this.idStrategy.idFromFilename(directory.getName());
        users.put(userId, directory);
      }  
    addEmptyUsernameIfExists(users);
    return users;
  }
  
  private void addEmptyUsernameIfExists(Map<String, File> users) throws IOException {
    File emptyUsernameConfigFile = new File(this.usersDirectory, "config.xml");
    if (emptyUsernameConfigFile.exists()) {
      File newEmptyUsernameDirectory = new File(this.usersDirectory, "emptyUsername");
      Files.createDirectory(newEmptyUsernameDirectory.toPath(), new java.nio.file.attribute.FileAttribute[0]);
      File newEmptyUsernameConfigFile = new File(newEmptyUsernameDirectory, "config.xml");
      Files.move(emptyUsernameConfigFile.toPath(), newEmptyUsernameConfigFile.toPath(), new CopyOption[0]);
      users.put("", newEmptyUsernameDirectory);
    } 
  }
  
  void migrateUsers(UserIdMapper mapper) throws IOException {
    LOGGER.fine("Beginning migration of users to userId mapping.");
    Map<String, File> existingUsers = scanExistingUsers();
    for (Map.Entry<String, File> existingUser : existingUsers.entrySet()) {
      File newDirectory = mapper.putIfAbsent((String)existingUser.getKey(), false);
      LOGGER.log(Level.INFO, "Migrating user '" + (String)existingUser.getKey() + "' from 'users/" + ((File)existingUser.getValue()).getName() + "/' to 'users/" + newDirectory.getName() + "/'");
      Files.move(((File)existingUser.getValue()).toPath(), newDirectory.toPath(), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
    } 
    mapper.save();
    LOGGER.fine("Completed migration of users to userId mapping.");
  }
}
