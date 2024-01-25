package hudson.util;

import hudson.remoting.Which;
import java.io.IOException;
import java.net.URL;

public class IncompatibleAntVersionDetected extends BootFailure {
  private final Class antClass;
  
  public IncompatibleAntVersionDetected(Class antClass) { this.antClass = antClass; }
  
  public String getMessage() {
    try {
      return "Incompatible Ant loaded from " + getWhereAntIsLoaded();
    } catch (IOException e) {
      return "Incompatible Ant loaded";
    } 
  }
  
  public URL getWhereAntIsLoaded() throws IOException { return Which.classFileUrl(this.antClass); }
}
