package jenkins.security;

import hudson.Extension;
import hudson.Functions;
import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.TaskListener;
import hudson.util.HttpResponses;
import hudson.util.SecretRewriter;
import hudson.util.VersionNumber;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.management.AsynchronousAdministrativeMonitor;
import jenkins.model.Jenkins;
import jenkins.util.io.FileBoolean;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol({"rekeySecret"})
public class RekeySecretAdminMonitor extends AsynchronousAdministrativeMonitor {
  private final FileBoolean needed = state("needed");
  
  private final FileBoolean done = state("done");
  
  private final FileBoolean scanOnBoot = state("scanOnBoot");
  
  public RekeySecretAdminMonitor() throws IOException {
    Jenkins j = Jenkins.get();
    if (j.isUpgradedFromBefore(new VersionNumber("1.496.*")) && (new FileBoolean(new File(j
          .getRootDir(), "secret.key.not-so-secret"))).isOff())
      this.needed.on(); 
    Util.deleteRecursive(new File(getBaseDir(), "backups"));
  }
  
  public boolean isActivated() { return this.needed.isOn(); }
  
  public boolean isDone() { return this.done.isOn(); }
  
  public void setNeeded() throws IOException { this.needed.on(); }
  
  public boolean isScanOnBoot() { return this.scanOnBoot.isOn(); }
  
  public boolean isSecurity() { return true; }
  
  @RequirePOST
  public HttpResponse doScan(StaplerRequest req) throws IOException, GeneralSecurityException {
    if (req.hasParameter("background")) {
      start(false);
    } else if (req.hasParameter("schedule")) {
      this.scanOnBoot.on();
    } else if (req.hasParameter("dismiss")) {
      disable(true);
    } else {
      throw HttpResponses.error(400, "Invalid request submission: " + req.getParameterMap());
    } 
    return HttpResponses.redirectViaContextPath("/manage");
  }
  
  private FileBoolean state(String name) { return new FileBoolean(new File(getBaseDir(), name)); }
  
  @Initializer(fatal = false, after = InitMilestone.PLUGINS_STARTED, before = InitMilestone.EXTENSIONS_AUGMENTED)
  public void scanOnReboot() throws IOException {
    FileBoolean flag = this.scanOnBoot;
    if (flag.isOn()) {
      flag.off();
      start(false).join();
    } 
  }
  
  public String getDisplayName() { return Messages.RekeySecretAdminMonitor_DisplayName(); }
  
  protected File getLogFile() { return new File(getBaseDir(), "rekey.log"); }
  
  protected void fix(TaskListener listener) throws Exception {
    LOGGER.info("Initiating a re-keying of secrets. See " + getLogFile());
    SecretRewriter rewriter = new SecretRewriter();
    try {
      PrintStream log = listener.getLogger();
      log.println("Started re-keying " + new Date());
      int count = rewriter.rewriteRecursive(Jenkins.get().getRootDir(), listener);
      log.printf("Completed re-keying %d files on %s%n", new Object[] { Integer.valueOf(count), new Date() });
      (new RekeySecretAdminMonitor()).done.on();
      LOGGER.info("Secret re-keying completed");
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Fatal failure in re-keying secrets", e);
      Functions.printStackTrace(e, listener.error("Fatal failure in rewriting secrets"));
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(RekeySecretAdminMonitor.class.getName());
}
