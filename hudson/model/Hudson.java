package hudson.model;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.ExtensionListView;
import hudson.Functions;
import hudson.Platform;
import hudson.PluginManager;
import hudson.Util;
import hudson.cli.declarative.CLIResolver;
import hudson.model.listeners.ItemListener;
import hudson.slaves.ComputerListener;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import org.jvnet.hudson.reactor.ReactorException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class Hudson extends Jenkins {
  @Deprecated
  private final CopyOnWriteList<ItemListener> itemListeners = ExtensionListView.createCopyOnWriteList(ItemListener.class);
  
  @Deprecated
  private final CopyOnWriteList<ComputerListener> computerListeners = ExtensionListView.createCopyOnWriteList(ComputerListener.class);
  
  @Deprecated
  @CLIResolver
  @Nullable
  public static Hudson getInstance() { return (Hudson)Jenkins.get(); }
  
  public Hudson(File root, ServletContext context) throws IOException, InterruptedException, ReactorException { this(root, context, null); }
  
  public Hudson(File root, ServletContext context, PluginManager pluginManager) throws IOException, InterruptedException, ReactorException { super(root, context, pluginManager); }
  
  @Deprecated
  public CopyOnWriteList<ItemListener> getJobListeners() { return this.itemListeners; }
  
  @Deprecated
  public CopyOnWriteList<ComputerListener> getComputerListeners() { return this.computerListeners; }
  
  @Deprecated
  public Slave getSlave(String name) {
    Node n = getNode(name);
    if (n instanceof Slave)
      return (Slave)n; 
    return null;
  }
  
  @Deprecated
  public List<Slave> getSlaves() { return getNodes(); }
  
  @Deprecated
  public void setSlaves(List<Slave> slaves) throws IOException { setNodes(slaves); }
  
  @Deprecated
  public TopLevelItem getJob(String name) { return getItem(name); }
  
  @Deprecated
  public TopLevelItem getJobCaseInsensitive(String name) {
    String match = Functions.toEmailSafeString(name);
    for (TopLevelItem item : getItems()) {
      if (Functions.toEmailSafeString(item.getName()).equalsIgnoreCase(match))
        return item; 
    } 
    return null;
  }
  
  @Deprecated
  @RequirePOST
  public void doQuietDown(StaplerResponse rsp) throws IOException, ServletException { doQuietDown().generateResponse(null, rsp, this); }
  
  @Deprecated
  public void doLogRss(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    String qs = req.getQueryString();
    rsp.sendRedirect2("./log/rss" + ((qs == null) ? "" : ("?" + qs)));
  }
  
  @Deprecated
  public void doFieldCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    doFieldCheck(
        Util.fixEmpty(req.getParameter("value")), 
        Util.fixEmpty(req.getParameter("type")), 
        Util.fixEmpty(req.getParameter("errorText")), 
        Util.fixEmpty(req.getParameter("warningText"))).generateResponse(req, rsp, this);
  }
  
  @Deprecated
  public FormValidation doFieldCheck(@QueryParameter(fixEmpty = true) String value, @QueryParameter(fixEmpty = true) String type, @QueryParameter(fixEmpty = true) String errorText, @QueryParameter(fixEmpty = true) String warningText) {
    if (value == null) {
      if (errorText != null)
        return FormValidation.error(errorText); 
      if (warningText != null)
        return FormValidation.warning(warningText); 
      return FormValidation.error("No error or warning text was set for fieldCheck().");
    } 
    if (type != null)
      try {
        if (type.equalsIgnoreCase("number")) {
          NumberFormat.getInstance().parse(value);
        } else if (type.equalsIgnoreCase("number-positive")) {
          if (NumberFormat.getInstance().parse(value).floatValue() <= 0.0F)
            return FormValidation.error(Messages.Hudson_NotAPositiveNumber()); 
        } else if (type.equalsIgnoreCase("number-negative") && 
          NumberFormat.getInstance().parse(value).floatValue() >= 0.0F) {
          return FormValidation.error(Messages.Hudson_NotANegativeNumber());
        } 
      } catch (ParseException e) {
        return FormValidation.error(Messages.Hudson_NotANumber());
      }  
    return FormValidation.ok();
  }
  
  @Deprecated
  public static boolean isWindows() { return (File.pathSeparatorChar == ';'); }
  
  @Deprecated
  public static boolean isDarwin() { return Platform.isDarwin(); }
  
  @Deprecated
  public static boolean adminCheck() { return adminCheck(Stapler.getCurrentRequest(), Stapler.getCurrentResponse()); }
  
  @Deprecated
  public static boolean adminCheck(StaplerRequest req, StaplerResponse rsp) throws IOException {
    if (isAdmin(req))
      return true; 
    rsp.sendError(403);
    return false;
  }
  
  @Deprecated
  public static boolean isAdmin() { return Jenkins.get().hasPermission(ADMINISTER); }
  
  @Deprecated
  public static boolean isAdmin(StaplerRequest req) { return isAdmin(); }
  
  static  {
    XSTREAM.alias("hudson", Hudson.class);
  }
}
