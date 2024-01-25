package jenkins.util;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public interface JenkinsClassLoader {
  Class<?> findClass(String paramString) throws ClassNotFoundException;
  
  Class<?> findLoadedClass2(String paramString) throws ClassNotFoundException;
  
  URL findResource(String paramString);
  
  Enumeration<URL> findResources(String paramString) throws IOException;
  
  Object getClassLoadingLock(String paramString);
}
