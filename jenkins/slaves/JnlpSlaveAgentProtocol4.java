package jenkins.slaves;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Computer;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import jenkins.AgentProtocol;
import jenkins.model.identity.InstanceIdentityProvider;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.engine.JnlpProtocol4Handler;
import org.jenkinsci.remoting.protocol.cert.PublicKeyMatchingX509ExtendedTrustManager;

@Extension
@Symbol({"jnlp4"})
public class JnlpSlaveAgentProtocol4 extends AgentProtocol {
  private static final Logger LOGGER = Logger.getLogger(JnlpSlaveAgentProtocol4.class.getName());
  
  private KeyStore keyStore;
  
  private JnlpProtocol4Handler handler;
  
  private void init() {
    SSLContext sslContext;
    KeyManagerFactory kmf;
    if (this.handler != null) {
      LOGGER.fine("already initialized");
      return;
    } 
    LOGGER.fine("initializing");
    X509Certificate identityCertificate = InstanceIdentityProvider.RSA.getCertificate();
    if (identityCertificate == null)
      throw new KeyStoreException("JENKINS-41987: no X509Certificate found; perhaps instance-identity plugin is not installed"); 
    RSAPrivateKey privateKey = (RSAPrivateKey)InstanceIdentityProvider.RSA.getPrivateKey();
    if (privateKey == null)
      throw new KeyStoreException("JENKINS-41987: no RSAPrivateKey found; perhaps instance-identity plugin is not installed"); 
    this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    char[] password = constructPassword();
    try {
      this.keyStore.load(null, password);
    } catch (IOException e) {
      throw new IllegalStateException("Specification says this should not happen as we are not doing I/O", kmf);
    } catch (NoSuchAlgorithmException|java.security.cert.CertificateException e) {
      throw new IllegalStateException("Specification says this should not happen as we are not loading keys", kmf);
    } 
    this.keyStore.setKeyEntry("jenkins", privateKey, password, new X509Certificate[] { identityCertificate });
    try {
      kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(this.keyStore, password);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Specification says the default algorithm should exist", e);
    } catch (UnrecoverableKeyException e) {
      throw new IllegalStateException("The key was just inserted with this exact password", e);
    } 
    PublicKeyMatchingX509ExtendedTrustManager publicKeyMatchingX509ExtendedTrustManager = new PublicKeyMatchingX509ExtendedTrustManager(false, true, new java.security.PublicKey[0]);
    TrustManager[] trustManagers = { publicKeyMatchingX509ExtendedTrustManager };
    try {
      sslContext = SSLContext.getInstance("TLS");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Java runtime specification requires support for TLS algorithm", e);
    } 
    sslContext.init(kmf.getKeyManagers(), trustManagers, null);
    IOHubProvider hub = (IOHubProvider)ExtensionList.lookupSingleton(IOHubProvider.class);
    this.handler = new JnlpProtocol4Handler(JnlpAgentReceiver.DATABASE, Computer.threadPoolForRemoting, hub.getHub(), sslContext, false, true);
  }
  
  private char[] constructPassword() { return "password".toCharArray(); }
  
  public boolean isOptIn() { return false; }
  
  public String getDisplayName() { return Messages.JnlpSlaveAgentProtocol4_displayName(); }
  
  public String getName() { return "JNLP4-connect"; }
  
  public void handle(Socket socket) throws IOException, InterruptedException {
    try {
      init();
    } catch (IOException x) {
      throw x;
    } catch (Exception x) {
      throw new IOException(x);
    } 
    try {
      X509Certificate certificate = (X509Certificate)this.keyStore.getCertificate("jenkins");
      if (certificate == null || certificate
        .getNotAfter().getTime() < System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L)) {
        LOGGER.log(Level.INFO, "Updating {0} TLS certificate to retain validity", getName());
        X509Certificate identityCertificate = InstanceIdentityProvider.RSA.getCertificate();
        RSAPrivateKey privateKey = (RSAPrivateKey)InstanceIdentityProvider.RSA.getPrivateKey();
        char[] password = constructPassword();
        this.keyStore.setKeyEntry("jenkins", privateKey, password, new X509Certificate[] { identityCertificate });
      } 
    } catch (KeyStoreException e) {
      LOGGER.log(Level.FINEST, "Ignored", e);
    } 
    this.handler.handle(socket, 
        Map.of("JnlpAgentProtocol.cookie", JnlpAgentReceiver.generateCookie()), 
        ExtensionList.lookup(JnlpAgentReceiver.class));
  }
}
