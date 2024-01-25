package jenkins.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.ChunkedInputStream;
import hudson.util.ChunkedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class FullDuplexHttpService {
  private static final Logger LOGGER = Logger.getLogger(FullDuplexHttpService.class.getName());
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean DIY_CHUNKING = SystemProperties.getBoolean("hudson.diyChunking");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(15L);
  
  protected final UUID uuid;
  
  private InputStream upload;
  
  private boolean completed;
  
  protected FullDuplexHttpService(UUID uuid) { this.uuid = uuid; }
  
  public void download(StaplerRequest req, StaplerResponse rsp) throws InterruptedException, IOException {
    rsp.setStatus(200);
    rsp.addHeader("Transfer-Encoding", "chunked");
    ChunkedOutputStream chunkedOutputStream = rsp.getOutputStream();
    if (DIY_CHUNKING)
      chunkedOutputStream = new ChunkedOutputStream(chunkedOutputStream); 
    chunkedOutputStream.write(0);
    chunkedOutputStream.flush();
    long end = System.currentTimeMillis() + CONNECTION_TIMEOUT;
    while (this.upload == null && System.currentTimeMillis() < end) {
      LOGGER.log(Level.FINE, "Waiting for upload stream for {0}: {1}", new Object[] { this.uuid, this });
      wait(1000L);
    } 
    if (this.upload == null)
      throw new IOException("HTTP full-duplex channel timeout: " + this.uuid); 
    LOGGER.log(Level.FINE, "Received upload stream {0} for {1}: {2}", new Object[] { this.upload, this.uuid, this });
    try {
      run(this.upload, chunkedOutputStream);
    } finally {
      this.completed = true;
      notify();
    } 
  }
  
  protected abstract void run(InputStream paramInputStream, OutputStream paramOutputStream) throws IOException, InterruptedException;
  
  public void upload(StaplerRequest req, StaplerResponse rsp) throws InterruptedException, IOException {
    rsp.setStatus(200);
    ChunkedInputStream chunkedInputStream = req.getInputStream();
    if (DIY_CHUNKING)
      chunkedInputStream = new ChunkedInputStream(chunkedInputStream); 
    this.upload = chunkedInputStream;
    LOGGER.log(Level.FINE, "Recording upload stream {0} for {1}: {2}", new Object[] { this.upload, this.uuid, this });
    notify();
    while (!this.completed)
      wait(); 
  }
}
