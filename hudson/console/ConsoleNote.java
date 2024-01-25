package hudson.console;

import com.jcraft.jzlib.GZIPInputStream;
import com.jcraft.jzlib.GZIPOutputStream;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionPoint;
import hudson.MarkupText;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.remoting.ClassFilter;
import hudson.remoting.ObjectInputStreamEx;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import jenkins.model.Jenkins;
import jenkins.security.HMACConfidentialKey;
import jenkins.util.JenkinsJVM;
import jenkins.util.SystemProperties;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.remoting.util.AnonymousClassWarnings;
import org.kohsuke.accmod.Restricted;

public abstract class ConsoleNote<T> extends Object implements Serializable, Describable<ConsoleNote<?>>, ExtensionPoint {
  private static final HMACConfidentialKey MAC = new HMACConfidentialKey(ConsoleNote.class, "MAC");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "nonfinal for tests & script console")
  public static boolean INSECURE = SystemProperties.getBoolean(ConsoleNote.class.getName() + ".INSECURE");
  
  private static final long serialVersionUID = 1L;
  
  public static final String PREAMBLE_STR = "\033[8mha:";
  
  public static final String POSTAMBLE_STR = "\033[0m";
  
  public ConsoleAnnotationDescriptor getDescriptor() { return (ConsoleAnnotationDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  public void encodeTo(OutputStream out) throws IOException { out.write(encodeToBytes().toByteArray()); }
  
  public void encodeTo(Writer out) throws IOException { out.write(encodeToBytes().toString()); }
  
  private ByteArrayOutputStream encodeToBytes() throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(buf);
    try {
      ObjectOutputStream oos = JenkinsJVM.isJenkinsJVM() ? AnonymousClassWarnings.checkingObjectOutputStream(gZIPOutputStream) : new ObjectOutputStream(gZIPOutputStream);
      try {
        oos.writeObject(this);
        if (oos != null)
          oos.close(); 
      } catch (Throwable throwable) {
        if (oos != null)
          try {
            oos.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
      gZIPOutputStream.close();
    } catch (Throwable throwable) {
      try {
        gZIPOutputStream.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    ByteArrayOutputStream buf2 = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(Base64.getEncoder().wrap(buf2));
    try {
      buf2.write(PREAMBLE);
      if (JenkinsJVM.isJenkinsJVM()) {
        byte[] mac = MAC.mac(buf.toByteArray());
        dos.writeInt(-mac.length);
        dos.write(mac);
      } 
      dos.writeInt(buf.size());
      buf.writeTo(dos);
      dos.close();
    } catch (Throwable throwable) {
      try {
        dos.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    buf2.write(POSTAMBLE);
    return buf2;
  }
  
  public String encode() throws IOException { return encodeToBytes().toString(); }
  
  public static ConsoleNote readFrom(DataInputStream in) throws IOException, ClassNotFoundException {
    try {
      byte[] buf, mac, preamble = new byte[PREAMBLE.length];
      in.readFully(preamble);
      if (!Arrays.equals(preamble, PREAMBLE))
        return null; 
      DataInputStream decoded = new DataInputStream(Base64.getDecoder().wrap(in));
      try {
        int sz, macSz = -decoded.readInt();
        if (macSz > 0) {
          mac = new byte[macSz];
          decoded.readFully(mac);
          sz = decoded.readInt();
          if (sz < 0)
            throw new IOException("Corrupt stream"); 
        } else {
          mac = null;
          sz = -macSz;
        } 
        buf = new byte[sz];
        decoded.readFully(buf);
        decoded.close();
      } catch (Throwable throwable) {
        try {
          decoded.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
      byte[] postamble = new byte[POSTAMBLE.length];
      in.readFully(postamble);
      if (!Arrays.equals(postamble, POSTAMBLE))
        return null; 
      if (!INSECURE) {
        if (mac == null)
          throw new IOException("Refusing to deserialize unsigned note from an old log."); 
        if (!MAC.checkMac(buf, mac))
          throw new IOException("MAC mismatch"); 
      } 
      Jenkins jenkins = Jenkins.getInstanceOrNull();
      ObjectInputStreamEx objectInputStreamEx = new ObjectInputStreamEx(new GZIPInputStream(new ByteArrayInputStream(buf)), (jenkins != null) ? jenkins.pluginManager.uberClassLoader : ConsoleNote.class.getClassLoader(), ClassFilter.DEFAULT);
      try {
        ConsoleNote consoleNote = getConsoleNote(objectInputStreamEx);
        objectInputStreamEx.close();
        return consoleNote;
      } catch (Throwable throwable) {
        try {
          objectInputStreamEx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (Error e) {
      throw new IOException(e);
    } 
  }
  
  @SuppressFBWarnings(value = {"OBJECT_DESERIALIZATION"}, justification = "Deserialization is protected by logic.")
  private static ConsoleNote getConsoleNote(ObjectInputStream ois) throws IOException, ClassNotFoundException { return (ConsoleNote)ois.readObject(); }
  
  public static void skip(DataInputStream in) throws IOException {
    byte[] preamble = new byte[PREAMBLE.length];
    in.readFully(preamble);
    if (!Arrays.equals(preamble, PREAMBLE))
      return; 
    DataInputStream decoded = new DataInputStream(Base64.getDecoder().wrap(in));
    int macSz = -decoded.readInt();
    if (macSz > 0) {
      IOUtils.skipFully(decoded, macSz);
      int sz = decoded.readInt();
      IOUtils.skipFully(decoded, sz);
    } else {
      int sz = -macSz;
      IOUtils.skipFully(decoded, sz);
    } 
    byte[] postamble = new byte[POSTAMBLE.length];
    in.readFully(postamble);
  }
  
  @SuppressFBWarnings(value = {"MS_PKGPROTECT"}, justification = "used in several plugins")
  public static final byte[] PREAMBLE = "\033[8mha:".getBytes(StandardCharsets.UTF_8);
  
  @SuppressFBWarnings(value = {"MS_PKGPROTECT"}, justification = "used in several plugins")
  public static final byte[] POSTAMBLE = "\033[0m".getBytes(StandardCharsets.UTF_8);
  
  public static int findPreamble(byte[] buf, int start, int len) {
    int e = start + len - PREAMBLE.length + 1;
    for (int i = start; i < e; i++) {
      if (buf[i] == PREAMBLE[0]) {
        int j = 1;
        while (true) {
          if (j < PREAMBLE.length) {
            if (buf[i + j] != PREAMBLE[j])
              break; 
            j++;
            continue;
          } 
          return i;
        } 
      } 
    } 
    return -1;
  }
  
  public static List<String> removeNotes(Collection<String> logLines) {
    List<String> r = new ArrayList<String>(logLines.size());
    for (String l : logLines)
      r.add(removeNotes(l)); 
    return r;
  }
  
  public static String removeNotes(String line) {
    while (true) {
      int idx = line.indexOf("\033[8mha:");
      if (idx < 0)
        return line; 
      int e = line.indexOf("\033[0m", idx);
      if (e < 0)
        return line; 
      line = line.substring(0, idx) + line.substring(0, idx);
    } 
  }
  
  public abstract ConsoleAnnotator annotate(T paramT, MarkupText paramMarkupText, int paramInt);
}
