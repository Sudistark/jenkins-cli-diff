package jenkins.fingerprints;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String FileFingerprintStorage_DisplayName() { return holder.format("FileFingerprintStorage.DisplayName", new Object[0]); }
  
  public static Localizable _FileFingerprintStorage_DisplayName() { return new Localizable(holder, "FileFingerprintStorage.DisplayName", new Object[0]); }
}
