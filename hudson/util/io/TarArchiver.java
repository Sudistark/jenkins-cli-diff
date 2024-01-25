package hudson.util.io;

import hudson.Functions;
import hudson.Util;
import hudson.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.BoundedInputStream;

final class TarArchiver extends Archiver {
  private final byte[] buf;
  
  private final TarArchiveOutputStream tar;
  
  TarArchiver(OutputStream out) {
    this.buf = new byte[8192];
    this.tar = new TarArchiveOutputStream(out);
    this.tar.setBigNumberMode(1);
    this.tar.setLongFileMode(2);
  }
  
  public void visitSymlink(File link, String target, String relativePath) throws IOException {
    TarArchiveEntry e = new TarArchiveEntry(relativePath, (byte)50);
    try {
      int mode = IOUtils.mode(link);
      if (mode != -1)
        e.setMode(mode); 
    } catch (IOException iOException) {}
    e.setLinkName(target);
    this.tar.putArchiveEntry(e);
    this.tar.closeArchiveEntry();
    this.entriesWritten++;
  }
  
  public boolean understandsSymlink() { return true; }
  
  public void visit(File file, String relativePath) throws IOException {
    if (Functions.isWindows())
      relativePath = relativePath.replace('\\', '/'); 
    BasicFileAttributes basicFileAttributes = Files.readAttributes(Util.fileToPath(file), BasicFileAttributes.class, new java.nio.file.LinkOption[0]);
    if (basicFileAttributes.isDirectory())
      relativePath = relativePath + "/"; 
    TarArchiveEntry te = new TarArchiveEntry(relativePath);
    int mode = IOUtils.mode(file);
    if (mode != -1)
      te.setMode(mode); 
    te.setModTime(basicFileAttributes.lastModifiedTime().toMillis());
    long size = 0L;
    if (!basicFileAttributes.isDirectory()) {
      size = basicFileAttributes.size();
      te.setSize(size);
    } 
    this.tar.putArchiveEntry(te);
    try {
      if (!basicFileAttributes.isDirectory()) {
        InputStream fin = Files.newInputStream(file.toPath(), new java.nio.file.OpenOption[0]);
        try {
          BoundedInputStream in = new BoundedInputStream(fin, size);
          try {
            while (true) {
              try {
                int len;
                if ((len = in.read(this.buf)) >= 0) {
                  this.tar.write(this.buf, 0, len);
                  continue;
                } 
                break;
              } catch (IOException|java.nio.file.InvalidPathException e) {
                throw new IOException("Error writing to tar file from: " + file, e);
              } 
            } 
            in.close();
          } catch (Throwable throwable) {
            try {
              in.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            } 
            throw throwable;
          } 
          if (fin != null)
            fin.close(); 
        } catch (Throwable throwable) {
          if (fin != null)
            try {
              fin.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } 
    } finally {
      this.tar.closeArchiveEntry();
    } 
    this.entriesWritten++;
  }
  
  public void close() throws IOException { this.tar.close(); }
}
