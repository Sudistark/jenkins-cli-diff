package hudson.scm;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String SCM_Permissions_Title() { return holder.format("SCM.Permissions.Title", new Object[0]); }
  
  public static Localizable _SCM_Permissions_Title() { return new Localizable(holder, "SCM.Permissions.Title", new Object[0]); }
  
  public static String NullSCM_DisplayName() { return holder.format("NullSCM.DisplayName", new Object[0]); }
  
  public static Localizable _NullSCM_DisplayName() { return new Localizable(holder, "NullSCM.DisplayName", new Object[0]); }
  
  public static String SCM_TagPermission_Description() { return holder.format("SCM.TagPermission.Description", new Object[0]); }
  
  public static Localizable _SCM_TagPermission_Description() { return new Localizable(holder, "SCM.TagPermission.Description", new Object[0]); }
}
