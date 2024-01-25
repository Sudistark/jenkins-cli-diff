package jenkins;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import java.net.URL;

public interface PluginLocaleDrivenResourceProvider extends ExtensionPoint {
  @CheckForNull
  URL lookup(@NonNull String paramString);
}
