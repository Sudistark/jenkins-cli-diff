package jenkins.security;

import hudson.FilePath;
import hudson.Util;
import hudson.util.Secret;
import hudson.util.TextFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import jenkins.model.Jenkins;

public class DefaultConfidentialStore extends ConfidentialStore {
  private final SecureRandom sr;
  
  private final File rootDir;
  
  private final SecretKey masterKey;
  
  public DefaultConfidentialStore() throws IOException, InterruptedException { this(new File(Jenkins.get().getRootDir(), "secrets")); }
  
  public DefaultConfidentialStore(File rootDir) throws IOException, InterruptedException {
    this.sr = new SecureRandom();
    this.rootDir = rootDir;
    if (rootDir.mkdirs())
      (new FilePath(rootDir)).chmod(448); 
    TextFile masterSecret = new TextFile(new File(rootDir, "master.key"));
    if (!masterSecret.exists())
      masterSecret.write(Util.toHexString(randomBytes(128))); 
    this.masterKey = Util.toAes128Key(masterSecret.readTrim());
  }
  
  protected void store(ConfidentialKey key, byte[] payload) throws IOException {
    try {
      Cipher sym = Secret.getCipher("AES");
      sym.init(1, this.masterKey);
      OutputStream fos = Files.newOutputStream(getFileFor(key).toPath(), new java.nio.file.OpenOption[0]);
      try {
        CipherOutputStream cos = new CipherOutputStream(fos, sym);
        try {
          cos.write(payload);
          cos.write(MAGIC);
          cos.close();
        } catch (Throwable throwable) {
          try {
            cos.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
        if (fos != null)
          fos.close(); 
      } catch (Throwable throwable) {
        if (fos != null)
          try {
            fos.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (GeneralSecurityException e) {
      throw new IOException("Failed to persist the key: " + key.getId(), e);
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  protected byte[] load(ConfidentialKey key) throws IOException {
    try {
      File f = getFileFor(key);
      if (!f.exists())
        return null; 
      Cipher sym = Secret.getCipher("AES");
      sym.init(2, this.masterKey);
      InputStream fis = Files.newInputStream(f.toPath(), new java.nio.file.OpenOption[0]);
      try {
        CipherInputStream cis = new CipherInputStream(fis, sym);
        try {
          byte[] bytes = cis.readAllBytes();
          byte[] arrayOfByte = verifyMagic(bytes);
          cis.close();
          if (fis != null)
            fis.close(); 
          return arrayOfByte;
        } catch (Throwable throwable) {
          try {
            cis.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } catch (Throwable throwable) {
        if (fis != null)
          try {
            fis.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (GeneralSecurityException e) {
      throw new IOException("Failed to load the key: " + key.getId(), e);
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } catch (IOException x) {
      if (x.getCause() instanceof javax.crypto.BadPaddingException)
        return null; 
      throw x;
    } 
  }
  
  private byte[] verifyMagic(byte[] payload) {
    int payloadLen = payload.length - MAGIC.length;
    if (payloadLen < 0)
      return null; 
    for (int i = 0; i < MAGIC.length; i++) {
      if (payload[payloadLen + i] != MAGIC[i])
        return null; 
    } 
    byte[] truncated = new byte[payloadLen];
    System.arraycopy(payload, 0, truncated, 0, truncated.length);
    return truncated;
  }
  
  private File getFileFor(ConfidentialKey key) { return new File(this.rootDir, key.getId()); }
  
  SecureRandom secureRandom() { return this.sr; }
  
  public byte[] randomBytes(int size) {
    byte[] random = new byte[size];
    this.sr.nextBytes(random);
    return random;
  }
  
  private static final byte[] MAGIC = "::::MAGIC::::".getBytes(StandardCharsets.US_ASCII);
}
