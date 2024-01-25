package jenkins.security.stapler;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import jenkins.YesNoMaybe;
import jenkins.util.SystemProperties;
import org.apache.commons.io.IOUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.CancelRequestHandlingException;
import org.kohsuke.stapler.DispatchValidator;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class StaplerDispatchValidator implements DispatchValidator {
  private static final Logger LOGGER = Logger.getLogger(StaplerDispatchValidator.class.getName());
  
  private static final String ATTRIBUTE_NAME = StaplerDispatchValidator.class.getName() + ".status";
  
  private static final String ESCAPE_HATCH = StaplerDispatchValidator.class.getName() + ".disabled";
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean DISABLED = SystemProperties.getBoolean(ESCAPE_HATCH);
  
  private final ValidatorCache cache;
  
  @NonNull
  private static YesNoMaybe setStatus(@NonNull StaplerRequest req, @NonNull YesNoMaybe status) {
    switch (null.$SwitchMap$jenkins$YesNoMaybe[status.ordinal()]) {
      case 1:
      case 2:
        LOGGER.fine(() -> "Request dispatch set status to " + status.toBool() + " for URL " + req.getPathInfo());
        req.setAttribute(ATTRIBUTE_NAME, status.toBool());
        return status;
      case 3:
        return status;
    } 
    throw new IllegalStateException("Unexpected value: " + status);
  }
  
  @NonNull
  private static YesNoMaybe computeStatusIfNull(@NonNull StaplerRequest req, @NonNull Supplier<YesNoMaybe> statusIfNull) {
    Object requestStatus = req.getAttribute(ATTRIBUTE_NAME);
    if (requestStatus instanceof Boolean)
      return ((Boolean)requestStatus).booleanValue() ? YesNoMaybe.YES : YesNoMaybe.NO; 
    return setStatus(req, (YesNoMaybe)statusIfNull.get());
  }
  
  public StaplerDispatchValidator() {
    this.cache = new ValidatorCache();
    this.cache.load();
  }
  
  @CheckForNull
  public Boolean isDispatchAllowed(@NonNull StaplerRequest req, @NonNull StaplerResponse rsp) {
    if (DISABLED)
      return Boolean.valueOf(true); 
    YesNoMaybe status = computeStatusIfNull(req, () -> {
          if (rsp.getContentType() != null)
            return YesNoMaybe.YES; 
          if (rsp.getStatus() >= 300)
            return YesNoMaybe.YES; 
          return YesNoMaybe.MAYBE;
        });
    LOGGER.finer(() -> req.getRequestURI() + " -> " + req.getRequestURI());
    return status.toBool();
  }
  
  @CheckForNull
  public Boolean isDispatchAllowed(@NonNull StaplerRequest req, @NonNull StaplerResponse rsp, @NonNull String viewName, @CheckForNull Object node) {
    if (DISABLED)
      return Boolean.valueOf(true); 
    YesNoMaybe status = computeStatusIfNull(req, () -> {
          if (viewName.equals("index"))
            return YesNoMaybe.YES; 
          if (node == null)
            return YesNoMaybe.MAYBE; 
          return this.cache.find(node.getClass()).isViewValid(viewName);
        });
    LOGGER.finer(() -> "<" + req.getRequestURI() + ", " + viewName + ", " + node + "> -> " + status.toBool());
    return status.toBool();
  }
  
  public void allowDispatch(@NonNull StaplerRequest req, @NonNull StaplerResponse rsp) {
    if (DISABLED)
      return; 
    setStatus(req, YesNoMaybe.YES);
  }
  
  public void requireDispatchAllowed(@NonNull StaplerRequest req, @NonNull StaplerResponse rsp) {
    if (DISABLED)
      return; 
    Boolean status = isDispatchAllowed(req, rsp);
    if (status == null || !status.booleanValue()) {
      LOGGER.fine(() -> "Cancelling dispatch for " + req.getRequestURI());
      throw new CancelRequestHandlingException();
    } 
  }
  
  @VisibleForTesting
  static StaplerDispatchValidator getInstance(@NonNull ServletContext context) { return (StaplerDispatchValidator)WebApp.get(context).getDispatchValidator(); }
  
  @VisibleForTesting
  void loadWhitelist(@NonNull InputStream in) throws IOException { this.cache.loadWhitelist(IOUtils.readLines(in, StandardCharsets.UTF_8)); }
}
