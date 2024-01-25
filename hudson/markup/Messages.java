package hudson.markup;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String EscapedMarkupFormatter_DisplayName() { return holder.format("EscapedMarkupFormatter.DisplayName", new Object[0]); }
  
  public static Localizable _EscapedMarkupFormatter_DisplayName() { return new Localizable(holder, "EscapedMarkupFormatter.DisplayName", new Object[0]); }
}
