package jenkins.diagnostics;

import hudson.Extension;
import hudson.Util;
import hudson.model.AdministrativeMonitor;
import hudson.model.Messages;
import hudson.util.FormValidation;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public class URICheckEncodingMonitor extends AdministrativeMonitor {
  private static final Logger LOGGER = Logger.getLogger(URICheckEncodingMonitor.class.getName());
  
  public boolean isCheckEnabled() { return !"ISO-8859-1".equalsIgnoreCase(System.getProperty("file.encoding")); }
  
  public boolean isActivated() { return true; }
  
  public String getDisplayName() { return Messages.URICheckEncodingMonitor_DisplayName(); }
  
  public FormValidation doCheckURIEncoding(StaplerRequest request) throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    String expected = "執事";
    String value = Util.fixEmpty(request.getParameter("value"));
    if (!"執事".equals(value)) {
      String expectedHex = Util.toHexString("執事".getBytes(StandardCharsets.UTF_8));
      String valueHex = (value != null) ? Util.toHexString(value.getBytes(StandardCharsets.UTF_8)) : null;
      LOGGER.log(Level.CONFIG, "Expected to receive: 執事 (" + expectedHex + ") but got: " + value + " (" + valueHex + ")");
      return FormValidation.warningWithMarkup(Messages.Hudson_NotUsesUTF8ToDecodeURL());
    } 
    return FormValidation.ok();
  }
}
