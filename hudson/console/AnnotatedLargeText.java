package hudson.console;

import com.jcraft.jzlib.GZIPInputStream;
import com.jcraft.jzlib.GZIPOutputStream;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.remoting.ObjectInputStreamEx;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import jenkins.model.Jenkins;
import jenkins.security.CryptoConfidentialKey;
import org.jenkinsci.remoting.util.AnonymousClassWarnings;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.io.ByteBuffer;
import org.kohsuke.stapler.framework.io.LargeText;

public class AnnotatedLargeText<T> extends LargeText {
  private T context;
  
  public AnnotatedLargeText(File file, Charset charset, boolean completed, T context) {
    super(file, charset, completed, true);
    this.context = context;
  }
  
  public AnnotatedLargeText(ByteBuffer memory, Charset charset, boolean completed, T context) {
    super(memory, charset, completed);
    this.context = context;
  }
  
  public void doProgressiveHtml(StaplerRequest req, StaplerResponse rsp) throws IOException {
    req.setAttribute("html", Boolean.valueOf(true));
    doProgressText(req, rsp);
  }
  
  public void doProgressiveText(StaplerRequest req, StaplerResponse rsp) throws IOException { doProgressText(req, rsp); }
  
  private boolean isHtml() {
    StaplerRequest req = Stapler.getCurrentRequest();
    return (req != null && req.getAttribute("html") != null);
  }
  
  protected void setContentType(StaplerResponse rsp) { rsp.setContentType(isHtml() ? "text/html;charset=UTF-8" : "text/plain;charset=UTF-8"); }
  
  private ConsoleAnnotator<T> createAnnotator(StaplerRequest req) throws IOException {
    try {
      String base64 = (req != null) ? req.getHeader("X-ConsoleAnnotator") : null;
      if (base64 != null) {
        Cipher sym = PASSING_ANNOTATOR.decrypt();
        try {
          ObjectInputStreamEx objectInputStreamEx = new ObjectInputStreamEx(new GZIPInputStream(new CipherInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8))), sym)), (Jenkins.get()).pluginManager.uberClassLoader);
          try {
            long timestamp = objectInputStreamEx.readLong();
            if (TimeUnit.HOURS.toMillis(1L) > Math.abs(System.currentTimeMillis() - timestamp)) {
              ConsoleAnnotator consoleAnnotator = getConsoleAnnotator(objectInputStreamEx);
              objectInputStreamEx.close();
              return consoleAnnotator;
            } 
            objectInputStreamEx.close();
          } catch (Throwable throwable) {
            try {
              objectInputStreamEx.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            } 
            throw throwable;
          } 
        } catch (RuntimeException ex) {
          throw new IOException("Could not decode input", ex);
        } 
      } 
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    } 
    return ConsoleAnnotator.initial(this.context);
  }
  
  @SuppressFBWarnings(value = {"OBJECT_DESERIALIZATION"}, justification = "Deserialization is protected by logic.")
  private ConsoleAnnotator getConsoleAnnotator(ObjectInputStream ois) throws IOException, ClassNotFoundException { return (ConsoleAnnotator)ois.readObject(); }
  
  @CheckReturnValue
  public long writeLogTo(long start, Writer w) throws IOException {
    if (isHtml())
      return writeHtmlTo(start, w); 
    return super.writeLogTo(start, w);
  }
  
  @CheckReturnValue
  public long writeLogTo(long start, OutputStream out) throws IOException { return super.writeLogTo(start, new PlainTextConsoleOutputStream(out)); }
  
  @CheckReturnValue
  public long writeRawLogTo(long start, OutputStream out) throws IOException { return super.writeLogTo(start, out); }
  
  @CheckReturnValue
  public long writeHtmlTo(long start, Writer w) throws IOException {
    ConsoleAnnotationOutputStream<T> caw = new ConsoleAnnotationOutputStream<T>(w, createAnnotator(Stapler.getCurrentRequest()), this.context, this.charset);
    long r = super.writeLogTo(start, caw);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    Cipher sym = PASSING_ANNOTATOR.encrypt();
    ObjectOutputStream oos = AnonymousClassWarnings.checkingObjectOutputStream(new GZIPOutputStream(new CipherOutputStream(baos, sym)));
    oos.writeLong(System.currentTimeMillis());
    oos.writeObject(caw.getConsoleAnnotator());
    oos.close();
    StaplerResponse rsp = Stapler.getCurrentResponse();
    if (rsp != null)
      rsp.setHeader("X-ConsoleAnnotator", Base64.getEncoder().encodeToString(baos.toByteArray())); 
    return r;
  }
  
  private static final CryptoConfidentialKey PASSING_ANNOTATOR = new CryptoConfidentialKey(AnnotatedLargeText.class, "consoleAnnotator");
}
