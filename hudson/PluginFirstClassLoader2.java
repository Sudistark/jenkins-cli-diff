package hudson;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.util.CompoundEnumeration;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import jenkins.util.URLClassLoader2;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class PluginFirstClassLoader2 extends URLClassLoader2 {
  static  {
    registerAsParallelCapable();
  }
  
  public PluginFirstClassLoader2(@NonNull URL[] urls, @NonNull ClassLoader parent) { super((URL[])Objects.requireNonNull(urls), (ClassLoader)Objects.requireNonNull(parent)); }
  
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      Class<?> c = findLoadedClass(name);
      if (c == null)
        try {
          c = findClass(name);
        } catch (ClassNotFoundException classNotFoundException) {} 
      if (c == null)
        c = getParent().loadClass(name); 
      if (resolve)
        resolveClass(c); 
      return c;
    } 
  }
  
  public URL getResource(String name) {
    Objects.requireNonNull(name);
    URL url = findResource(name);
    if (url == null)
      url = getParent().getResource(name); 
    return url;
  }
  
  public Enumeration<URL> getResources(String name) throws IOException {
    Objects.requireNonNull(name);
    return new CompoundEnumeration(new Enumeration[] { findResources(name), getParent().getResources(name) });
  }
}
