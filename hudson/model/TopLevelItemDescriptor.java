package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.lang.StringUtils;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconSpec;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;
import org.kohsuke.stapler.jelly.JellyClassTearOff;
import org.springframework.security.access.AccessDeniedException;

public abstract class TopLevelItemDescriptor extends Descriptor<TopLevelItem> implements IconSpec {
  private static final Logger LOGGER = Logger.getLogger(TopLevelItemDescriptor.class.getName());
  
  protected TopLevelItemDescriptor(Class<? extends TopLevelItem> clazz) { super(clazz); }
  
  protected TopLevelItemDescriptor() {}
  
  public boolean isApplicable(Descriptor descriptor) { return true; }
  
  public boolean isApplicableIn(ItemGroup parent) { return true; }
  
  public final void checkApplicableIn(ItemGroup parent) {
    if (!isApplicableIn(parent))
      throw new AccessDeniedException(
          Messages.TopLevelItemDescriptor_NotApplicableIn(getDisplayName(), parent.getFullDisplayName())); 
  }
  
  public boolean testInstance(TopLevelItem i) { return this.clazz.isInstance(i); }
  
  @NonNull
  public String getDisplayName() { return super.getDisplayName(); }
  
  @NonNull
  public String getDescription() {
    Stapler stapler = Stapler.getCurrent();
    if (stapler != null)
      try {
        WebApp webapp = WebApp.getCurrent();
        MetaClass meta = webapp.getMetaClass(this);
        Script s = (Script)((JellyClassTearOff)meta.loadTearOff(JellyClassTearOff.class)).findScript("newInstanceDetail");
        if (s == null)
          return ""; 
        DefaultScriptInvoker dsi = new DefaultScriptInvoker();
        StringWriter sw = new StringWriter();
        XMLOutput xml = dsi.createXMLOutput(sw, true);
        dsi.invokeScript(Stapler.getCurrentRequest(), Stapler.getCurrentResponse(), s, this, xml);
        return sw.toString();
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, null, e);
        return "";
      }  
    return "";
  }
  
  @NonNull
  public String getCategoryId() { return "uncategorized"; }
  
  @Deprecated
  @CheckForNull
  public String getIconFilePathPattern() { return null; }
  
  @Deprecated
  @CheckForNull
  public String getIconFilePath(String size) {
    if (!StringUtils.isBlank(getIconFilePathPattern()))
      return getIconFilePathPattern().replace(":size", size); 
    return null;
  }
  
  public String getIconClassName() {
    String pattern = getIconFilePathPattern();
    if (pattern != null) {
      String path = pattern.replace(":size", "24x24");
      if (path.indexOf('/') == -1)
        return IconSet.toNormalizedIconNameClass(path); 
      if (Jenkins.RESOURCE_PATH.length() > 0 && path.startsWith(Jenkins.RESOURCE_PATH))
        path = path.substring(Jenkins.RESOURCE_PATH.length()); 
      Icon icon = IconSet.icons.getIconByUrl(path);
      if (icon != null)
        return icon.getClassSpec().replaceAll("\\s*icon-md\\s*", " ").replaceAll("\\s+", " "); 
    } 
    return null;
  }
  
  @Deprecated
  public TopLevelItem newInstance(StaplerRequest req) throws Descriptor.FormException { throw new UnsupportedOperationException(); }
  
  @Deprecated
  public TopLevelItem newInstance(String name) { return newInstance(Jenkins.get(), name); }
  
  public static ExtensionList<TopLevelItemDescriptor> all() { return Items.all(); }
  
  public abstract TopLevelItem newInstance(ItemGroup paramItemGroup, String paramString);
}
