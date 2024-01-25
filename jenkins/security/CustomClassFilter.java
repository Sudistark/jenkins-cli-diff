package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionPoint;

public interface CustomClassFilter extends ExtensionPoint {
  @CheckForNull
  default Boolean permits(Class<?> c) { return null; }
  
  @CheckForNull
  default Boolean permits(String name) { return null; }
}
