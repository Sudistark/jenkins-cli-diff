package hudson.util;

import hudson.Functions;
import hudson.Util;
import hudson.model.TaskListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class SecretRewriter {
  private Set<String> callstack = new HashSet();
  
  private final Cipher cipher = Secret.getCipher("AES");
  
  private final SecretKey key = HistoricalSecrets.getLegacyKey();
  
  public SecretRewriter() throws GeneralSecurityException {}
  
  @Deprecated
  public SecretRewriter(File backupDirectory) throws GeneralSecurityException { this(); }
  
  private String tryRewrite(String s) throws InvalidKeyException {
    byte[] in;
    if (s.length() < 24)
      return s; 
    if (!isBase64(s))
      return s; 
    try {
      in = Base64.getDecoder().decode(s.getBytes(StandardCharsets.UTF_8));
    } catch (IllegalArgumentException e) {
      return s;
    } 
    this.cipher.init(2, this.key);
    Secret sec = HistoricalSecrets.tryDecrypt(this.cipher, in);
    if (sec != null)
      return sec.getEncryptedValue(); 
    return s;
  }
  
  @Deprecated
  public boolean rewrite(File f, File backup) throws InvalidKeyException, IOException { return rewrite(f); }
  
  public boolean rewrite(File f) throws InvalidKeyException, IOException {
    w = new AtomicFileWriter(f.toPath(), StandardCharsets.UTF_8);
    try {
      boolean modified = false;
      PrintWriter out = new PrintWriter(new BufferedWriter(w));
      try {
        InputStream fin = Files.newInputStream(Util.fileToPath(f), new java.nio.file.OpenOption[0]);
        try {
          BufferedReader r = new BufferedReader(new InputStreamReader(fin, StandardCharsets.UTF_8));
          try {
            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
              int copied = 0;
              buf.setLength(0);
              while (true) {
                int sidx = line.indexOf('>', copied);
                if (sidx < 0)
                  break; 
                int eidx = line.indexOf('<', sidx);
                if (eidx < 0)
                  break; 
                String elementText = line.substring(sidx + 1, eidx);
                String replacement = tryRewrite(elementText);
                if (!replacement.equals(elementText))
                  modified = true; 
                buf.append(line, copied, sidx + 1);
                buf.append(replacement);
                copied = eidx;
              } 
              buf.append(line.substring(copied));
              out.println(buf);
            } 
            r.close();
          } catch (Throwable throwable) {
            try {
              r.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            } 
            throw throwable;
          } 
          if (fin != null)
            fin.close(); 
        } catch (Throwable throwable) {
          if (fin != null)
            try {
              fin.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
        out.close();
      } catch (Throwable throwable) {
        try {
          out.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
      if (modified)
        w.commit(); 
      return modified;
    } finally {
      w.abort();
    } 
  }
  
  public int rewriteRecursive(File dir, TaskListener listener) throws InvalidKeyException { return rewriteRecursive(dir, "", listener); }
  
  private int rewriteRecursive(File dir, String relative, TaskListener listener) throws InvalidKeyException {
    try {
      canonical = dir.toPath().toRealPath(new java.nio.file.LinkOption[0]).toString();
    } catch (IOException|java.nio.file.InvalidPathException e) {
      canonical = dir.getAbsolutePath();
    } 
    if (!this.callstack.add(canonical)) {
      listener.getLogger().println("Cycle detected: " + dir);
      return 0;
    } 
    try {
      File[] children = dir.listFiles();
      if (children == null)
        return 0; 
      int rewritten = 0;
      for (File child : children) {
        String cn = child.getName();
        if (cn.endsWith(".xml")) {
          if (this.count++ % 100 == 0)
            listener.getLogger().println("Scanning " + child); 
          try {
            if (rewrite(child)) {
              listener.getLogger().println("Rewritten " + child);
              rewritten++;
            } 
          } catch (IOException e) {
            Functions.printStackTrace(e, listener.error("Failed to rewrite " + child));
          } 
        } 
        if (child.isDirectory() && 
          !isIgnoredDir(child))
          rewritten += rewriteRecursive(child, 
              relative.isEmpty() ? cn : (relative + "/" + relative), listener); 
      } 
      return rewritten;
    } finally {
      this.callstack.remove(canonical);
    } 
  }
  
  protected boolean isIgnoredDir(File dir) throws InvalidKeyException, IOException {
    String n = dir.getName();
    return (n.equals("workspace") || n.equals("artifacts") || n
      .equals("plugins") || n
      .equals(".") || n.equals(".."));
  }
  
  private static boolean isBase64(char ch) { return (ch < '' && IS_BASE64[ch]); }
  
  private static boolean isBase64(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (!isBase64(s.charAt(i)))
        return false; 
    } 
    return true;
  }
  
  private static final boolean[] IS_BASE64 = new boolean[128];
  
  private int count;
  
  static  {
    chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    for (int i = 0; i < chars.length(); i++)
      IS_BASE64[chars.charAt(i)] = true; 
  }
}
