package jenkins.slaves;

import hudson.Util;
import hudson.security.AccessControlled;
import hudson.security.Permission;
import hudson.slaves.SlaveComputer;
import hudson.util.Secret;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class EncryptedSlaveAgentJnlpFile implements HttpResponse {
  private static final Logger LOG = Logger.getLogger(EncryptedSlaveAgentJnlpFile.class.getName());
  
  private static final SecureRandom RANDOM = new SecureRandom();
  
  private final AccessControlled it;
  
  private final String viewName;
  
  private final String slaveName;
  
  private final Permission connectPermission;
  
  public EncryptedSlaveAgentJnlpFile(AccessControlled it, String viewName, String slaveName, Permission connectPermission) {
    this.it = it;
    this.viewName = viewName;
    this.connectPermission = connectPermission;
    this.slaveName = slaveName;
  }
  
  public void generateResponse(StaplerRequest req, StaplerResponse res, Object node) throws IOException, ServletException {
    RequestDispatcher view = req.getView(this.it, this.viewName);
    if ("true".equals(req.getParameter("encrypt"))) {
      byte[] encrypted, jnlpMac;
      CapturingServletOutputStream csos = new CapturingServletOutputStream();
      ResponseImpl responseImpl = new ResponseImpl(req.getStapler(), new Object(this, res, csos));
      view.forward(req, responseImpl);
      byte[] iv = new byte[16];
      RANDOM.nextBytes(iv);
      if (this.it instanceof SlaveComputer) {
        jnlpMac = Util.fromHexString(((SlaveComputer)this.it).getJnlpMac());
      } else {
        jnlpMac = JnlpAgentReceiver.SLAVE_SECRET.mac(this.slaveName.getBytes(StandardCharsets.UTF_8));
      } 
      SecretKey key = new SecretKeySpec(jnlpMac, 0, 16, "AES");
      try {
        Cipher c = Secret.getCipher("AES/CFB8/NoPadding");
        c.init(1, key, new IvParameterSpec(iv));
        encrypted = c.doFinal(csos.getBytes());
      } catch (GeneralSecurityException x) {
        throw new IOException(x);
      } 
      res.setContentType("application/octet-stream");
      res.getOutputStream().write(iv);
      res.getOutputStream().write(encrypted);
    } else {
      this.it.checkPermission(this.connectPermission);
      view.forward(req, res);
    } 
  }
}
