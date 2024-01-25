package jenkins.mvn;

import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public abstract class GlobalSettingsProvider extends AbstractDescribableImpl<GlobalSettingsProvider> implements ExtensionPoint {
  public abstract FilePath supplySettings(AbstractBuild<?, ?> paramAbstractBuild, TaskListener paramTaskListener);
  
  public static GlobalSettingsProvider parseSettingsProvider(StaplerRequest req) throws Descriptor.FormException, ServletException {
    JSONObject settings = req.getSubmittedForm().getJSONObject("globalSettings");
    if (settings == null)
      return new DefaultGlobalSettingsProvider(); 
    return (GlobalSettingsProvider)req.bindJSON(GlobalSettingsProvider.class, settings);
  }
  
  public static FilePath getSettingsFilePath(GlobalSettingsProvider settings, AbstractBuild<?, ?> build, TaskListener listener) {
    FilePath settingsPath = null;
    if (settings != null)
      settingsPath = settings.supplySettings(build, listener); 
    return settingsPath;
  }
  
  public static String getSettingsRemotePath(GlobalSettingsProvider provider, AbstractBuild<?, ?> build, TaskListener listener) {
    FilePath fp = getSettingsFilePath(provider, build, listener);
    return (fp == null) ? null : fp.getRemote();
  }
}
