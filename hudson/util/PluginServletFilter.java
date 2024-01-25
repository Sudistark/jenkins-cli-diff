package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionPoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;

public final class PluginServletFilter implements Filter, ExtensionPoint {
  private final List<Filter> list = new CopyOnWriteArrayList();
  
  private FilterConfig config;
  
  private static final List<Filter> LEGACY = new Vector();
  
  private static final String KEY = PluginServletFilter.class.getName();
  
  @CheckForNull
  private static PluginServletFilter getInstance(ServletContext c) { return (PluginServletFilter)c.getAttribute(KEY); }
  
  public void init(FilterConfig config) throws ServletException {
    this.config = config;
    synchronized (LEGACY) {
      this.list.addAll(LEGACY);
      LEGACY.clear();
    } 
    for (Filter f : this.list)
      f.init(config); 
    config.getServletContext().setAttribute(KEY, this);
  }
  
  public static void addFilter(Filter filter) throws ServletException {
    Jenkins j = Jenkins.getInstanceOrNull();
    PluginServletFilter container = null;
    if (j != null)
      container = getInstance(j.servletContext); 
    if (j == null || container == null) {
      LOGGER.log(Level.WARNING, "Filter instance is registered too early: " + filter, new Exception());
      LEGACY.add(filter);
    } else {
      filter.init(container.config);
      container.list.add(filter);
    } 
  }
  
  public static boolean hasFilter(Filter filter) {
    Jenkins j = Jenkins.getInstanceOrNull();
    PluginServletFilter container = null;
    if (j != null)
      container = getInstance(j.servletContext); 
    if (j == null || container == null)
      return LEGACY.contains(filter); 
    return container.list.contains(filter);
  }
  
  public static void removeFilter(Filter filter) throws ServletException {
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j == null || getInstance(j.servletContext) == null) {
      LEGACY.remove(filter);
    } else {
      (getInstance(j.servletContext)).list.remove(filter);
    } 
  }
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    (new Object(this, chain))











      
      .doFilter(request, response);
  }
  
  public void destroy() {
    for (Filter f : this.list)
      f.destroy(); 
    this.list.clear();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static void cleanUp() {
    jenkins = Jenkins.getInstanceOrNull();
    if (jenkins == null)
      return; 
    PluginServletFilter instance = getInstance(jenkins.servletContext);
    if (instance != null) {
      for (Filter f : new ArrayList(instance.list)) {
        instance.list.remove(f);
        try {
          f.destroy();
        } catch (RuntimeException e) {
          LOGGER.log(Level.WARNING, "Filter " + f + " propagated an exception from its destroy method", e);
        } catch (Error e) {
          throw e;
        } catch (Throwable e) {
          LOGGER.log(Level.SEVERE, "Filter " + f + " propagated an exception from its destroy method", e);
        } 
      } 
      if (!instance.list.isEmpty())
        LOGGER.log(Level.SEVERE, "The following filters appear to have been added during clean up: {0}", instance.list); 
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(PluginServletFilter.class.getName());
}
