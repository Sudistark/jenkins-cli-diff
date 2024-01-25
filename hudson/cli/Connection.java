package hudson.cli;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.remoting.ClassFilter;
import hudson.remoting.ObjectInputStreamEx;
import hudson.remoting.SocketChannelStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.AlgorithmParameterGenerator;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.IvParameterSpec;
import org.jenkinsci.remoting.util.AnonymousClassWarnings;

@Deprecated
public class Connection {
  public final InputStream in;
  
  public final OutputStream out;
  
  public final DataInputStream din;
  
  public final DataOutputStream dout;
  
  public Connection(Socket socket) throws IOException { this(SocketChannelStream.in(socket), SocketChannelStream.out(socket)); }
  
  public Connection(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
    this.din = new DataInputStream(in);
    this.dout = new DataOutputStream(out);
  }
  
  public void writeUTF(String msg) throws IOException { this.dout.writeUTF(msg); }
  
  public String readUTF() throws IOException { return this.din.readUTF(); }
  
  public void writeBoolean(boolean b) throws IOException { this.dout.writeBoolean(b); }
  
  public boolean readBoolean() throws IOException { return this.din.readBoolean(); }
  
  public void writeObject(Object o) throws IOException {
    ObjectOutputStream oos = AnonymousClassWarnings.checkingObjectOutputStream(this.out);
    oos.writeObject(o);
  }
  
  @SuppressFBWarnings(value = {"OBJECT_DESERIALIZATION"}, justification = "Not used. We should just remove it. Class is deprecated.")
  public <T> T readObject() throws IOException, ClassNotFoundException {
    ObjectInputStreamEx objectInputStreamEx = new ObjectInputStreamEx(this.in, getClass().getClassLoader(), ClassFilter.DEFAULT);
    return (T)objectInputStreamEx.readObject();
  }
  
  public void writeKey(Key key) throws IOException { writeUTF(Base64.getEncoder().encodeToString(key.getEncoded())); }
  
  public X509EncodedKeySpec readKey() throws IOException {
    byte[] otherHalf = Base64.getDecoder().decode(readUTF());
    return new X509EncodedKeySpec(otherHalf);
  }
  
  public void writeByteArray(byte[] data) throws IOException {
    this.dout.writeInt(data.length);
    this.dout.write(data);
  }
  
  public byte[] readByteArray() throws IOException {
    int bufSize = this.din.readInt();
    if (bufSize < 0)
      throw new IOException("DataInputStream unexpectedly returned negative integer"); 
    byte[] buf = new byte[bufSize];
    this.din.readFully(buf);
    return buf;
  }
  
  public KeyAgreement diffieHellman(boolean side) throws IOException, GeneralSecurityException { return diffieHellman(side, 512); }
  
  public KeyAgreement diffieHellman(boolean side, int keySize) throws IOException, GeneralSecurityException {
    PublicKey otherHalf;
    KeyPair keyPair;
    if (side) {
      AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
      paramGen.init(keySize);
      KeyPairGenerator dh = KeyPairGenerator.getInstance("DH");
      dh.initialize(paramGen.generateParameters().getParameterSpec(javax.crypto.spec.DHParameterSpec.class));
      keyPair = dh.generateKeyPair();
      writeKey(keyPair.getPublic());
      otherHalf = KeyFactory.getInstance("DH").generatePublic(readKey());
    } else {
      otherHalf = KeyFactory.getInstance("DH").generatePublic(readKey());
      KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
      keyPairGen.initialize(((DHPublicKey)otherHalf).getParams());
      keyPair = keyPairGen.generateKeyPair();
      writeKey(keyPair.getPublic());
    } 
    KeyAgreement ka = KeyAgreement.getInstance("DH");
    ka.init(keyPair.getPrivate());
    ka.doPhase(otherHalf, true);
    return ka;
  }
  
  public Connection encryptConnection(SecretKey sessionKey, String algorithm) throws IOException, GeneralSecurityException {
    Cipher cout = Cipher.getInstance(algorithm);
    cout.init(1, sessionKey, createIv(sessionKey));
    CipherOutputStream o = new CipherOutputStream(this.out, cout);
    Cipher cin = Cipher.getInstance(algorithm);
    cin.init(2, sessionKey, createIv(sessionKey));
    CipherInputStream i = new CipherInputStream(this.in, cin);
    return new Connection(i, o);
  }
  
  private IvParameterSpec createIv(SecretKey sessionKey) { return new IvParameterSpec(sessionKey.getEncoded()); }
  
  public static byte[] fold(byte[] bytes, int size) {
    byte[] r = new byte[size];
    for (int i = Math.max(bytes.length, size) - 1; i >= 0; i--)
      r[i % r.length] = (byte)(r[i % r.length] ^ bytes[i % bytes.length]); 
    return r;
  }
  
  private String detectKeyAlgorithm(KeyPair kp) { return detectKeyAlgorithm(kp.getPublic()); }
  
  private String detectKeyAlgorithm(PublicKey kp) {
    if (kp instanceof java.security.interfaces.RSAPublicKey)
      return "RSA"; 
    if (kp instanceof java.security.interfaces.DSAPublicKey)
      return "DSA"; 
    throw new IllegalArgumentException("Unknown public key type: " + kp);
  }
  
  public void proveIdentity(byte[] sharedSecret, KeyPair key) throws IOException, GeneralSecurityException {
    String algorithm = detectKeyAlgorithm(key);
    writeUTF(algorithm);
    writeKey(key.getPublic());
    Signature sig = Signature.getInstance("SHA1with" + algorithm);
    sig.initSign(key.getPrivate());
    sig.update(key.getPublic().getEncoded());
    sig.update(sharedSecret);
    writeObject(sig.sign());
  }
  
  public PublicKey verifyIdentity(byte[] sharedSecret) throws IOException, GeneralSecurityException {
    try {
      String serverKeyAlgorithm = readUTF();
      PublicKey spk = KeyFactory.getInstance(serverKeyAlgorithm).generatePublic(readKey());
      Signature sig = Signature.getInstance("SHA1with" + serverKeyAlgorithm);
      sig.initVerify(spk);
      sig.update(spk.getEncoded());
      sig.update(sharedSecret);
      sig.verify((byte[])readObject());
      return spk;
    } catch (ClassNotFoundException e) {
      throw new AssertionError(e);
    } 
  }
  
  public void close() throws IOException {
    this.in.close();
    this.out.close();
  }
}
