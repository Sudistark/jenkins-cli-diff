package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;

public abstract class DescriptorVisibilityFilter implements ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(DescriptorVisibilityFilter.class.getName());
  
  public boolean filterType(@NonNull Class<?> contextClass, @NonNull Descriptor descriptor) { return true; }
  
  public abstract boolean filter(@CheckForNull Object paramObject, @NonNull Descriptor paramDescriptor);
  
  public static ExtensionList<DescriptorVisibilityFilter> all() { return ExtensionList.lookup(DescriptorVisibilityFilter.class); }
  
  public static <T extends Descriptor> List<T> apply(Object context, Iterable<T> source) {
    ExtensionList<DescriptorVisibilityFilter> filters = all();
    List<T> r = new ArrayList<T>();
    Class<?> contextClass = (context == null) ? null : context.getClass();
    if (source == null)
      throw new NullPointerException("Descriptor list is null for context '" + contextClass + "' in thread '" + Thread.currentThread().getName() + "'"); 
    Iterator iterator;
    label42: for (iterator = source.iterator(); iterator.hasNext(); ) {
      T d = (T)(Descriptor)iterator.next();
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.fine("Determining visibility of " + d + " in context " + context); 
      for (DescriptorVisibilityFilter f : filters) {
        if (LOGGER.isLoggable(Level.FINER))
          LOGGER.finer("Querying " + f + " for visibility of " + d + " in " + context); 
        try {
          if (contextClass != null && !f.filterType(contextClass, d)) {
            if (LOGGER.isLoggable(Level.CONFIG)) {
              LOGGER.config("Filter " + f + " hides " + d + " in contexts of type " + contextClass);
              continue label42;
            } 
            continue label42;
          } 
          if (!f.filter(context, d)) {
            if (LOGGER.isLoggable(Level.CONFIG)) {
              LOGGER.config("Filter " + f + " hides " + d + " in context " + context);
              continue label42;
            } 
            continue label42;
          } 
        } catch (Error e) {
          LOGGER.log(Level.WARNING, "Encountered error while processing filter " + f + " for context " + context, e);
          throw e;
        } catch (Throwable e) {
          LOGGER.log(logLevelFor(f), "Uncaught exception from filter " + f + " for context " + context, e);
          continue label42;
        } 
      } 
      r.add(d);
    } 
    return r;
  }
  
  public static <T extends Descriptor> List<T> applyType(Class<?> contextClass, Iterable<T> source) {
    ExtensionList<DescriptorVisibilityFilter> filters = all();
    List<T> r = new ArrayList<T>();
    Iterator iterator;
    label29: for (iterator = source.iterator(); iterator.hasNext(); ) {
      T d = (T)(Descriptor)iterator.next();
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.fine("Determining visibility of " + d + " in contexts of type " + contextClass); 
      for (DescriptorVisibilityFilter f : filters) {
        if (LOGGER.isLoggable(Level.FINER))
          LOGGER.finer("Querying " + f + " for visibility of " + d + " in type " + contextClass); 
        try {
          if (contextClass != null && !f.filterType(contextClass, d)) {
            if (LOGGER.isLoggable(Level.CONFIG)) {
              LOGGER.config("Filter " + f + " hides " + d + " in contexts of type " + contextClass);
              continue label29;
            } 
            continue label29;
          } 
        } catch (Error e) {
          LOGGER.log(Level.WARNING, "Encountered error while processing filter " + f + " for contexts of type " + contextClass, e);
          throw e;
        } catch (Throwable e) {
          LOGGER.log(logLevelFor(f), "Uncaught exception from filter " + f + " for context of type " + contextClass, e);
          continue label29;
        } 
      } 
      r.add(d);
    } 
    return r;
  }
  
  private static Level logLevelFor(DescriptorVisibilityFilter f) {
    Long interval = SystemProperties.getLong(DescriptorVisibilityFilter.class
        .getName() + ".badFilterLogWarningIntervalMinutes", 
        Long.valueOf(60L));
    synchronized (ResourceHolder.BAD_FILTERS) {
      Long lastTime = (Long)ResourceHolder.BAD_FILTERS.get(f);
      if (lastTime == null || lastTime.longValue() + TimeUnit.MINUTES.toMillis(interval.longValue()) < System.currentTimeMillis()) {
        ResourceHolder.BAD_FILTERS.put(f, Long.valueOf(System.currentTimeMillis()));
        return Level.WARNING;
      } 
      return Level.FINE;
    } 
  }
}
