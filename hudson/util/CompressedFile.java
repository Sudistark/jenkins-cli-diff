package hudson.util;

import com.jcraft.jzlib.GZIPInputStream;
import hudson.Util;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CompressedFile {
  private final File file;
  
  private final File gz;
  
  public CompressedFile(File file) {
    this.file = file;
    this.gz = new File(file.getParentFile(), file.getName() + ".gz");
  }
  
  public OutputStream write() throws IOException {
    Files.deleteIfExists(Util.fileToPath(this.gz));
    return Files.newOutputStream(Util.fileToPath(this.file), new java.nio.file.OpenOption[0]);
  }
  
  public InputStream read() throws IOException {
    if (Files.exists(Util.fileToPath(this.file), new java.nio.file.LinkOption[0]))
      return Files.newInputStream(Util.fileToPath(this.file), new java.nio.file.OpenOption[0]); 
    if (Files.exists(Util.fileToPath(this.gz), new java.nio.file.LinkOption[0]))
      return new GZIPInputStream(Files.newInputStream(Util.fileToPath(this.gz), new java.nio.file.OpenOption[0])); 
    throw new FileNotFoundException(this.file.getName());
  }
  
  @Deprecated
  public String loadAsString() throws IOException {
    long sizeGuess;
    if (this.file.exists()) {
      sizeGuess = this.file.length();
    } else if (this.gz.exists()) {
      sizeGuess = this.gz.length() * 2L;
    } else {
      return "";
    } 
    StringBuilder str = new StringBuilder((int)sizeGuess);
    InputStream is = read();
    try {
      Reader r = new InputStreamReader(is, Charset.defaultCharset());
      try {
        char[] buf = new char[8192];
        int len;
        while ((len = r.read(buf, 0, buf.length)) > 0)
          str.append(buf, 0, len); 
        r.close();
      } catch (Throwable throwable) {
        try {
          r.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
      if (is != null)
        is.close(); 
    } catch (Throwable throwable) {
      if (is != null)
        try {
          is.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    return str.toString();
  }
  
  public void compress() { compressionThread.submit(new Object(this)); }
  
  private static final ExecutorService compressionThread = new ThreadPoolExecutor(0, 1, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ExceptionCatchingThreadFactory(new NamingThreadFactory(new DaemonThreadFactory(), "CompressedFile")));
  
  private static final Logger LOGGER = Logger.getLogger(CompressedFile.class.getName());
}
