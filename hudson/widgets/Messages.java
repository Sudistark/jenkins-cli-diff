package hudson.widgets;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String BuildHistoryWidget_DisplayName() { return holder.format("BuildHistoryWidget.DisplayName", new Object[0]); }
  
  public static Localizable _BuildHistoryWidget_DisplayName() { return new Localizable(holder, "BuildHistoryWidget.DisplayName", new Object[0]); }
}
