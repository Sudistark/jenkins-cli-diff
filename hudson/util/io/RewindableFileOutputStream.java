package hudson.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import org.apache.commons.io.FileUtils;

public class RewindableFileOutputStream extends OutputStream {
  protected final File out;
  
  private boolean closed;
  
  private OutputStream current;
  
  public RewindableFileOutputStream(File out) { this.out = out; }
  
  private OutputStream current() throws IOException {
    if (this.current == null)
      if (!this.closed) {
        FileUtils.forceMkdir(this.out.getParentFile());
        try {
          this.current = Files.newOutputStream(this.out.toPath(), new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING });
        } catch (FileNotFoundException|java.nio.file.NoSuchFileException|java.nio.file.InvalidPathException e) {
          throw new IOException("Failed to open " + this.out, e);
        } 
      } else {
        throw new IOException(this.out.getName() + " stream is closed");
      }  
    return this.current;
  }
  
  public void write(int b) throws IOException { current().write(b); }
  
  public void write(byte[] b) throws IOException { current().write(b); }
  
  public void write(byte[] b, int off, int len) throws IOException { current().write(b, off, len); }
  
  public void flush() throws IOException { current().flush(); }
  
  public void close() throws IOException {
    closeCurrent();
    this.closed = true;
  }
  
  public void rewind() throws IOException { closeCurrent(); }
  
  private void closeCurrent() throws IOException {
    if (this.current != null) {
      this.current.close();
      this.current = null;
    } 
  }
}
