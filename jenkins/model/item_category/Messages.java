package jenkins.model.item_category;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String StandaloneProjects_DisplayName() { return holder.format("StandaloneProjects.DisplayName", new Object[0]); }
  
  public static Localizable _StandaloneProjects_DisplayName() { return new Localizable(holder, "StandaloneProjects.DisplayName", new Object[0]); }
  
  public static String NestedProjects_DisplayName() { return holder.format("NestedProjects.DisplayName", new Object[0]); }
  
  public static Localizable _NestedProjects_DisplayName() { return new Localizable(holder, "NestedProjects.DisplayName", new Object[0]); }
  
  public static String NestedProjects_Description() { return holder.format("NestedProjects.Description", new Object[0]); }
  
  public static Localizable _NestedProjects_Description() { return new Localizable(holder, "NestedProjects.Description", new Object[0]); }
  
  public static String StandaloneProjects_Description() { return holder.format("StandaloneProjects.Description", new Object[0]); }
  
  public static Localizable _StandaloneProjects_Description() { return new Localizable(holder, "StandaloneProjects.Description", new Object[0]); }
  
  public static String Uncategorized_Description() { return holder.format("Uncategorized.Description", new Object[0]); }
  
  public static Localizable _Uncategorized_Description() { return new Localizable(holder, "Uncategorized.Description", new Object[0]); }
  
  public static String Uncategorized_DisplayName() { return holder.format("Uncategorized.DisplayName", new Object[0]); }
  
  public static Localizable _Uncategorized_DisplayName() { return new Localizable(holder, "Uncategorized.DisplayName", new Object[0]); }
}
