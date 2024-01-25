package jenkins.util;

import java.net.URL;
import java.net.URLClassLoader;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class URLClassLoader2 extends URLClassLoader implements JenkinsClassLoader {
  static  {
    registerAsParallelCapable();
  }
  
  public URLClassLoader2(URL[] urls) { super(urls); }
  
  public URLClassLoader2(URL[] urls, ClassLoader parent) { super(urls, parent); }
  
  public void addURL(URL url) { super.addURL(url); }
  
  public Class<?> findClass(String name) throws ClassNotFoundException { return super.findClass(name); }
  
  public Class<?> findLoadedClass2(String name) throws ClassNotFoundException { return findLoadedClass(name); }
  
  public Object getClassLoadingLock(String className) { return super.getClassLoadingLock(className); }
}
