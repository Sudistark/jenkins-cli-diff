package hudson;

import com.thoughtworks.xstream.core.JVM;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.AWTProblem;
import hudson.util.BootFailure;
import hudson.util.ChartUtil;
import hudson.util.HudsonIsLoading;
import hudson.util.IncompatibleAntVersionDetected;
import hudson.util.IncompatibleServletVersionDetected;
import hudson.util.IncompatibleVMDetected;
import hudson.util.InsufficientPermissionDetected;
import hudson.util.NoHomeDir;
import hudson.util.NoTempDir;
import hudson.util.RingBufferLogHandler;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.security.Security;
import java.util.Date;
import java.util.EnumSet;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.jelly.JellyFacet;

public class WebAppMain implements ServletContextListener {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String FORCE_SESSION_TRACKING_BY_COOKIE_PROP = WebAppMain.class.getName() + ".forceSessionTrackingByCookie";
  
  private final RingBufferLogHandler handler = new Object(this, getDefaultRingBufferSize());
  
  private static final String APP = "app";
  
  private boolean terminated;
  
  private Thread initThread;
  
  public static int getDefaultRingBufferSize() { return RingBufferLogHandler.getDefaultRingBufferSize(); }
  
  public void contextInitialized(ServletContextEvent event) {
    if (Main.isDevelopmentMode && System.getProperty("java.util.logging.config.file") == null)
      try {
        Formatter formatter = (Formatter)Class.forName("io.jenkins.lib.support_log_formatter.SupportLogFormatter").getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        for (Handler h : Logger.getLogger("").getHandlers()) {
          if (h instanceof java.util.logging.ConsoleHandler)
            h.setFormatter(formatter); 
        } 
      } catch (ClassNotFoundException classNotFoundException) {
      
      } catch (Exception x) {
        LOGGER.log(Level.WARNING, null, x);
      }  
    JenkinsJVMAccess._setJenkinsJVM(true);
    ServletContext context = event.getServletContext();
    File home = null;
    try {
      JVM jvm;
      LocaleProvider.setProvider(new Object(this));
      try {
        jvm = new JVM();
        new URLClassLoader(new java.net.URL[0], getClass().getClassLoader());
      } catch (SecurityException e) {
        throw new InsufficientPermissionDetected(e);
      } 
      try {
        Security.removeProvider("SunPKCS11-Solaris");
      } catch (SecurityException securityException) {}
      installLogger();
      FileAndDescription describedHomeDir = getHomeDir(event);
      home = describedHomeDir.file.getAbsoluteFile();
      try {
        Util.createDirectories(home.toPath(), new java.nio.file.attribute.FileAttribute[0]);
      } catch (IOException|java.nio.file.InvalidPathException e) {
        throw (NoHomeDir)(new NoHomeDir(home)).initCause(e);
      } 
      LOGGER.info("Jenkins home directory: " + home + " found at: " + describedHomeDir.description);
      recordBootAttempt(home);
      if (jvm.bestReflectionProvider().getClass() == com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider.class)
        throw new IncompatibleVMDetected(); 
      try {
        javax.servlet.ServletResponse.class.getMethod("setCharacterEncoding", new Class[] { String.class });
      } catch (NoSuchMethodException e) {
        throw (IncompatibleServletVersionDetected)(new IncompatibleServletVersionDetected(javax.servlet.ServletResponse.class)).initCause(e);
      } 
      try {
        org.apache.tools.ant.types.FileSet.class.getMethod("getDirectoryScanner", new Class[0]);
      } catch (NoSuchMethodException e) {
        throw (IncompatibleAntVersionDetected)(new IncompatibleAntVersionDetected(org.apache.tools.ant.types.FileSet.class)).initCause(e);
      } 
      if (ChartUtil.awtProblemCause != null)
        throw new AWTProblem(ChartUtil.awtProblemCause); 
      try {
        File f = File.createTempFile("test", "test");
        boolean result = f.delete();
        if (!result)
          LOGGER.log(Level.FINE, "Temp file test.test could not be deleted."); 
      } catch (IOException e) {
        throw new NoTempDir(e);
      } 
      installExpressionFactory(event);
      context.setAttribute("app", new HudsonIsLoading());
      if (SystemProperties.getBoolean(FORCE_SESSION_TRACKING_BY_COOKIE_PROP, true))
        context.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE)); 
      File _home = home;
      this.initThread = new Object(this, "Jenkins initialization thread", _home, context);
      this.initThread.start();
    } catch (BootFailure e) {
      JVM jvm;
      jvm.publish(context, home);
    } catch (Error|RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Failed to initialize Jenkins", e);
      throw e;
    } 
  }
  
  public void joinInit() { this.initThread.join(); }
  
  private void recordBootAttempt(File home) {
    try {
      OutputStream o = Files.newOutputStream(BootFailure.getBootFailureFile(home).toPath(), new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND });
      try {
        o.write(("" + new Date() + new Date()).getBytes(Charset.defaultCharset()));
        if (o != null)
          o.close(); 
      } catch (Throwable throwable) {
        if (o != null)
          try {
            o.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException|java.nio.file.InvalidPathException e) {
      LOGGER.log(Level.WARNING, "Failed to record boot attempts", e);
    } 
  }
  
  public static void installExpressionFactory(ServletContextEvent event) { JellyFacet.setExpressionFactory(event, new ExpressionFactory2()); }
  
  private void installLogger() {
    Jenkins.logRecords = this.handler.getView();
    Logger.getLogger("").addHandler(this.handler);
  }
  
  public FileAndDescription getHomeDir(ServletContextEvent event) {
    for (String name : HOME_NAMES) {
      String sysProp = SystemProperties.getString(name);
      if (sysProp != null)
        return new FileAndDescription(new File(sysProp.trim()), "SystemProperties.getProperty(\"" + name + "\")"); 
    } 
    for (String name : HOME_NAMES) {
      String env = (String)EnvVars.masterEnvVars.get(name);
      if (env != null)
        return new FileAndDescription((new File(env.trim())).getAbsoluteFile(), "EnvVars.masterEnvVars.get(\"" + name + "\")"); 
    } 
    String root = event.getServletContext().getRealPath("/WEB-INF/workspace");
    if (root != null) {
      File ws = new File(root.trim());
      if (ws.exists())
        return new FileAndDescription(ws, "getServletContext().getRealPath(\"/WEB-INF/workspace\")"); 
    } 
    File legacyHome = new File(new File(System.getProperty("user.home")), ".hudson");
    if (legacyHome.exists())
      return new FileAndDescription(legacyHome, "$user.home/.hudson"); 
    File newHome = new File(new File(System.getProperty("user.home")), ".jenkins");
    return new FileAndDescription(newHome, "$user.home/.jenkins");
  }
  
  public void contextDestroyed(ServletContextEvent event) {
    try {
      ACLContext old = ACL.as2(ACL.SYSTEM2);
      try {
        Jenkins instance = Jenkins.getInstanceOrNull();
        try {
          if (instance != null)
            instance.cleanUp(); 
        } catch (Throwable e) {
          LOGGER.log(Level.SEVERE, "Failed to clean up. Restart will continue.", e);
        } 
        this.terminated = true;
        Thread t = this.initThread;
        if (t != null && t.isAlive()) {
          LOGGER.log(Level.INFO, "Shutting down a Jenkins instance that was still starting up", new Throwable("reason"));
          t.interrupt();
        } 
        Logger.getLogger("").removeHandler(this.handler);
        if (old != null)
          old.close(); 
      } catch (Throwable throwable) {
        if (old != null)
          try {
            old.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } finally {
      JenkinsJVMAccess._setJenkinsJVM(false);
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(WebAppMain.class.getName());
  
  private static final String[] HOME_NAMES = { "JENKINS_HOME", "HUDSON_HOME" };
}
