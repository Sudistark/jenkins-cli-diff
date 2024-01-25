package jenkins;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.RestrictedSince;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import jenkins.util.JenkinsClassLoader;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@RestrictedSince("2.324")
public class ClassLoaderReflectionToolkit {
  private static <T extends Exception> Object invoke(Method method, Class<T> exception, Object obj, Object... args) throws T {
    try {
      return method.invoke(obj, args);
    } catch (IllegalAccessException x) {
      throw new LinkageError(x.getMessage(), x);
    } catch (InvocationTargetException x) {
      Throwable x2 = x.getCause();
      if (x2 instanceof RuntimeException)
        throw (RuntimeException)x2; 
      if (x2 instanceof Error)
        throw (Error)x2; 
      if (exception.isInstance(x2))
        throw (Exception)exception.cast(x2); 
      throw new AssertionError(x2);
    } 
  }
  
  private static Object getClassLoadingLock(ClassLoader cl, String name) {
    if (cl instanceof JenkinsClassLoader)
      return ((JenkinsClassLoader)cl).getClassLoadingLock(name); 
    return invoke(GetClassLoadingLock.GET_CLASS_LOADING_LOCK, RuntimeException.class, cl, new Object[] { name });
  }
  
  @NonNull
  public static Class<?> loadClass(ClassLoader cl, String name) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(cl, name)) {
      Class<?> c;
      if (cl instanceof JenkinsClassLoader) {
        c = ((JenkinsClassLoader)cl).findLoadedClass2(name);
      } else {
        c = (Class)invoke(FindLoadedClass.FIND_LOADED_CLASS, RuntimeException.class, cl, new Object[] { name });
      } 
      if (c == null)
        if (cl instanceof JenkinsClassLoader) {
          c = ((JenkinsClassLoader)cl).findClass(name);
        } else {
          c = (Class)invoke(FindClass.FIND_CLASS, ClassNotFoundException.class, cl, new Object[] { name });
        }  
      return c;
    } 
  }
  
  @CheckForNull
  public static URL _findResource(ClassLoader cl, String name) {
    URL url;
    if (cl instanceof JenkinsClassLoader) {
      url = ((JenkinsClassLoader)cl).findResource(name);
    } else if (cl instanceof URLClassLoader) {
      url = ((URLClassLoader)cl).findResource(name);
    } else {
      url = (URL)invoke(FindResource.FIND_RESOURCE, RuntimeException.class, cl, new Object[] { name });
    } 
    return url;
  }
  
  @NonNull
  public static Enumeration<URL> _findResources(ClassLoader cl, String name) throws IOException {
    Enumeration<URL> urls;
    if (cl instanceof JenkinsClassLoader) {
      urls = ((JenkinsClassLoader)cl).findResources(name);
    } else {
      urls = (Enumeration)invoke(FindResources.FIND_RESOURCES, IOException.class, cl, new Object[] { name });
    } 
    return urls;
  }
}
