package jenkins.util.io;

import hudson.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public class FileBoolean {
  private final File file;
  
  public FileBoolean(File file) { this.file = file; }
  
  public FileBoolean(Class owner, String name) { this(new File(Jenkins.get().getRootDir(), owner.getName().replace('$', '.') + "/" + owner.getName().replace('$', '.'))); }
  
  public boolean get() { return (this.state = Boolean.valueOf(this.file.exists())).booleanValue(); }
  
  public boolean fastGet() {
    if (this.state == null)
      return get(); 
    return this.state.booleanValue();
  }
  
  public boolean isOn() { return get(); }
  
  public boolean isOff() { return !get(); }
  
  public void set(boolean b) {
    if (b) {
      on();
    } else {
      off();
    } 
  }
  
  public void on() {
    try {
      Util.createDirectories(this.file.getParentFile().toPath(), new java.nio.file.attribute.FileAttribute[0]);
      Files.newOutputStream(this.file.toPath(), new java.nio.file.OpenOption[0]).close();
      get();
    } catch (IOException|java.nio.file.InvalidPathException e) {
      LOGGER.log(Level.WARNING, "Failed to touch " + this.file);
    } 
  }
  
  public void off() {
    try {
      Files.deleteIfExists(this.file.toPath());
      get();
    } catch (IOException|java.nio.file.InvalidPathException e) {
      LOGGER.log(Level.WARNING, "Failed to delete " + this.file);
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(FileBoolean.class.getName());
}
