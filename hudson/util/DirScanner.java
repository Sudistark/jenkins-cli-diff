package hudson.util;

import hudson.Util;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public abstract class DirScanner implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public abstract void scan(File paramFile, FileVisitor paramFileVisitor) throws IOException;
  
  protected final void scanSingle(File f, String relative, FileVisitor visitor) throws IOException {
    if (visitor.understandsSymlink()) {
      String target;
      try {
        target = Util.resolveSymlink(f);
      } catch (IOException x) {
        target = null;
      } 
      if (target != null) {
        visitor.visitSymlink(f, target, relative);
        return;
      } 
    } 
    visitor.visit(f, relative);
  }
}
