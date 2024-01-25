package jenkins.model.identity;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionPoint;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

public abstract class InstanceIdentityProvider<PUB extends PublicKey, PRIV extends PrivateKey> extends Object implements ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(InstanceIdentityProvider.class.getName());
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final KeyTypes<RSAPublicKey, RSAPrivateKey> RSA = new KeyTypes(RSAPublicKey.class, RSAPrivateKey.class);
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public static final KeyTypes<DSAPublicKey, DSAPrivateKey> DSA = new KeyTypes(DSAPublicKey.class, DSAPrivateKey.class);
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public static final KeyTypes<ECPublicKey, ECPrivateKey> EC = new KeyTypes(ECPublicKey.class, ECPrivateKey.class);
  
  @CheckForNull
  protected abstract KeyPair getKeyPair();
  
  @CheckForNull
  protected PUB getPublicKey() {
    KeyPair keyPair = getKeyPair();
    return (PUB)((keyPair == null) ? null : keyPair.getPublic());
  }
  
  @CheckForNull
  protected PRIV getPrivateKey() {
    KeyPair keyPair = getKeyPair();
    return (PRIV)((keyPair == null) ? null : keyPair.getPrivate());
  }
  
  @CheckForNull
  protected abstract X509Certificate getCertificate();
}
