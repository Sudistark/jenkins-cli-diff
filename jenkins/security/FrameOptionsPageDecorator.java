package jenkins.security;

import hudson.Extension;
import hudson.model.PageDecorator;
import jenkins.util.SystemProperties;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Extension(ordinal = 1000.0D)
@Symbol({"frameOptions"})
public class FrameOptionsPageDecorator extends PageDecorator {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean enabled = Boolean.parseBoolean(SystemProperties.getString(FrameOptionsPageDecorator.class.getName() + ".enabled", "true"));
}
