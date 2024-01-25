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

public abstract class SettingsProvider extends AbstractDescribableImpl<SettingsProvider> implements ExtensionPoint {
  public abstract FilePath supplySettings(AbstractBuild<?, ?> paramAbstractBuild, TaskListener paramTaskListener);
  
  public static SettingsProvider parseSettingsProvider(StaplerRequest req) throws Descriptor.FormException, ServletException {
    JSONObject settings = req.getSubmittedForm().getJSONObject("settings");
    if (settings == null)
      return new DefaultSettingsProvider(); 
    return (SettingsProvider)req.bindJSON(SettingsProvider.class, settings);
  }
  
  public static FilePath getSettingsFilePath(SettingsProvider settings, AbstractBuild<?, ?> build, TaskListener listener) {
    FilePath settingsPath = null;
    if (settings != null)
      settingsPath = settings.supplySettings(build, listener); 
    return settingsPath;
  }
  
  public static String getSettingsRemotePath(SettingsProvider settings, AbstractBuild<?, ?> build, TaskListener listener) {
    FilePath fp = getSettingsFilePath(settings, build, listener);
    return (fp == null) ? null : fp.getRemote();
  }
}
