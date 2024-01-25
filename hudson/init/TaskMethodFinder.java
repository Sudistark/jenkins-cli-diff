package hudson.init;

import com.google.inject.Injector;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.jvnet.hudson.annotation_indexer.Index;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.Task;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.localizer.ResourceBundleHolder;

abstract class TaskMethodFinder<T extends Annotation> extends TaskBuilder {
  private static final Logger LOGGER = Logger.getLogger(TaskMethodFinder.class.getName());
  
  protected final ClassLoader cl;
  
  private final Set<Method> discovered;
  
  private final Class<T> type;
  
  private final Class<? extends Enum> milestoneType;
  
  TaskMethodFinder(Class<T> type, Class<? extends Enum> milestoneType, ClassLoader cl) {
    this.discovered = new HashSet();
    this.type = type;
    this.milestoneType = milestoneType;
    this.cl = cl;
  }
  
  public Collection<Task> discoverTasks(Reactor session) throws IOException {
    List<Task> result = new ArrayList<Task>();
    for (Method e : Index.list(this.type, this.cl, Method.class)) {
      if (filter(e))
        continue; 
      T i = (T)e.getAnnotation(this.type);
      if (i == null)
        continue; 
      result.add(new TaskImpl(this, i, e));
    } 
    return result;
  }
  
  protected boolean filter(Method e) { return !this.discovered.add(e); }
  
  protected String getDisplayNameOf(Method e, T i) {
    Class<?> c = e.getDeclaringClass();
    String key = displayNameOf(i);
    if (key.isEmpty())
      return c.getSimpleName() + "." + c.getSimpleName(); 
    try {
      ResourceBundleHolder rb = ResourceBundleHolder.get(c
          .getClassLoader().loadClass(c.getPackage().getName() + ".Messages"));
      return rb.format(key, new Object[0]);
    } catch (ClassNotFoundException x) {
      LOGGER.log(Level.WARNING, "Failed to load " + x.getMessage() + " for " + e, x);
      return key;
    } catch (MissingResourceException x) {
      LOGGER.log(Level.WARNING, "Could not find key '" + key + "' in " + c.getPackage().getName() + ".Messages", x);
      return key;
    } 
  }
  
  protected void invoke(Method e) {
    try {
      Class[] pt = e.getParameterTypes();
      Object[] args = new Object[pt.length];
      for (int i = 0; i < args.length; i++)
        args[i] = lookUp(pt[i]); 
      e.invoke(
          Modifier.isStatic(e.getModifiers()) ? null : lookUp(e.getDeclaringClass()), args);
    } catch (IllegalAccessException x) {
      throw (Error)(new IllegalAccessError()).initCause(x);
    } catch (InvocationTargetException x) {
      throw new Error(x);
    } 
  }
  
  private Object lookUp(Class<?> type) {
    Jenkins j = Jenkins.get();
    assert j != null : "This method is only invoked after the Jenkins singleton instance has been set";
    if (type == Jenkins.class || type == hudson.model.Hudson.class)
      return j; 
    Injector i = j.getInjector();
    if (i != null)
      return i.getInstance(type); 
    throw new IllegalArgumentException("Unable to inject " + type);
  }
  
  protected abstract String displayNameOf(T paramT);
  
  protected abstract String[] requiresOf(T paramT);
  
  protected abstract String[] attainsOf(T paramT);
  
  protected abstract Milestone afterOf(T paramT);
  
  protected abstract Milestone beforeOf(T paramT);
  
  protected abstract boolean fatalOf(T paramT);
}
