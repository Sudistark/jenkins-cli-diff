package hudson.util.io;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.OpenOption;
import org.kohsuke.accmod.Restricted;

public abstract class ArchiverFactory implements Serializable {
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "used in plugin")
  public static ArchiverFactory TAR = new TarArchiverFactory(FilePath.TarCompression.NONE);
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "used in plugin")
  public static ArchiverFactory TARGZ = new TarArchiverFactory(FilePath.TarCompression.GZIP);
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "used in plugin")
  public static ArchiverFactory ZIP = new ZipArchiverFactory();
  
  private static final long serialVersionUID = 1L;
  
  @NonNull
  public abstract Archiver create(OutputStream paramOutputStream) throws IOException;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static ArchiverFactory createZipWithPrefix(String prefix, OpenOption... openOptions) { return new ZipArchiverFactory(prefix, openOptions); }
}
