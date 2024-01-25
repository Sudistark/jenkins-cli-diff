package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Queue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Executables {
  @NonNull
  public static SubTask getParentOf(@NonNull Queue.Executable e) throws Error, RuntimeException {
    try {
      return e.getParent();
    } catch (AbstractMethodError ignored) {
      try {
        Method m = e.getClass().getMethod("getParent", new Class[0]);
        m.setAccessible(true);
        return (SubTask)m.invoke(e, new Object[0]);
      } catch (IllegalAccessException x) {
        throw (Error)(new IllegalAccessError()).initCause(x);
      } catch (NoSuchMethodException x) {
        throw (Error)(new NoSuchMethodError()).initCause(x);
      } catch (InvocationTargetException x) {
        Throwable y = x.getTargetException();
        if (y instanceof Error)
          throw (Error)y; 
        if (y instanceof RuntimeException)
          throw (RuntimeException)y; 
        throw new Error(x);
      } 
    } 
  }
  
  @Deprecated
  public static long getEstimatedDurationFor(@CheckForNull Queue.Executable e) {
    if (e == null)
      return -1L; 
    return e.getEstimatedDuration();
  }
}
