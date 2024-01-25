package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.Cleaner;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;

public class AtomicFileWriter extends Writer {
  private static final Logger LOGGER = Logger.getLogger(AtomicFileWriter.class.getName());
  
  private static final Cleaner CLEANER = Cleaner.create(new NamingThreadFactory(new DaemonThreadFactory(), AtomicFileWriter.class
        .getName() + ".cleaner"));
  
  private static boolean DISABLE_FORCED_FLUSH = SystemProperties.getBoolean(AtomicFileWriter.class
      .getName() + ".DISABLE_FORCED_FLUSH");
  
  private final FileChannelWriter core;
  
  private final Path tmpPath;
  
  private final Path destPath;
  
  static  {
    if (DISABLE_FORCED_FLUSH)
      LOGGER.log(Level.WARNING, "DISABLE_FORCED_FLUSH flag used, this could result in dataloss if failures happen in your storage subsystem."); 
  }
  
  public AtomicFileWriter(File f) throws IOException { this(toPath(f), StandardCharsets.UTF_8); }
  
  @Deprecated
  public AtomicFileWriter(@NonNull File f, @Nullable String encoding) throws IOException { this(toPath(f), (encoding == null) ? Charset.defaultCharset() : Charset.forName(encoding)); }
  
  private static Path toPath(@NonNull File file) throws IOException {
    try {
      return file.toPath();
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  public AtomicFileWriter(@NonNull Path destinationPath, @NonNull Charset charset) throws IOException { this(destinationPath, charset, false, true); }
  
  @Deprecated
  public AtomicFileWriter(@NonNull Path destinationPath, @NonNull Charset charset, boolean integrityOnFlush, boolean integrityOnClose) throws IOException {
    if (charset == null)
      throw new IllegalArgumentException("charset is null"); 
    this.destPath = destinationPath;
    Path dir = this.destPath.getParent();
    if (Files.exists(dir, new java.nio.file.LinkOption[0]) && !Files.isDirectory(dir, new java.nio.file.LinkOption[0]))
      throw new IOException("" + dir + " exists and is neither a directory nor a symlink to a directory"); 
    if (Files.isSymbolicLink(dir)) {
      LOGGER.log(Level.CONFIG, "{0} is a symlink to a directory", dir);
    } else {
      Files.createDirectories(dir, new java.nio.file.attribute.FileAttribute[0]);
    } 
    try {
      this.tmpPath = File.createTempFile("atomic", "tmp", dir.toFile()).toPath();
    } catch (IOException e) {
      throw new IOException("Failed to create a temporary file in " + dir, e);
    } 
    if (DISABLE_FORCED_FLUSH) {
      integrityOnFlush = false;
      integrityOnClose = false;
    } 
    this.core = new FileChannelWriter(this.tmpPath, charset, integrityOnFlush, integrityOnClose, new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE });
    CLEANER.register(this, new CleanupChecker(this.core, this.tmpPath, this.destPath));
  }
  
  public void write(int c) throws IOException { this.core.write(c); }
  
  public void write(String str, int off, int len) throws IOException { this.core.write(str, off, len); }
  
  public void write(char[] cbuf, int off, int len) throws IOException { this.core.write(cbuf, off, len); }
  
  public void flush() throws IOException { this.core.flush(); }
  
  public void close() throws IOException { this.core.close(); }
  
  public void abort() throws IOException {
    try {
      close();
    } finally {
      Files.deleteIfExists(this.tmpPath);
    } 
  }
  
  public void commit() throws IOException {
    close();
    try {
      Files.move(this.tmpPath, this.destPath, new CopyOption[] { StandardCopyOption.ATOMIC_MOVE });
    } catch (IOException moveFailed) {
      if (moveFailed instanceof java.nio.file.AtomicMoveNotSupportedException) {
        LOGGER.log(Level.WARNING, "Atomic move not supported. falling back to non-atomic move.", moveFailed);
      } else {
        LOGGER.log(Level.WARNING, "Unable to move atomically, falling back to non-atomic move.", moveFailed);
      } 
      if (this.destPath.toFile().exists())
        LOGGER.log(Level.INFO, "The target file {0} was already existing", this.destPath); 
      try {
        Files.move(this.tmpPath, this.destPath, new CopyOption[] { StandardCopyOption.REPLACE_EXISTING });
      } catch (IOException replaceFailed) {
        replaceFailed.addSuppressed(moveFailed);
        LOGGER.log(Level.WARNING, "Unable to move {0} to {1}. Attempting to delete {0} and abandoning.", new Path[] { this.tmpPath, this.destPath });
        try {
          Files.deleteIfExists(this.tmpPath);
        } catch (IOException deleteFailed) {
          replaceFailed.addSuppressed(deleteFailed);
          LOGGER.log(Level.WARNING, "Unable to delete {0}, good bye then!", this.tmpPath);
          throw replaceFailed;
        } 
        throw replaceFailed;
      } 
    } 
  }
  
  @Deprecated
  public File getTemporaryFile() { return this.tmpPath.toFile(); }
  
  public Path getTemporaryPath() { return this.tmpPath; }
}
