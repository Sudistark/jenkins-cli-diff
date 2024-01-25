package jenkins.diagnosis;

import hudson.Extension;
import hudson.Functions;
import hudson.Util;
import hudson.diagnosis.Messages;
import hudson.model.AdministrativeMonitor;
import hudson.util.jna.Kernel32Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.security.stapler.StaplerDispatchable;
import jenkins.util.JavaVMArguments;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;

@Extension(optional = true)
@Symbol({"hsErrPid"})
public class HsErrPidList extends AdministrativeMonitor {
  final List<HsErrPidFile> files = new ArrayList();
  
  private MappedByteBuffer map;
  
  private static final String ERROR_FILE_OPTION = "-XX:ErrorFile=";
  
  public HsErrPidList() {
    if (Functions.getIsUnitTest())
      return; 
    try {
      try {
        FileChannel ch = FileChannel.open(getSecretKeyFile().toPath(), new OpenOption[] { StandardOpenOption.READ });
        try {
          this.map = ch.map(FileChannel.MapMode.READ_ONLY, 0L, 1L);
          if (ch != null)
            ch.close(); 
        } catch (Throwable throwable) {
          if (ch != null)
            try {
              ch.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } catch (InvalidPathException e) {
        throw new IOException(e);
      } 
      scan("./hs_err_pid%p.log");
      if (Functions.isWindows()) {
        File dir = Kernel32Utils.getTempDir();
        if (dir != null)
          scan(dir.getPath() + "\\hs_err_pid%p.log"); 
      } else {
        scan("/tmp/hs_err_pid%p.log");
      } 
      for (String a : JavaVMArguments.current()) {
        if (a.startsWith("-XX:ErrorFile="))
          scan(a.substring("-XX:ErrorFile=".length())); 
      } 
    } catch (UnsupportedOperationException unsupportedOperationException) {
    
    } catch (Throwable e) {
      LOGGER.log(Level.WARNING, "Failed to list up hs_err_pid files", e);
    } 
  }
  
  public String getDisplayName() { return Messages.HsErrPidList_DisplayName(); }
  
  @StaplerDispatchable
  public List<HsErrPidFile> getFiles() { return this.files; }
  
  private void scan(String pattern) {
    LOGGER.fine("Scanning " + pattern + " for hs_err_pid files");
    pattern = pattern.replace("%p", "*").replace("%%", "%");
    File f = (new File(pattern)).getAbsoluteFile();
    if (!pattern.contains("*")) {
      scanFile(f);
    } else {
      File commonParent = f;
      while (commonParent != null && commonParent.getPath().contains("*"))
        commonParent = commonParent.getParentFile(); 
      if (commonParent == null) {
        LOGGER.warning("Failed to process " + f);
        return;
      } 
      FileSet fs = Util.createFileSet(commonParent, f.getPath().substring(commonParent.getPath().length() + 1), null);
      DirectoryScanner ds = fs.getDirectoryScanner(new Project());
      for (String child : ds.getIncludedFiles())
        scanFile(new File(commonParent, child)); 
    } 
  }
  
  private void scanFile(File log) {
    LOGGER.fine("Scanning " + log);
    try {
      Reader rawReader = Files.newBufferedReader(log.toPath(), Charset.defaultCharset());
      try {
        BufferedReader r = new BufferedReader(rawReader);
        try {
          if (!findHeader(r)) {
            r.close();
            if (rawReader != null)
              rawReader.close(); 
            return;
          } 
          String secretKey = getSecretKeyFile().getAbsolutePath();
          String line;
          while ((line = r.readLine()) != null) {
            if (line.contains(secretKey)) {
              this.files.add(new HsErrPidFile(this, log));
              r.close();
              if (rawReader != null)
                rawReader.close(); 
              return;
            } 
          } 
          r.close();
        } catch (Throwable throwable) {
          try {
            r.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
        if (rawReader != null)
          rawReader.close(); 
      } catch (Throwable throwable) {
        if (rawReader != null)
          try {
            rawReader.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException|InvalidPathException e) {
      LOGGER.log(Level.FINE, "Failed to parse hs_err_pid file: " + log, e);
    } 
  }
  
  private File getSecretKeyFile() { return new File(Jenkins.get().getRootDir(), "secret.key"); }
  
  private boolean findHeader(BufferedReader r) throws IOException {
    for (int i = 0; i < 5; i++) {
      String line = r.readLine();
      if (line == null)
        return false; 
      if (line.startsWith("# A fatal error has been detected by the Java Runtime Environment:"))
        return true; 
    } 
    return false;
  }
  
  public boolean isActivated() { return !this.files.isEmpty(); }
  
  private static final Logger LOGGER = Logger.getLogger(HsErrPidList.class.getName());
}
