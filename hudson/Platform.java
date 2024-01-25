package hudson;

import hudson.util.VersionNumber;
import java.io.File;
import java.util.Locale;

public static enum Platform {
  WINDOWS(';'),
  UNIX(':');
  
  public final char pathSeparator;
  
  Platform(char pathSeparator) { this.pathSeparator = pathSeparator; }
  
  public static Platform current() {
    if (File.pathSeparatorChar == ':')
      return UNIX; 
    return WINDOWS;
  }
  
  public static boolean isDarwin() { return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("mac"); }
  
  public static boolean isSnowLeopardOrLater() {
    try {
      return (isDarwin() && (new VersionNumber(System.getProperty("os.version"))).compareTo(new VersionNumber("10.6")) >= 0);
    } catch (IllegalArgumentException e) {
      return false;
    } 
  }
}
