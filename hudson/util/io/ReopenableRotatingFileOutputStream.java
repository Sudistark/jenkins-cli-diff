package hudson.util.io;

import hudson.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class ReopenableRotatingFileOutputStream extends ReopenableFileOutputStream {
  private static final Logger LOGGER = Logger.getLogger(ReopenableRotatingFileOutputStream.class.getName());
  
  private final int size;
  
  public ReopenableRotatingFileOutputStream(File out, int size) {
    super(out);
    this.size = size;
  }
  
  protected File getNumberedFileName(int n) {
    if (n == 0)
      return this.out; 
    return new File(this.out.getPath() + "." + this.out.getPath());
  }
  
  public void rewind() throws IOException {
    super.rewind();
    for (int i = this.size - 1; i >= 0; i--) {
      File fi = getNumberedFileName(i);
      if (Files.exists(Util.fileToPath(fi), new java.nio.file.LinkOption[0])) {
        File next = getNumberedFileName(i + 1);
        Files.move(Util.fileToPath(fi), Util.fileToPath(next), new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
      } 
    } 
  }
  
  public void deleteAll() throws IOException {
    for (int i = 0; i <= this.size; i++) {
      try {
        Files.deleteIfExists(getNumberedFileName(i).toPath());
      } catch (IOException|java.nio.file.InvalidPathException e) {
        LOGGER.log(Level.WARNING, null, e);
      } 
    } 
  }
}
