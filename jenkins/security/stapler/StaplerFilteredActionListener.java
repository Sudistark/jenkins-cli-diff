package jenkins.security.stapler;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.event.FilteredDispatchTriggerListener;
import org.kohsuke.stapler.event.FilteredDoActionTriggerListener;
import org.kohsuke.stapler.event.FilteredFieldTriggerListener;
import org.kohsuke.stapler.event.FilteredGetterTriggerListener;
import org.kohsuke.stapler.lang.FieldRef;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class StaplerFilteredActionListener implements FilteredDoActionTriggerListener, FilteredGetterTriggerListener, FilteredFieldTriggerListener, FilteredDispatchTriggerListener {
  private static final Logger LOGGER = Logger.getLogger(StaplerFilteredActionListener.class.getName());
  
  private static final String LOG_MESSAGE = "New Stapler routing rules result in the URL \"{0}\" no longer being allowed. If you consider it safe to use, add the following to the whitelist: \"{1}\". Learn more: https://www.jenkins.io/redirect/stapler-routing";
  
  public boolean onDoActionTrigger(Function f, StaplerRequest req, StaplerResponse rsp, Object node) {
    LOGGER.log(Level.WARNING, "New Stapler routing rules result in the URL \"{0}\" no longer being allowed. If you consider it safe to use, add the following to the whitelist: \"{1}\". Learn more: https://www.jenkins.io/redirect/stapler-routing", new Object[] { req
          .getPathInfo(), f
          .getSignature() });
    return false;
  }
  
  public boolean onGetterTrigger(Function f, StaplerRequest req, StaplerResponse rsp, Object node, String expression) {
    LOGGER.log(Level.WARNING, "New Stapler routing rules result in the URL \"{0}\" no longer being allowed. If you consider it safe to use, add the following to the whitelist: \"{1}\". Learn more: https://www.jenkins.io/redirect/stapler-routing", new Object[] { req
          .getPathInfo(), f
          .getSignature() });
    return false;
  }
  
  public boolean onFieldTrigger(FieldRef f, StaplerRequest req, StaplerResponse staplerResponse, Object node, String expression) {
    LOGGER.log(Level.WARNING, "New Stapler routing rules result in the URL \"{0}\" no longer being allowed. If you consider it safe to use, add the following to the whitelist: \"{1}\". Learn more: https://www.jenkins.io/redirect/stapler-routing", new Object[] { req
          .getPathInfo(), f
          .getSignature() });
    return false;
  }
  
  public boolean onDispatchTrigger(StaplerRequest req, StaplerResponse rsp, Object node, String viewName) {
    LOGGER.warning(() -> "New Stapler dispatch rules result in the URL \"" + req.getPathInfo() + "\" no longer being allowed. If you consider it safe to use, add the following to the whitelist: \"" + node
        .getClass().getName() + " " + viewName + "\". Learn more: https://www.jenkins.io/redirect/stapler-facet-restrictions");
    return false;
  }
}
