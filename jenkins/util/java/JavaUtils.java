package jenkins.util.java;

import io.jenkins.lib.versionnumber.JavaSpecificationVersion;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class JavaUtils {
  public static boolean isRunningWithJava8OrBelow() {
    javaVersion = getCurrentRuntimeJavaVersion();
    return javaVersion.startsWith("1.");
  }
  
  public static boolean isRunningWithPostJava8() {
    javaVersion = getCurrentRuntimeJavaVersion();
    return !javaVersion.startsWith("1.");
  }
  
  public static JavaSpecificationVersion getCurrentJavaRuntimeVersionNumber() { return JavaSpecificationVersion.forCurrentJVM(); }
  
  public static String getCurrentRuntimeJavaVersion() {
    runtimeVersion = Runtime.version();
    return String.valueOf(runtimeVersion.version().get(0));
  }
}
