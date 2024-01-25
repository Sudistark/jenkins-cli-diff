package hudson.init.impl;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String InitialUserContent_init() { return holder.format("InitialUserContent.init", new Object[0]); }
  
  public static Localizable _InitialUserContent_init() { return new Localizable(holder, "InitialUserContent.init", new Object[0]); }
  
  public static String GroovyInitScript_init() { return holder.format("GroovyInitScript.init", new Object[0]); }
  
  public static Localizable _GroovyInitScript_init() { return new Localizable(holder, "GroovyInitScript.init", new Object[0]); }
}
