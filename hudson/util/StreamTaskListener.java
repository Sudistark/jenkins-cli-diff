package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.CloseProofOutputStream;
import hudson.model.TaskListener;
import hudson.remoting.RemoteOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;
import org.kohsuke.stapler.framework.io.WriterOutputStream;

public class StreamTaskListener extends AbstractTaskListener implements TaskListener, Closeable {
  @NonNull
  private PrintStream out;
  
  @CheckForNull
  private Charset charset;
  
  @Deprecated
  public StreamTaskListener(@NonNull PrintStream out) { this(out, null); }
  
  @Deprecated
  public StreamTaskListener(@NonNull OutputStream out) { this(out, null); }
  
  public StreamTaskListener(@NonNull OutputStream out, @CheckForNull Charset charset) {
    if (charset == null) {
      this.out = (out instanceof PrintStream) ? (PrintStream)out : new PrintStream(out, false, Charset.defaultCharset());
    } else {
      this.out = new PrintStream(out, false, charset);
    } 
    this.charset = charset;
  }
  
  @Deprecated
  public StreamTaskListener(@NonNull File out) throws IOException { this(out, null); }
  
  public StreamTaskListener(@NonNull File out, @CheckForNull Charset charset) throws IOException { this(Files.newOutputStream(asPath(out), new OpenOption[0]), charset); }
  
  private static Path asPath(@NonNull File out) throws IOException {
    try {
      return out.toPath();
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  public StreamTaskListener(@NonNull File out, boolean append, @CheckForNull Charset charset) throws IOException { this(Files.newOutputStream(
          asPath(out), new OpenOption[] { StandardOpenOption.CREATE, 
            append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING }), charset); }
  
  public StreamTaskListener(@NonNull Writer w) throws IOException { this(new WriterOutputStream(w)); }
  
  @Deprecated
  public StreamTaskListener() throws IOException { this(OutputStream.nullOutputStream()); }
  
  public static StreamTaskListener fromStdout() { return new StreamTaskListener(System.out, Charset.defaultCharset()); }
  
  public static StreamTaskListener fromStderr() { return new StreamTaskListener(System.err, Charset.defaultCharset()); }
  
  public PrintStream getLogger() { return this.out; }
  
  public Charset getCharset() { return (this.charset != null) ? this.charset : Charset.defaultCharset(); }
  
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(new RemoteOutputStream(new CloseProofOutputStream(this.out)));
    out.writeObject((this.charset == null) ? null : this.charset.name());
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.log(Level.FINE, null, new Throwable("serializing here with AUTO_FLUSH=" + AUTO_FLUSH)); 
  }
  
  private static final String KEY_AUTO_FLUSH = StreamTaskListener.class.getName() + ".AUTO_FLUSH";
  
  private static boolean AUTO_FLUSH;
  
  private static final long serialVersionUID = 1L;
  
  private static final Logger LOGGER;
  
  static  {
    SystemProperties.allowOnAgent(KEY_AUTO_FLUSH);
    AUTO_FLUSH = SystemProperties.getBoolean(KEY_AUTO_FLUSH);
    LOGGER = Logger.getLogger(StreamTaskListener.class.getName());
  }
  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    OutputStream os = (OutputStream)in.readObject();
    String name = (String)in.readObject();
    this.out = new PrintStream(os, AUTO_FLUSH, (name != null) ? name : Charset.defaultCharset().name());
    this.charset = (name == null) ? null : Charset.forName(name);
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.log(Level.FINE, null, new Throwable("deserializing here with AUTO_FLUSH=" + AUTO_FLUSH)); 
  }
  
  public void close() throws IOException { this.out.close(); }
  
  public void closeQuietly() throws IOException {
    try {
      close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to close", e);
    } 
  }
}
