package jenkins;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.LocaleDrivenResourceProvider;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public final class MetaLocaleDrivenResourceProvider extends LocaleDrivenResourceProvider {
  private static final Logger LOGGER = Logger.getLogger(MetaLocaleDrivenResourceProvider.class.getName());
  
  @CheckForNull
  public URL lookup(@NonNull String s) {
    for (PluginLocaleDrivenResourceProvider provider : ExtensionList.lookup(PluginLocaleDrivenResourceProvider.class)) {
      try {
        URL url = provider.lookup(s);
        if (url != null)
          return url; 
      } catch (RuntimeException e) {
        LOGGER.log(Level.WARNING, "Failed to lookup URL for '" + s + "' from '" + provider.toString(), e);
      } 
    } 
    return null;
  }
}
