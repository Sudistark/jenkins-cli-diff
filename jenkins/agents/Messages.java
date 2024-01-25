package jenkins.agents;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String CloudSet_NoSuchCloud(Object arg0) { return holder.format("CloudSet.NoSuchCloud", new Object[] { arg0 }); }
  
  public static Localizable _CloudSet_NoSuchCloud(Object arg0) { return new Localizable(holder, "CloudSet.NoSuchCloud", new Object[] { arg0 }); }
  
  public static String CloudSet_DisplayName() { return holder.format("CloudSet.DisplayName", new Object[0]); }
  
  public static Localizable _CloudSet_DisplayName() { return new Localizable(holder, "CloudSet.DisplayName", new Object[0]); }
  
  public static String CloudsLink_Description() { return holder.format("CloudsLink.Description", new Object[0]); }
  
  public static Localizable _CloudsLink_Description() { return new Localizable(holder, "CloudsLink.Description", new Object[0]); }
  
  public static String CloudsLink_DisplayName() { return holder.format("CloudsLink.DisplayName", new Object[0]); }
  
  public static Localizable _CloudsLink_DisplayName() { return new Localizable(holder, "CloudsLink.DisplayName", new Object[0]); }
  
  public static String CloudSet_CloudAlreadyExists(Object arg0) { return holder.format("CloudSet.CloudAlreadyExists", new Object[] { arg0 }); }
  
  public static Localizable _CloudSet_CloudAlreadyExists(Object arg0) { return new Localizable(holder, "CloudSet.CloudAlreadyExists", new Object[] { arg0 }); }
  
  public static String CloudSet_SpecifyCloudToCopy() { return holder.format("CloudSet.SpecifyCloudToCopy", new Object[0]); }
  
  public static Localizable _CloudSet_SpecifyCloudToCopy() { return new Localizable(holder, "CloudSet.SpecifyCloudToCopy", new Object[0]); }
}
