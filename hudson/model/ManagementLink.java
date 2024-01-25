package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionListView;
import hudson.ExtensionPoint;
import hudson.security.Permission;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.management.Badge;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;

public abstract class ManagementLink implements ExtensionPoint, Action {
  @CheckForNull
  public abstract String getIconFileName();
  
  public String getDescription() { return ""; }
  
  @CheckForNull
  public abstract String getUrlName();
  
  public boolean getRequiresConfirmation() { return false; }
  
  @Deprecated
  public static final List<ManagementLink> LIST = ExtensionListView.createList(ManagementLink.class);
  
  @NonNull
  public static ExtensionList<ManagementLink> all() { return ExtensionList.lookup(ManagementLink.class); }
  
  @NonNull
  public Permission getRequiredPermission() { return Jenkins.ADMINISTER; }
  
  public boolean getRequiresPOST() { return false; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  protected String getCategoryName() { return "UNCATEGORIZED"; }
  
  @NonNull
  public Category getCategory() {
    try {
      return Category.valueOf(getCategoryName());
    } catch (RuntimeException e) {
      LOGGER.log(Level.WARNING, "invalid category {0} for class {1}", new Object[] { getCategoryName(), getClass().getName() });
      return Category.UNCATEGORIZED;
    } 
  }
  
  @CheckForNull
  public Badge getBadge() { return null; }
  
  private static final Logger LOGGER = Logger.getLogger(ManagementLink.class.getName());
}
