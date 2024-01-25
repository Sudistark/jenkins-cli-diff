package hudson.logging;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.RestrictedSince;
import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AbstractModelObject;
import hudson.model.Failure;
import hudson.model.RSS;
import hudson.util.CopyOnWriteMap;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithChildren;
import jenkins.model.ModelObjectWithContextMenu;
import jenkins.util.SystemProperties;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class LogRecorderManager extends AbstractModelObject implements ModelObjectWithChildren, StaplerProxy {
  private static final Logger LOGGER = Logger.getLogger(LogRecorderManager.class.getName());
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.323")
  public final Map<String, LogRecorder> logRecorders = new CopyOnWriteMap.Tree();
  
  private List<LogRecorder> recorders = new ArrayList();
  
  public List<LogRecorder> getRecorders() { return this.recorders; }
  
  @DataBoundSetter
  public void setRecorders(List<LogRecorder> recorders) {
    this.recorders = recorders;
    Map<String, LogRecorder> values = (Map)recorders.stream().collect(Collectors.toMap(LogRecorder::getName, 
          
          Function.identity(), (recorder1, recorder2) -> {
            LOGGER.warning(String.format("Ignoring duplicate log recorder '%s', check $JENKINS_HOME/log and remove the duplicate recorder", new Object[] { recorder2.getName() }));
            return recorder1;
          }));
    ((CopyOnWriteMap)this.logRecorders).replaceBy(values);
  }
  
  public String getDisplayName() { return Messages.LogRecorderManager_DisplayName(); }
  
  public String getSearchUrl() { return "/log"; }
  
  public LogRecorder getDynamic(String token) { return getLogRecorder(token); }
  
  public LogRecorder getLogRecorder(String token) { return (LogRecorder)this.recorders.stream().filter(logRecorder -> logRecorder.getName().equals(token)).findAny().orElse(null); }
  
  static File configDir() { return new File(Jenkins.get().getRootDir(), "log"); }
  
  public void load() {
    this.recorders.clear();
    File dir = configDir();
    File[] files = dir.listFiles(new WildcardFileFilter("*.xml"));
    if (files == null)
      return; 
    for (File child : files) {
      String name = child.getName();
      name = name.substring(0, name.length() - 4);
      LogRecorder lr = new LogRecorder(name);
      lr.load();
      this.recorders.add(lr);
    } 
    setRecorders(this.recorders);
  }
  
  @RequirePOST
  public HttpResponse doNewLogRecorder(@QueryParameter String name) {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    Jenkins.checkGoodName(name);
    this.recorders.add(new LogRecorder(name));
    return new HttpRedirect(name + "/configure");
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public FormValidation doCheckNewName(@QueryParameter String name) {
    if (Util.fixEmpty(name) == null)
      return FormValidation.ok(); 
    try {
      Jenkins.checkGoodName(name);
    } catch (Failure e) {
      return FormValidation.error(e.getMessage());
    } 
    return FormValidation.ok();
  }
  
  public ModelObjectWithContextMenu.ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
    ModelObjectWithContextMenu.ContextMenu menu = new ModelObjectWithContextMenu.ContextMenu();
    menu.add("all", "All Jenkins Logs");
    for (LogRecorder lr : this.recorders)
      menu.add(lr.getSearchUrl(), lr.getDisplayName()); 
    return menu;
  }
  
  @RequirePOST
  @SuppressFBWarnings(value = {"LG_LOST_LOGGER_DUE_TO_WEAK_REFERENCE"}, justification = "if the logger is known, then we have a reference to it in LogRecorder#loggers")
  public HttpResponse doConfigLogger(@QueryParameter String name, @QueryParameter String level) {
    Level lv;
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (level.equals("inherit")) {
      lv = null;
    } else {
      lv = Level.parse(level.toUpperCase(Locale.ENGLISH));
    } 
    Logger target;
    if (Collections.list(LogManager.getLogManager().getLoggerNames()).contains(name) && (
      target = Logger.getLogger(name)) != null) {
      target.setLevel(lv);
      return new HttpRedirect("levels");
    } 
    throw new Failure(Messages.LogRecorderManager_LoggerNotFound(name));
  }
  
  public void doRss(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { doRss(req, rsp, Jenkins.logRecords); }
  
  static void doRss(StaplerRequest req, StaplerResponse rsp, List<LogRecord> logs) throws IOException, ServletException {
    String entryType = "all";
    String level = req.getParameter("level");
    if (level != null) {
      Level threshold = Level.parse(level);
      List<LogRecord> filtered = new ArrayList<LogRecord>();
      for (LogRecord r : logs) {
        if (r.getLevel().intValue() >= threshold.intValue())
          filtered.add(r); 
      } 
      logs = filtered;
      entryType = level;
    } 
    RSS.forwardToRss("Jenkins:log (" + entryType + " entries)", "", logs, new Object(), req, rsp);
  }
  
  @Initializer(before = InitMilestone.PLUGINS_PREPARED)
  public static void init(Jenkins h) throws IOException { h.getLog().load(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Object getTarget() {
    if (!SKIP_PERMISSION_CHECK)
      Jenkins.get().checkPermission(Jenkins.SYSTEM_READ); 
    return this;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_PERMISSION_CHECK = SystemProperties.getBoolean(LogRecorderManager.class.getName() + ".skipPermissionCheck");
}
