package hudson.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public abstract class FileVisitor {
  public abstract void visit(File paramFile, String paramString) throws IOException;
  
  public void visitSymlink(File link, String target, String relativePath) throws IOException { visit(link, relativePath); }
  
  public boolean understandsSymlink() { return false; }
  
  public final FileVisitor with(FileFilter f) {
    if (f == null)
      return this; 
    return new FilterFileVisitor(f, this);
  }
}
