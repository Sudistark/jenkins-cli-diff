package jenkins.fingerprints;

import com.thoughtworks.xstream.converters.basic.DateConverter;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.Functions;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.Fingerprint;
import hudson.model.TaskListener;
import hudson.model.listeners.SaveableListener;
import hudson.util.AtomicFileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import jenkins.model.FingerprintFacet;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Symbol({"fileFingerprintStorage"})
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension(ordinal = -100.0D)
public class FileFingerprintStorage extends FingerprintStorage {
  private static final Logger logger = Logger.getLogger(FileFingerprintStorage.class.getName());
  
  private static final DateConverter DATE_CONVERTER = new DateConverter();
  
  public static final String FINGERPRINTS_DIR_NAME = "fingerprints";
  
  private static final Pattern FINGERPRINT_FILE_PATTERN = Pattern.compile("[0-9a-f]{28}\\.xml");
  
  @CheckForNull
  public Fingerprint load(@NonNull String id) throws IOException {
    if (!isAllowed(id))
      return null; 
    return load(getFingerprintFile(id));
  }
  
  @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"}, justification = "intentional check for fingerprint corruption")
  @CheckForNull
  public static Fingerprint load(@NonNull File file) throws IOException {
    XmlFile configFile = getConfigFile(file);
    if (!configFile.exists())
      return null; 
    try {
      Object loaded = configFile.read();
      if (!(loaded instanceof Fingerprint))
        throw new IOException("Unexpected Fingerprint type. Expected " + Fingerprint.class + " or subclass but got " + (
            (loaded != null) ? loaded.getClass() : "null")); 
      Fingerprint f = (Fingerprint)loaded;
      if (f.getPersistedFacets() == null) {
        logger.log(Level.WARNING, "Malformed fingerprint {0}: Missing facets", configFile);
        Files.deleteIfExists(Util.fileToPath(file));
        return null;
      } 
      return f;
    } catch (IOException e) {
      if (Files.exists(Util.fileToPath(file), new java.nio.file.LinkOption[0]) && Files.size(Util.fileToPath(file)) == 0L) {
        logger.log(Level.WARNING, "Size zero fingerprint. Disk corruption? {0}", configFile);
        Files.delete(Util.fileToPath(file));
        return null;
      } 
      String parseError = messageOfParseException(e);
      if (parseError != null) {
        logger.log(Level.WARNING, "Malformed XML in {0}: {1}", new Object[] { configFile, parseError });
        Files.deleteIfExists(Util.fileToPath(file));
        return null;
      } 
      logger.log(Level.WARNING, "Failed to load " + configFile, e);
      throw e;
    } 
  }
  
  public void save(Fingerprint fp) throws IOException {
    File file;
    synchronized (fp) {
      file = getFingerprintFile(fp.getHashString());
      save(fp, file);
    } 
    SaveableListener.fireOnChange(fp, getConfigFile(file));
  }
  
  public static void save(Fingerprint fp, File file) throws IOException {
    if (fp.getPersistedFacets().isEmpty()) {
      Util.createDirectories(Util.fileToPath(file.getParentFile()), new java.nio.file.attribute.FileAttribute[0]);
      afw = new AtomicFileWriter(file);
      try {
        PrintWriter w = new PrintWriter(new BufferedWriter(afw));
        try {
          w.println("<?xml version='1.1' encoding='UTF-8'?>");
          w.println("<fingerprint>");
          w.print("  <timestamp>");
          w.print(DATE_CONVERTER.toString(fp.getTimestamp()));
          w.println("</timestamp>");
          if (fp.getOriginal() != null) {
            w.println("  <original>");
            w.print("    <name>");
            w.print(Util.xmlEscape(fp.getOriginal().getName()));
            w.println("</name>");
            w.print("    <number>");
            w.print(fp.getOriginal().getNumber());
            w.println("</number>");
            w.println("  </original>");
          } 
          w.print("  <md5sum>");
          w.print(fp.getHashString());
          w.println("</md5sum>");
          w.print("  <fileName>");
          w.print(Util.xmlEscape(fp.getFileName()));
          w.println("</fileName>");
          w.println("  <usages>");
          for (Map.Entry<String, Fingerprint.RangeSet> e : fp.getUsages().entrySet()) {
            w.println("    <entry>");
            w.print("      <string>");
            w.print(Util.xmlEscape((String)e.getKey()));
            w.println("</string>");
            w.print("      <ranges>");
            w.print(Fingerprint.RangeSet.ConverterImpl.serialize((Fingerprint.RangeSet)e.getValue()));
            w.println("</ranges>");
            w.println("    </entry>");
          } 
          w.println("  </usages>");
          w.println("  <facets/>");
          w.print("</fingerprint>");
          w.flush();
          afw.commit();
          w.close();
        } catch (Throwable throwable) {
          try {
            w.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } finally {
        afw.abort();
      } 
    } else {
      getConfigFile(file).write(fp);
    } 
  }
  
  public void delete(String id) throws IOException {
    File file = getFingerprintFile(id);
    if (!file.exists())
      return; 
    if (!file.delete())
      throw new IOException("Error occurred in deleting Fingerprint " + id); 
    File inner = new File(Jenkins.get().getRootDir(), "fingerprints/" + id.substring(0, 2) + "/" + id.substring(2, 4));
    String[] innerFiles = inner.list();
    if (innerFiles != null && innerFiles.length == 0 && 
      !inner.delete())
      throw new IOException("Error occurred in deleting inner directory of Fingerprint " + id); 
    File outer = new File(Jenkins.get().getRootDir(), "fingerprints/" + id.substring(0, 2));
    String[] outerFiles = outer.list();
    if (outerFiles != null && outerFiles.length == 0 && 
      !outer.delete())
      throw new IOException("Error occurred in deleting outer directory of Fingerprint " + id); 
  }
  
  public boolean isReady() { return (new File(Jenkins.get().getRootDir(), "fingerprints")).exists(); }
  
  public void iterateAndCleanupFingerprints(TaskListener taskListener) {
    int numFiles = 0;
    File root = new File(getRootDir(), "fingerprints");
    File[] files1 = root.listFiles(f -> (f.isDirectory() && f.getName().length() == 2));
    if (files1 != null)
      for (File file1 : files1) {
        File[] files2 = file1.listFiles(f -> (f.isDirectory() && f.getName().length() == 2));
        for (File file2 : files2) {
          File[] files3 = file2.listFiles(f -> (f.isFile() && FINGERPRINT_FILE_PATTERN.matcher(f.getName()).matches()));
          for (File file3 : files3) {
            if (cleanFingerprint(file3, taskListener))
              numFiles++; 
          } 
          deleteIfEmpty(file2);
        } 
        deleteIfEmpty(file1);
      }  
    taskListener.getLogger().println("Cleaned up " + numFiles + " records");
  }
  
  private boolean cleanFingerprint(File fingerprintFile, TaskListener listener) {
    try {
      Fingerprint fp = loadFingerprint(fingerprintFile);
      if (fp == null || (!fp.isAlive() && fp.getFacetBlockingDeletion() == null)) {
        listener.getLogger().println("deleting obsolete " + fingerprintFile);
        Files.deleteIfExists(fingerprintFile.toPath());
        return true;
      } 
      if (!fp.isAlive()) {
        FingerprintFacet deletionBlockerFacet = fp.getFacetBlockingDeletion();
        listener.getLogger().println(deletionBlockerFacet.getClass().getName() + " created on " + deletionBlockerFacet.getClass().getName() + " blocked deletion of " + new Date(deletionBlockerFacet.getTimestamp()));
      } 
      fp = getFingerprint(fp);
      return fp.trim();
    } catch (IOException|java.nio.file.InvalidPathException e) {
      Functions.printStackTrace(e, listener.error("Failed to process " + fingerprintFile));
      return false;
    } 
  }
  
  @NonNull
  private static XmlFile getConfigFile(@NonNull File file) { return new XmlFile(Fingerprint.getXStream(), file); }
  
  @NonNull
  private static File getFingerprintFile(@NonNull String id) {
    return new File(Jenkins.get().getRootDir(), "fingerprints/" + id
        .substring(0, 2) + "/" + id.substring(2, 4) + "/" + id.substring(4) + ".xml");
  }
  
  private static boolean isAllowed(String id) {
    try {
      Util.fromHexString(id);
      return true;
    } catch (NumberFormatException nfe) {
      return false;
    } 
  }
  
  private static String messageOfParseException(Throwable throwable) {
    if (throwable instanceof org.xmlpull.v1.XmlPullParserException || throwable instanceof java.io.EOFException)
      return throwable.getMessage(); 
    Throwable causeOfThrowable = throwable.getCause();
    if (causeOfThrowable != null)
      return messageOfParseException(causeOfThrowable); 
    return null;
  }
  
  @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE"}, justification = "https://github.com/spotbugs/spotbugs/issues/756")
  private void deleteIfEmpty(File dir) {
    try {
      if (Files.isDirectory(dir.toPath(), new java.nio.file.LinkOption[0])) {
        boolean isEmpty;
        DirectoryStream<Path> directory = Files.newDirectoryStream(dir.toPath());
        try {
          isEmpty = !directory.iterator().hasNext();
          if (directory != null)
            directory.close(); 
        } catch (Throwable throwable) {
          if (directory != null)
            try {
              directory.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
        if (isEmpty)
          Files.delete(dir.toPath()); 
      } 
    } catch (IOException|java.nio.file.InvalidPathException e) {
      IOException iOException;
      logger.log(Level.WARNING, null, iOException);
    } 
  }
  
  protected Fingerprint loadFingerprint(File fingerprintFile) throws IOException { return load(fingerprintFile); }
  
  protected Fingerprint getFingerprint(Fingerprint fp) throws IOException { return Jenkins.get()._getFingerprint(fp.getHashString()); }
  
  protected File getRootDir() { return Jenkins.get().getRootDir(); }
}
