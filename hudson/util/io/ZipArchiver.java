package hudson.util.io;

import hudson.FilePath;
import hudson.Util;
import hudson.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.Zip64Mode;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.kohsuke.accmod.Restricted;

final class ZipArchiver extends Archiver {
  private final byte[] buf;
  
  private final ZipOutputStream zip;
  
  private final OpenOption[] openOptions;
  
  private final String prefix;
  
  private static final long BITMASK_IS_DIRECTORY = 16L;
  
  ZipArchiver(OutputStream out) { this(out, "", new OpenOption[0]); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  ZipArchiver(OutputStream out, String prefix, OpenOption... openOptions) {
    this.buf = new byte[8192];
    this.openOptions = openOptions;
    if (StringUtils.isBlank(prefix)) {
      this.prefix = "";
    } else {
      this.prefix = Util.ensureEndsWith(prefix, "/");
    } 
    this.zip = new ZipOutputStream(out);
    this.zip.setEncoding(System.getProperty("file.encoding"));
    this.zip.setUseZip64(Zip64Mode.AsNeeded);
  }
  
  public void visit(File f, String _relativePath) throws IOException {
    int mode = IOUtils.mode(f);
    String relativePath = _relativePath.replace('\\', '/');
    BasicFileAttributes basicFileAttributes = Files.readAttributes(Util.fileToPath(f), BasicFileAttributes.class, new java.nio.file.LinkOption[0]);
    if (basicFileAttributes.isDirectory()) {
      ZipEntry dirZipEntry = new ZipEntry(this.prefix + this.prefix + "/");
      dirZipEntry.setExternalAttributes(16L);
      if (mode != -1)
        dirZipEntry.setUnixMode(mode); 
      dirZipEntry.setTime(basicFileAttributes.lastModifiedTime().toMillis());
      this.zip.putNextEntry(dirZipEntry);
      this.zip.closeEntry();
    } else {
      ZipEntry fileZipEntry = new ZipEntry(this.prefix + this.prefix);
      if (mode != -1)
        fileZipEntry.setUnixMode(mode); 
      fileZipEntry.setTime(basicFileAttributes.lastModifiedTime().toMillis());
      fileZipEntry.setSize(basicFileAttributes.size());
      this.zip.putNextEntry(fileZipEntry);
      try {
        InputStream in = FilePath.openInputStream(f, this.openOptions);
        try {
          int len;
          while ((len = in.read(this.buf)) >= 0)
            this.zip.write(this.buf, 0, len); 
          if (in != null)
            in.close(); 
        } catch (Throwable throwable) {
          if (in != null)
            try {
              in.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } catch (InvalidPathException e) {
        throw new IOException(e);
      } 
      this.zip.closeEntry();
    } 
    this.entriesWritten++;
  }
  
  public void close() throws IOException { this.zip.close(); }
}
