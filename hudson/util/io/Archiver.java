package hudson.util.io;

import hudson.util.FileVisitor;
import java.io.Closeable;

public abstract class Archiver extends FileVisitor implements Closeable {
  protected int entriesWritten = 0;
  
  public int countEntries() { return this.entriesWritten; }
}
