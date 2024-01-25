package hudson.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

@Deprecated
public class ReopenableFileOutputStream extends OutputStream {
  protected final File out;
  
  private OutputStream current;
  
  private boolean appendOnNextOpen;
  
  public ReopenableFileOutputStream(File out) {
    this.appendOnNextOpen = false;
    this.out = out;
  }
  
  private OutputStream current() throws IOException {
    if (this.current == null)
      try {
        this.current = Files.newOutputStream(this.out.toPath(), new OpenOption[] { StandardOpenOption.CREATE, 
              this.appendOnNextOpen ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING });
      } catch (FileNotFoundException|java.nio.file.NoSuchFileException|java.nio.file.InvalidPathException e) {
        throw new IOException("Failed to open " + this.out, e);
      }  
    return this.current;
  }
  
  public void write(int b) throws IOException { current().write(b); }
  
  public void write(byte[] b) throws IOException { current().write(b); }
  
  public void write(byte[] b, int off, int len) throws IOException { current().write(b, off, len); }
  
  public void flush() throws IOException { current().flush(); }
  
  public void close() throws IOException {
    if (this.current != null) {
      this.current.close();
      this.appendOnNextOpen = true;
      this.current = null;
    } 
  }
  
  public void rewind() throws IOException {
    close();
    this.appendOnNextOpen = false;
  }
}
