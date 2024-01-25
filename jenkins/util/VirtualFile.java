package jenkins.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.Util;
import hudson.remoting.Callable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.selectors.TokenizedPath;
import org.apache.tools.ant.types.selectors.TokenizedPattern;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.kohsuke.accmod.Restricted;

public abstract class VirtualFile extends Object implements Comparable<VirtualFile>, Serializable {
  @CheckForNull
  public String readLink() { return null; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public VirtualFile[] list(OpenOption... openOptions) throws IOException { return list(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean supportsQuickRecursiveListing() throws IOException { return false; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasSymlink(OpenOption... openOptions) throws IOException { return false; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public List<VirtualFile> listOnlyDescendants() throws IOException {
    VirtualFile[] children = list();
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    for (VirtualFile child : children) {
      if (child.isDescendant(""))
        result.add(child); 
    } 
    return result;
  }
  
  @Deprecated
  @NonNull
  public String[] list(String glob) throws IOException { return (String[])list(glob.replace('\\', '/'), null, true).toArray(MemoryReductionUtil.EMPTY_STRING_ARRAY); }
  
  @NonNull
  public Collection<String> list(@NonNull String includes, @CheckForNull String excludes, boolean useDefaultExcludes) throws IOException { return list(includes, excludes, useDefaultExcludes, new OpenOption[0]); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public Collection<String> list(@NonNull String includes, @CheckForNull String excludes, boolean useDefaultExcludes, OpenOption... openOptions) throws IOException {
    Collection<String> r = (Collection)run(new CollectFiles(this));
    List<TokenizedPattern> includePatterns = patterns(includes);
    List<TokenizedPattern> excludePatterns = patterns(excludes);
    if (useDefaultExcludes)
      for (String patt : DirectoryScanner.getDefaultExcludes())
        excludePatterns.add(new TokenizedPattern(patt.replace('/', File.separatorChar)));  
    return (Collection)r.stream().filter(p -> {
          TokenizedPath path = new TokenizedPath(p.replace('/', File.separatorChar));
          return (includePatterns.stream().anyMatch(()) && excludePatterns.stream().noneMatch(()));
        }).collect(Collectors.toSet());
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean containsSymLinkChild(OpenOption... openOptions) throws IOException { return false; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean containsTmpDirChild(OpenOption... openOptions) throws IOException {
    for (VirtualFile child : list()) {
      if (child.isDirectory() && FilePath.isTmpDir(child.getName(), openOptions))
        return true; 
    } 
    return false;
  }
  
  private List<TokenizedPattern> patterns(String patts) {
    List<TokenizedPattern> r = new ArrayList<TokenizedPattern>();
    if (patts != null)
      for (String patt : patts.split(",")) {
        if (patt.endsWith("/"))
          patt = patt + "**"; 
        r.add(new TokenizedPattern(patt.replace('/', File.separatorChar)));
      }  
    return r;
  }
  
  public int zip(OutputStream outputStream, String includes, String excludes, boolean useDefaultExcludes, String prefix, OpenOption... openOptions) throws IOException {
    String correctPrefix;
    if (StringUtils.isBlank(prefix)) {
      correctPrefix = "";
    } else {
      correctPrefix = Util.ensureEndsWith(prefix, "/");
    } 
    Collection<String> files = list(includes, excludes, useDefaultExcludes, openOptions);
    ZipOutputStream zos = new ZipOutputStream(outputStream);
    try {
      zos.setEncoding(System.getProperty("file.encoding"));
      for (String relativePath : files) {
        VirtualFile virtualFile = child(relativePath);
        sendOneZipEntry(zos, virtualFile, relativePath, correctPrefix, openOptions);
      } 
      zos.close();
    } catch (Throwable throwable) {
      try {
        zos.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    return files.size();
  }
  
  private void sendOneZipEntry(ZipOutputStream zos, VirtualFile vf, String relativePath, String prefix, OpenOption... openOptions) throws IOException {
    String onlyForwardRelativePath = relativePath.replace('\\', '/');
    String zipEntryName = prefix + prefix;
    ZipEntry e = new ZipEntry(zipEntryName);
    e.setTime(vf.lastModified());
    zos.putNextEntry(e);
    try {
      InputStream in = vf.open(openOptions);
      try {
        IOUtils.copy(in, zos);
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
    } finally {
      zos.closeEntry();
    } 
  }
  
  public int mode() throws IOException { return -1; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public InputStream open(OpenOption... openOptions) throws IOException { return open(); }
  
  public final int compareTo(VirtualFile o) { return getName().compareToIgnoreCase(o.getName()); }
  
  public final boolean equals(Object obj) { return (obj instanceof VirtualFile && toURI().equals(((VirtualFile)obj).toURI())); }
  
  public final int hashCode() throws IOException { return toURI().hashCode(); }
  
  public final String toString() { return toURI().toString(); }
  
  public <V> V run(Callable<V, IOException> callable) throws IOException { return (V)callable.call(); }
  
  @CheckForNull
  public URL toExternalURL() throws IOException { return null; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean supportIsDescendant() throws IOException { return false; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isDescendant(String childRelativePath) throws IOException { return false; }
  
  String joinWithForwardSlashes(Collection<String> relativePath) {
    return String.join("/", relativePath) + "/";
  }
  
  public static VirtualFile forFile(File f) { return new FileVF(f, f); }
  
  public static VirtualFile forFilePath(FilePath f) { return new FilePathVF(f, f); }
  
  @NonNull
  public abstract String getName();
  
  @NonNull
  public abstract URI toURI();
  
  public abstract VirtualFile getParent();
  
  public abstract boolean isDirectory() throws IOException;
  
  public abstract boolean isFile() throws IOException;
  
  public abstract boolean exists() throws IOException;
  
  @NonNull
  public abstract VirtualFile[] list() throws IOException;
  
  @NonNull
  public abstract VirtualFile child(@NonNull String paramString);
  
  public abstract long length() throws IOException;
  
  public abstract long lastModified() throws IOException;
  
  public abstract boolean canRead() throws IOException;
  
  public abstract InputStream open() throws IOException;
}
