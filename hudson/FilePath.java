package hudson;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Computer;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.Channel;
import hudson.remoting.DelegatingCallable;
import hudson.remoting.Future;
import hudson.remoting.LocalChannel;
import hudson.remoting.Pipe;
import hudson.remoting.RemoteInputStream;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import hudson.remoting.Which;
import hudson.security.AccessControlled;
import hudson.slaves.WorkspaceList;
import hudson.util.DaemonThreadFactory;
import hudson.util.DirScanner;
import hudson.util.ExceptionCatchingThreadFactory;
import hudson.util.FileVisitor;
import hudson.util.FormValidation;
import hudson.util.IOUtils;
import hudson.util.NamingThreadFactory;
import hudson.util.io.Archiver;
import hudson.util.io.ArchiverFactory;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import jenkins.model.Jenkins;
import jenkins.util.ContextResettingExecutorService;
import jenkins.util.SystemProperties;
import jenkins.util.VirtualFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.jenkinsci.remoting.SerializableOnlyOverRemoting;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Stapler;

public final class FilePath implements SerializableOnlyOverRemoting {
  private static final int MAX_REDIRECTS = 20;
  
  private VirtualChannel channel;
  
  private String remote;
  
  public FilePath(@CheckForNull VirtualChannel channel, @NonNull String remote) {
    this.channel = (channel instanceof LocalChannel) ? null : channel;
    this.remote = normalize(remote);
  }
  
  public FilePath(@NonNull File localPath) {
    this.channel = null;
    this.remote = normalize(localPath.getPath());
  }
  
  public FilePath(@NonNull FilePath base, @NonNull String rel) {
    this.channel = base.channel;
    this.remote = normalize(resolvePathIfRelative(base, rel));
  }
  
  private Object readResolve() {
    this.remote = normalize(this.remote);
    return this;
  }
  
  private String resolvePathIfRelative(@NonNull FilePath base, @NonNull String rel) {
    if (isAbsolute(rel))
      return rel; 
    if (base.isUnix())
      return base.remote + "/" + base.remote; 
    return base.remote + "\\" + base.remote;
  }
  
  private static boolean isAbsolute(@NonNull String rel) { return (rel.startsWith("/") || DRIVE_PATTERN.matcher(rel).matches() || UNC_PATTERN.matcher(rel).matches()); }
  
  private static final Pattern DRIVE_PATTERN = Pattern.compile("[A-Za-z]:[\\\\/].*");
  
  private static final Pattern UNC_PATTERN = Pattern.compile("^\\\\\\\\.*");
  
  private static final Pattern ABSOLUTE_PREFIX_PATTERN = Pattern.compile("^(\\\\\\\\|(?:[A-Za-z]:)?[\\\\/])[\\\\/]*");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String normalize(@NonNull String path) {
    StringBuilder buf = new StringBuilder();
    Matcher m = ABSOLUTE_PREFIX_PATTERN.matcher(path);
    if (m.find()) {
      buf.append(m.group(1));
      path = path.substring(m.end());
    } 
    boolean isAbsolute = (buf.length() > 0);
    List<String> tokens = new ArrayList<String>();
    int s = 0, end = path.length();
    for (int i = 0; i < end; i++) {
      char c = path.charAt(i);
      if (c == '/' || c == '\\') {
        tokens.add(path.substring(s, i));
        s = i;
        while (++i < end && ((c = path.charAt(i)) == '/' || c == '\\'));
        if (i < end)
          tokens.add(path.substring(s, s + 1)); 
        s = i;
      } 
    } 
    if (s < end)
      tokens.add(path.substring(s)); 
    for (int i = 0; i < tokens.size(); ) {
      String token = (String)tokens.get(i);
      if (token.equals(".")) {
        tokens.remove(i);
        if (tokens.size() > 0)
          tokens.remove((i > 0) ? (i - 1) : i); 
        continue;
      } 
      if (token.equals("..")) {
        if (i == 0) {
          tokens.remove(0);
          if (tokens.size() > 0)
            token = token + token; 
          if (!isAbsolute)
            buf.append(token); 
          continue;
        } 
        i -= 2;
        for (int j = 0; j < 3; ) {
          tokens.remove(i);
          j++;
        } 
        if (i > 0) {
          tokens.remove(i - 1);
          continue;
        } 
        if (tokens.size() > 0)
          tokens.remove(0); 
        continue;
      } 
      i += 2;
    } 
    for (String token : tokens)
      buf.append(token); 
    if (buf.length() == 0)
      buf.append('.'); 
    return buf.toString();
  }
  
  boolean isUnix() {
    if (!isRemote())
      return (File.pathSeparatorChar != ';'); 
    if (this.remote.length() > 3 && this.remote.charAt(1) == ':' && this.remote.charAt(2) == '\\')
      return false; 
    return !this.remote.contains("\\");
  }
  
  public String getRemote() { return this.remote; }
  
  @Deprecated
  public void createZipArchive(OutputStream os) throws IOException, InterruptedException { zip(os); }
  
  public void zip(OutputStream os) throws IOException, InterruptedException { zip(os, (FileFilter)null); }
  
  public void zip(FilePath dst) throws IOException, InterruptedException {
    OutputStream os = dst.write();
    try {
      zip(os);
      if (os != null)
        os.close(); 
    } catch (Throwable throwable) {
      if (os != null)
        try {
          os.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  public void zip(OutputStream os, FileFilter filter) throws IOException, InterruptedException { archive(ArchiverFactory.ZIP, os, filter); }
  
  @Deprecated
  public void createZipArchive(OutputStream os, String glob) throws IOException, InterruptedException { archive(ArchiverFactory.ZIP, os, glob); }
  
  public void zip(OutputStream os, String glob) throws IOException, InterruptedException { archive(ArchiverFactory.ZIP, os, glob); }
  
  public int zip(OutputStream out, DirScanner scanner) throws IOException, InterruptedException { return archive(ArchiverFactory.ZIP, out, scanner); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public int zip(OutputStream out, DirScanner scanner, String verificationRoot, String prefix, OpenOption... openOptions) throws IOException, InterruptedException {
    ArchiverFactory archiverFactory = (prefix == null) ? ArchiverFactory.ZIP : ArchiverFactory.createZipWithPrefix(prefix, openOptions);
    return archive(archiverFactory, out, scanner, verificationRoot, openOptions);
  }
  
  public int archive(ArchiverFactory factory, OutputStream os, DirScanner scanner) throws IOException, InterruptedException { return archive(factory, os, scanner, null, new OpenOption[0]); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public int archive(ArchiverFactory factory, OutputStream os, DirScanner scanner, String verificationRoot, OpenOption... openOptions) throws IOException, InterruptedException {
    RemoteOutputStream remoteOutputStream = (this.channel != null) ? new RemoteOutputStream(os) : os;
    return ((Integer)act(new Archive(factory, remoteOutputStream, scanner, verificationRoot, openOptions))).intValue();
  }
  
  public int archive(ArchiverFactory factory, OutputStream os, FileFilter filter) throws IOException, InterruptedException { return archive(factory, os, new DirScanner.Filter(filter)); }
  
  public int archive(ArchiverFactory factory, OutputStream os, String glob) throws IOException, InterruptedException { return archive(factory, os, new DirScanner.Glob(glob, null)); }
  
  public void unzip(FilePath target) throws IOException, InterruptedException {
    if (this.channel != target.channel) {
      RemoteInputStream in = new RemoteInputStream(read(), RemoteInputStream.Flag.GREEDY);
      target.act(new UnzipRemote(in));
    } else {
      target.act(new UnzipLocal(this));
    } 
  }
  
  public void untar(FilePath target, TarCompression compression) throws IOException, InterruptedException {
    FilePath source = this;
    if (source.channel != target.channel) {
      RemoteInputStream in = new RemoteInputStream(source.read(), RemoteInputStream.Flag.GREEDY);
      target.act(new UntarRemote(source.getName(), compression, in));
    } else {
      target.act(new UntarLocal(source, compression));
    } 
  }
  
  public void unzipFrom(InputStream _in) throws IOException, InterruptedException {
    RemoteInputStream remoteInputStream = new RemoteInputStream(_in, RemoteInputStream.Flag.GREEDY);
    act(new UnzipFrom(remoteInputStream));
  }
  
  private static void unzip(File dir, InputStream in) throws IOException {
    tmpFile = File.createTempFile("tmpzip", null);
    try {
      IOUtils.copy(in, tmpFile);
      unzip(dir, tmpFile);
    } finally {
      Files.delete(Util.fileToPath(tmpFile));
    } 
  }
  
  private static void unzip(File dir, File zipFile) throws IOException {
    dir = dir.getAbsoluteFile();
    ZipFile zip = new ZipFile(zipFile);
    try {
      Enumeration<ZipEntry> entries = zip.getEntries();
      while (entries.hasMoreElements()) {
        ZipEntry e = (ZipEntry)entries.nextElement();
        File f = new File(dir, e.getName());
        if (!f.getCanonicalFile().toPath().startsWith(dir.getCanonicalPath()))
          throw new IOException("Zip " + zipFile
              .getPath() + " contains illegal file name that breaks out of the target directory: " + e.getName()); 
        if (e.isDirectory()) {
          mkdirs(f);
          continue;
        } 
        File p = f.getParentFile();
        if (p != null)
          mkdirs(p); 
        input = zip.getInputStream(e);
        try {
          IOUtils.copy(input, f);
          if (input != null)
            input.close(); 
        } catch (Throwable throwable) {
          if (input != null)
            try {
              input.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
        try {
          FilePath target = new FilePath(f);
          int mode = e.getUnixMode();
          if (mode != 0)
            target.chmod(mode); 
        } catch (InterruptedException input) {
          InterruptedException ex;
          LOGGER.log(Level.WARNING, "unable to set permissions", ex);
        } 
        Files.setLastModifiedTime(Util.fileToPath(f), e.getLastModifiedTime());
      } 
      zip.close();
    } catch (Throwable throwable) {
      try {
        zip.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public FilePath absolutize() throws IOException, InterruptedException { return new FilePath(this.channel, (String)act(new Absolutize())); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean hasSymlink(FilePath verificationRoot, OpenOption... openOptions) throws IOException, InterruptedException { return ((Boolean)act(new HasSymlink((verificationRoot == null) ? null : verificationRoot.remote, openOptions))).booleanValue(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean containsSymlink(FilePath verificationRoot, OpenOption... openOptions) throws IOException, InterruptedException { return !list(new SymlinkRetainingFileFilter(verificationRoot, openOptions)).isEmpty(); }
  
  public void symlinkTo(String target, TaskListener listener) throws IOException, InterruptedException { act(new SymlinkTo(target, listener)); }
  
  public String readLink() { return (String)act(new ReadLink()); }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    FilePath that = (FilePath)o;
    if (!Objects.equals(this.channel, that.channel))
      return false; 
    return this.remote.equals(that.remote);
  }
  
  public int hashCode() { return 31 * ((this.channel != null) ? this.channel.hashCode() : 0) + this.remote.hashCode(); }
  
  public void untarFrom(InputStream _in, TarCompression compression) throws IOException, InterruptedException {
    InputStream inputStream = _in;
    try {
      RemoteInputStream remoteInputStream = new RemoteInputStream(_in, RemoteInputStream.Flag.GREEDY);
      act(new UntarFrom(compression, remoteInputStream));
      if (inputStream != null)
        inputStream.close(); 
    } catch (Throwable throwable) {
      if (inputStream != null)
        try {
          inputStream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  public boolean installIfNecessaryFrom(@NonNull URL archive, @CheckForNull TaskListener listener, @NonNull String message) throws IOException, InterruptedException {
    if (listener == null)
      listener = TaskListener.NULL; 
    return installIfNecessaryFrom(archive, listener, message, 20);
  }
  
  private boolean installIfNecessaryFrom(@NonNull URL archive, @NonNull TaskListener listener, @NonNull String message, int maxRedirects) throws InterruptedException, IOException {
    try {
      URLConnection con;
      FilePath timestamp = child(".timestamp");
      long lastModified = timestamp.lastModified();
      try {
        con = ProxyConfiguration.open(archive);
        if (lastModified != 0L)
          con.setIfModifiedSince(lastModified); 
        con.connect();
      } catch (IOException x) {
        if (exists()) {
          listener.getLogger().println("Skipping installation of " + archive + " to " + this.remote + ": " + x);
          return false;
        } 
        throw x;
      } 
      if (con instanceof HttpURLConnection) {
        HttpURLConnection httpCon = (HttpURLConnection)con;
        int responseCode = httpCon.getResponseCode();
        if (responseCode == 301 || responseCode == 302) {
          if (maxRedirects > 0) {
            String location = httpCon.getHeaderField("Location");
            listener.getLogger().println("Following redirect " + archive.toExternalForm() + " -> " + location);
            return installIfNecessaryFrom(getUrlFactory().newURL(location), listener, message, maxRedirects - 1);
          } 
          listener.getLogger().println("Skipping installation of " + archive + " to " + this.remote + " due to too many redirects.");
          return false;
        } 
        if (lastModified != 0L) {
          if (responseCode == 304)
            return false; 
          if (responseCode != 200) {
            listener.getLogger().println("Skipping installation of " + archive + " to " + this.remote + " due to server error: " + responseCode + " " + httpCon.getResponseMessage());
            return false;
          } 
        } 
      } 
      long sourceTimestamp = con.getLastModified();
      if (exists()) {
        if (lastModified != 0L && sourceTimestamp == lastModified)
          return false; 
        deleteContents();
      } else {
        mkdirs();
      } 
      listener.getLogger().println(message);
      if (isRemote())
        try {
          act(new Unpack(archive));
          timestamp.touch(sourceTimestamp);
          return true;
        } catch (IOException x) {
          Functions.printStackTrace(x, listener.error("Failed to download " + archive + " from agent; will retry from controller"));
        }  
      InputStream in = archive.getProtocol().startsWith("http") ? ProxyConfiguration.getInputStream(archive) : con.getInputStream();
      CountingInputStream cis = new CountingInputStream(in);
      try {
        if (archive.toExternalForm().endsWith(".zip")) {
          unzipFrom(cis);
        } else {
          untarFrom(cis, TarCompression.GZIP);
        } 
      } catch (IOException e) {
        throw new IOException(String.format("Failed to unpack %s (%d bytes read of total %d)", new Object[] { archive, 
                Long.valueOf(cis.getByteCount()), Integer.valueOf(con.getContentLength()) }), e);
      } 
      timestamp.touch(sourceTimestamp);
      return true;
    } catch (IOException e) {
      throw new IOException("Failed to install " + archive + " to " + this.remote, e);
    } 
  }
  
  public void copyFrom(URL url) throws IOException, InterruptedException {
    InputStream in = url.openStream();
    try {
      copyFrom(in);
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
  }
  
  public void copyFrom(InputStream in) throws IOException, InterruptedException {
    OutputStream os = write();
    try {
      IOUtils.copy(in, os);
      if (os != null)
        os.close(); 
    } catch (Throwable throwable) {
      if (os != null)
        try {
          os.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  public void copyFrom(FilePath src) throws IOException, InterruptedException { src.copyTo(this); }
  
  public void copyFrom(FileItem file) throws IOException, InterruptedException {
    if (this.channel == null) {
      try {
        file.write(new File(this.remote));
      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        throw new IOException(e);
      } 
    } else {
      InputStream i = file.getInputStream();
      try {
        OutputStream o = write();
        try {
          IOUtils.copy(i, o);
          if (o != null)
            o.close(); 
        } catch (Throwable throwable) {
          if (o != null)
            try {
              o.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
        if (i != null)
          i.close(); 
      } catch (Throwable throwable) {
        if (i != null)
          try {
            i.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } 
  }
  
  public <T> T act(FileCallable<T> callable) throws IOException, InterruptedException { return (T)act(callable, callable.getClass().getClassLoader()); }
  
  private <T> T act(FileCallable<T> callable, ClassLoader cl) throws IOException, InterruptedException {
    if (this.channel != null)
      try {
        DelegatingCallable delegatingCallable = new FileCallableWrapper(callable, cl, this);
        for (FileCallableWrapperFactory factory : ExtensionList.lookup(FileCallableWrapperFactory.class))
          delegatingCallable = factory.wrap(delegatingCallable); 
        return (T)this.channel.call(delegatingCallable);
      } catch (TunneledInterruptedException e) {
        throw (InterruptedException)(new InterruptedException(e.getMessage())).initCause(e);
      }  
    return (T)callable.invoke(new File(this.remote), localChannel);
  }
  
  public <T> Future<T> actAsync(FileCallable<T> callable) throws IOException, InterruptedException {
    try {
      DelegatingCallable delegatingCallable = new FileCallableWrapper(callable, this);
      for (FileCallableWrapperFactory factory : ExtensionList.lookup(FileCallableWrapperFactory.class))
        delegatingCallable = factory.wrap(delegatingCallable); 
      return ((this.channel != null) ? this.channel : localChannel)
        .callAsync(delegatingCallable);
    } catch (IOException e) {
      throw new IOException("remote file operation failed", e);
    } 
  }
  
  public <V, E extends Throwable> V act(Callable<V, E> callable) throws IOException, InterruptedException, E {
    if (this.channel != null)
      return (V)this.channel.call(callable); 
    return (V)callable.call();
  }
  
  public <V> Callable<V, IOException> asCallableWith(FileCallable<V> task) { return new CallableWith(this, task); }
  
  public URI toURI() throws IOException, InterruptedException { return (URI)act(new ToURI()); }
  
  public VirtualFile toVirtualFile() { return VirtualFile.forFilePath(this); }
  
  @CheckForNull
  public Computer toComputer() {
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j != null)
      for (Computer c : j.getComputers()) {
        if (getChannel() == c.getChannel())
          return c; 
      }  
    return null;
  }
  
  public void mkdirs() throws IOException, InterruptedException {
    if (!((Boolean)act(new Mkdirs())).booleanValue())
      throw new IOException("Failed to mkdirs: " + this.remote); 
  }
  
  public void deleteSuffixesRecursive() throws IOException, InterruptedException { act(new DeleteSuffixesRecursive()); }
  
  private static File[] listParentFiles(File f) {
    File parentFile = f.getParentFile();
    if (parentFile != null) {
      File[] files = parentFile.listFiles();
      if (files != null)
        return files; 
    } 
    return new File[0];
  }
  
  public void deleteRecursive() throws IOException, InterruptedException { act(new DeleteRecursive()); }
  
  public void deleteContents() throws IOException, InterruptedException { act(new DeleteContents()); }
  
  public String getBaseName() {
    String n = getName();
    int idx = n.lastIndexOf('.');
    if (idx < 0)
      return n; 
    return n.substring(0, idx);
  }
  
  public String getName() {
    String r = this.remote;
    if (r.endsWith("\\") || r.endsWith("/"))
      r = r.substring(0, r.length() - 1); 
    int len = r.length() - 1;
    while (len >= 0) {
      char ch = r.charAt(len);
      if (ch == '\\' || ch == '/')
        break; 
      len--;
    } 
    return r.substring(len + 1);
  }
  
  @CheckForNull
  public FilePath sibling(String rel) {
    FilePath parent = getParent();
    return (parent != null) ? parent.child(rel) : null;
  }
  
  public FilePath withSuffix(String suffix) { return new FilePath(this.channel, this.remote + this.remote); }
  
  @NonNull
  public FilePath child(String relOrAbsolute) { return new FilePath(this, relOrAbsolute); }
  
  @CheckForNull
  public FilePath getParent() throws IOException, InterruptedException {
    int i = this.remote.length() - 2;
    for (; i >= 0; i--) {
      char ch = this.remote.charAt(i);
      if (ch == '\\' || ch == '/')
        break; 
    } 
    return (i >= 0) ? new FilePath(this.channel, this.remote.substring(0, i + 1)) : null;
  }
  
  public FilePath createTempFile(String prefix, String suffix) throws IOException, InterruptedException {
    try {
      return new FilePath(this, (String)act(new CreateTempFile(prefix, suffix)));
    } catch (IOException e) {
      throw new IOException("Failed to create a temp file on " + this.remote, e);
    } 
  }
  
  public FilePath createTextTempFile(String prefix, String suffix, String contents) throws IOException, InterruptedException { return createTextTempFile(prefix, suffix, contents, true); }
  
  public FilePath createTextTempFile(String prefix, String suffix, String contents, boolean inThisDirectory) throws IOException, InterruptedException {
    try {
      return new FilePath(this.channel, (String)act(new CreateTextTempFile(inThisDirectory, prefix, suffix, contents)));
    } catch (IOException e) {
      throw new IOException("Failed to create a temp file on " + this.remote, e);
    } 
  }
  
  public FilePath createTempDir(String prefix, String suffix) throws IOException, InterruptedException {
    try {
      String[] s;
      if (StringUtils.isBlank(suffix)) {
        s = new String[] { prefix, "tmp" };
      } else {
        s = new String[] { prefix, suffix };
      } 
      String name = String.join(".", s);
      return new FilePath(this, (String)act(new CreateTempDir(name)));
    } catch (IOException e) {
      throw new IOException("Failed to create a temp directory on " + this.remote, e);
    } 
  }
  
  public boolean delete() {
    act(new Delete());
    return true;
  }
  
  public boolean exists() { return ((Boolean)act(new Exists())).booleanValue(); }
  
  public long lastModified() throws IOException, InterruptedException { return ((Long)act(new LastModified())).longValue(); }
  
  public void touch(long timestamp) throws IOException, InterruptedException { act(new Touch(timestamp)); }
  
  private void setLastModifiedIfPossible(long timestamp) throws IOException, InterruptedException {
    String message = (String)act(new SetLastModified(timestamp));
    if (message != null)
      LOGGER.warning(message); 
  }
  
  public boolean isDirectory() { return ((Boolean)act(new IsDirectory())).booleanValue(); }
  
  public long length() throws IOException, InterruptedException { return ((Long)act(new Length())).longValue(); }
  
  public long getFreeDiskSpace() throws IOException, InterruptedException { return ((Long)act(new GetFreeDiskSpace())).longValue(); }
  
  public long getTotalDiskSpace() throws IOException, InterruptedException { return ((Long)act(new GetTotalDiskSpace())).longValue(); }
  
  public long getUsableDiskSpace() throws IOException, InterruptedException { return ((Long)act(new GetUsableDiskSpace())).longValue(); }
  
  public void chmod(int mask) throws IOException, InterruptedException {
    if (!isUnix() || mask == -1)
      return; 
    act(new Chmod(mask));
  }
  
  private static void _chmod(File f, int mask) throws IOException {
    if (File.pathSeparatorChar == ';')
      return; 
    Files.setPosixFilePermissions(Util.fileToPath(f), Util.modeToPermissions(mask));
  }
  
  private static boolean CHMOD_WARNED = false;
  
  public int mode() {
    if (!isUnix())
      return -1; 
    return ((Integer)act(new Mode())).intValue();
  }
  
  @NonNull
  public List<FilePath> list() throws IOException, InterruptedException { return list((FileFilter)null); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public List<FilePath> list(FilePath verificationRoot, OpenOption... openOptions) throws IOException, InterruptedException { return list(new OptionalDiscardingFileFilter(verificationRoot, openOptions)); }
  
  @NonNull
  public List<FilePath> listDirectories() throws IOException, InterruptedException { return list(new DirectoryFilter()); }
  
  @NonNull
  public List<FilePath> list(FileFilter filter) throws IOException, InterruptedException {
    if (filter != null && !(filter instanceof java.io.Serializable))
      throw new IllegalArgumentException("Non-serializable filter of " + filter.getClass()); 
    return (List)act(new ListFilter(filter), ((filter != null) ? filter : this).getClass().getClassLoader());
  }
  
  @NonNull
  public FilePath[] list(String includes) throws IOException, InterruptedException { return list(includes, null); }
  
  @NonNull
  public FilePath[] list(String includes, String excludes) throws IOException, InterruptedException { return list(includes, excludes, true); }
  
  @NonNull
  public FilePath[] list(String includes, String excludes, boolean defaultExcludes) throws IOException, InterruptedException { return (FilePath[])act(new ListGlob(includes, excludes, defaultExcludes)); }
  
  @NonNull
  private static String[] glob(File dir, String includes, String excludes, boolean defaultExcludes) throws IOException {
    DirectoryScanner ds;
    if (isAbsolute(includes))
      throw new IOException("Expecting Ant GLOB pattern, but saw '" + includes + "'. See https://ant.apache.org/manual/Types/fileset.html for syntax"); 
    FileSet fs = Util.createFileSet(dir, includes, excludes);
    fs.setDefaultexcludes(defaultExcludes);
    try {
      ds = fs.getDirectoryScanner(new Project());
    } catch (BuildException x) {
      throw new IOException(x.getMessage());
    } 
    return ds.getIncludedFiles();
  }
  
  public InputStream read() throws IOException, InterruptedException { return read(null, new OpenOption[0]); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public InputStream read(FilePath rootPath, OpenOption... openOptions) throws IOException, InterruptedException {
    String rootPathString = (rootPath == null) ? null : rootPath.remote;
    if (this.channel == null) {
      File file = new File(this.remote);
      return newInputStreamDenyingSymlinkAsNeeded(file, rootPathString, openOptions);
    } 
    Pipe p = Pipe.createRemoteToLocal();
    actAsync(new Read(p, rootPathString, openOptions));
    return p.getIn();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static InputStream newInputStreamDenyingSymlinkAsNeeded(File file, String verificationRoot, OpenOption... openOptions) throws IOException {
    InputStream inputStream = null;
    try {
      denyTmpDir(file, verificationRoot, openOptions);
      denySymlink(file, verificationRoot, openOptions);
      inputStream = openInputStream(file, openOptions);
      denySymlink(file, verificationRoot, openOptions);
    } catch (IOException ioe) {
      if (inputStream != null)
        inputStream.close(); 
      throw ioe;
    } 
    return inputStream;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static InputStream openInputStream(File file, OpenOption[] openOptions) throws IOException { return Files.newInputStream(Util.fileToPath(file), stripLocalOptions(openOptions)); }
  
  private static OpenOption[] stripLocalOptions(OpenOption... openOptions) {
    if (openOptions != null)
      return (OpenOption[])Arrays.stream(openOptions).filter(option -> (option != DisplayOption.IGNORE_TMP_DIRS)).toArray(x$0 -> new OpenOption[x$0]); 
    return null;
  }
  
  private static void denySymlink(File file, String root, OpenOption... openOptions) throws IOException {
    if (isSymlink(file, root, openOptions))
      throw new IOException("Symlinks are prohibited."); 
  }
  
  private static void denyTmpDir(File file, String root, OpenOption... openOptions) throws IOException {
    if (isTmpDir(file, root, openOptions))
      throw new IOException("Tmp directory is prohibited."); 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isSymlink(File file, String root, OpenOption... openOptions) {
    if (isNoFollowLink(openOptions)) {
      if (Util.isSymlink(file.toPath()))
        return true; 
      return isFileAncestorSymlink(file, root);
    } 
    return false;
  }
  
  private static boolean isSymlink(VisitorInfo visitorInfo) { return isSymlink(visitorInfo.f, visitorInfo.verificationRoot, visitorInfo.openOptions); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isTmpDir(File file, String root, OpenOption... openOptions) {
    if (isIgnoreTmpDirs(openOptions)) {
      if (isTmpDir(file))
        return true; 
      return isFileAncestorTmpDir(file, root);
    } 
    return false;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isTmpDir(String filename, OpenOption... openOptions) {
    if (isIgnoreTmpDirs(openOptions))
      return isTmpDir(filename); 
    return false;
  }
  
  private static boolean isTmpDir(VisitorInfo visitorInfo) { return isTmpDir(visitorInfo.f, visitorInfo.verificationRoot, visitorInfo.openOptions); }
  
  private static boolean isTmpDir(File file) { return (file.isDirectory() && isTmpDir(file.getName())); }
  
  private static boolean isTmpDir(String filename) { return (filename.length() > WorkspaceList.TMP_DIR_SUFFIX.length() && filename.endsWith(WorkspaceList.TMP_DIR_SUFFIX)); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isNoFollowLink(OpenOption... openOptions) { return Arrays.asList(openOptions).contains(LinkOption.NOFOLLOW_LINKS); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isIgnoreTmpDirs(OpenOption... openOptions) { return Arrays.asList(openOptions).contains(DisplayOption.IGNORE_TMP_DIRS); }
  
  private static boolean isFileAncestorSymlink(File file, String root) { return doesFileAncestorMatch(file, root, Util::isSymlink); }
  
  private static boolean isFileAncestorTmpDir(File file, String root) { return doesFileAncestorMatch(file, root, path -> isTmpDir(path.toFile())); }
  
  private static boolean doesFileAncestorMatch(File file, String root, Predicate<Path> matcher) {
    if (root != null) {
      Path rootPath = Paths.get(root, new String[0]);
      Path currPath = file.toPath();
      try {
        while (!getRealPath(currPath).equals(getRealPath(rootPath))) {
          if (matcher.test(currPath))
            return true; 
          currPath = currPath.getParent();
          if (currPath == null)
            return false; 
        } 
      } catch (IOException ioe) {
        return false;
      } 
    } 
    return false;
  }
  
  public InputStream readFromOffset(long offset) throws IOException, InterruptedException {
    if (this.channel == null) {
      RandomAccessFile raf = new RandomAccessFile(new File(this.remote), "r");
      try {
        raf.seek(offset);
      } catch (IOException e) {
        try {
          raf.close();
        } catch (IOException iOException) {}
        throw e;
      } 
      return new Object(this, raf);
    } 
    Pipe p = Pipe.createRemoteToLocal();
    actAsync(new OffsetPipeSecureFileCallable(p, offset));
    return new GZIPInputStream(p.getIn());
  }
  
  public String readToString() { return (String)act(new ReadToString()); }
  
  public OutputStream write() throws IOException, InterruptedException {
    if (this.channel == null) {
      File f = (new File(this.remote)).getAbsoluteFile();
      mkdirs(f.getParentFile());
      return Files.newOutputStream(Util.fileToPath(f), new OpenOption[0]);
    } 
    return (OutputStream)act(new WritePipe());
  }
  
  public void write(String content, String encoding) throws IOException, InterruptedException { act(new Write(encoding, content)); }
  
  public String digest() { return (String)act(new Digest()); }
  
  public void renameTo(FilePath target) throws IOException, InterruptedException {
    if (this.channel != target.channel)
      throw new IOException("renameTo target must be on the same host"); 
    act(new RenameTo(target));
  }
  
  public void moveAllChildrenTo(FilePath target) throws IOException, InterruptedException {
    if (this.channel != target.channel)
      throw new IOException("pullUpTo target must be on the same host"); 
    act(new MoveAllChildrenTo(target));
  }
  
  public void copyTo(FilePath target) throws IOException, InterruptedException {
    try {
      OutputStream out = target.write();
      try {
        copyTo(out);
        if (out != null)
          out.close(); 
      } catch (Throwable throwable) {
        if (out != null)
          try {
            out.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException e) {
      throw new IOException("Failed to copy " + this + " to " + target, e);
    } 
  }
  
  public void copyToWithPermission(FilePath target) throws IOException, InterruptedException {
    if (this.channel == target.channel) {
      act(new CopyToWithPermission(target));
      return;
    } 
    copyTo(target);
    target.chmod(mode());
    target.setLastModifiedIfPossible(lastModified());
  }
  
  public void copyTo(OutputStream os) throws IOException, InterruptedException {
    RemoteOutputStream remoteOutputStream = new RemoteOutputStream(os);
    act(new CopyTo(remoteOutputStream));
    syncIO();
  }
  
  private void syncIO() throws IOException, InterruptedException {
    try {
      if (this.channel != null)
        this.channel.syncLocalIO(); 
    } catch (AbstractMethodError e) {
      try {
        LOGGER.log(Level.WARNING, "Looks like an old agent.jar. Please update " + Which.jarFile(Channel.class) + " to the new version", e);
      } catch (IOException iOException) {}
    } 
  }
  
  private void _syncIO() throws IOException, InterruptedException { this.channel.syncLocalIO(); }
  
  public int copyRecursiveTo(FilePath target) throws IOException, InterruptedException { return copyRecursiveTo("**/*", target); }
  
  public int copyRecursiveTo(String fileMask, FilePath target) throws IOException, InterruptedException { return copyRecursiveTo(fileMask, null, target); }
  
  public int copyRecursiveTo(String fileMask, String excludes, FilePath target) throws IOException, InterruptedException { return copyRecursiveTo(new DirScanner.Glob(fileMask, excludes), target, fileMask); }
  
  public int copyRecursiveTo(DirScanner scanner, FilePath target, String description) throws IOException, InterruptedException { return copyRecursiveTo(scanner, target, description, TarCompression.GZIP); }
  
  public int copyRecursiveTo(DirScanner scanner, FilePath target, String description, @NonNull TarCompression compression) throws IOException, InterruptedException {
    if (this.channel == target.channel)
      return ((Integer)act(new CopyRecursiveLocal(target, scanner))).intValue(); 
    if (this.channel == null) {
      Pipe pipe = Pipe.createLocalToRemote();
      Future<Void> future = target.actAsync(new ReadFromTar(target, pipe, description, compression));
      Future<Integer> future2 = actAsync(new WriteToTar(scanner, pipe, compression));
      try {
        future.get();
        return ((Integer)future2.get()).intValue();
      } catch (ExecutionException e) {
        throw ioWithCause(e);
      } 
    } 
    Pipe pipe = Pipe.createRemoteToLocal();
    Future<Integer> future = actAsync(new CopyRecursiveRemoteToLocal(pipe, scanner, compression));
    try {
      readFromTar(this.remote + "/" + this.remote, new File(target.remote), compression.extract(pipe.getIn()));
    } catch (IOException e) {
      try {
        future.get(3L, TimeUnit.SECONDS);
        throw e;
      } catch (ExecutionException x) {
        e.addSuppressed(x);
        throw e;
      } catch (TimeoutException ignored) {
        throw e;
      } 
    } 
    try {
      return ((Integer)future.get()).intValue();
    } catch (ExecutionException e) {
      throw ioWithCause(e);
    } 
  }
  
  private IOException ioWithCause(ExecutionException e) {
    Throwable cause = e.getCause();
    if (cause == null)
      cause = e; 
    return (cause instanceof IOException) ? 
      (IOException)cause : 
      new IOException(cause);
  }
  
  public int tar(OutputStream out, String glob) throws IOException, InterruptedException { return archive(ArchiverFactory.TAR, out, glob); }
  
  public int tar(OutputStream out, FileFilter filter) throws IOException, InterruptedException { return archive(ArchiverFactory.TAR, out, filter); }
  
  public int tar(OutputStream out, DirScanner scanner) throws IOException, InterruptedException { return archive(ArchiverFactory.TAR, out, scanner); }
  
  private static Integer writeToTar(File baseDir, DirScanner scanner, OutputStream out) throws IOException {
    Archiver tw = ArchiverFactory.TAR.create(out);
    Archiver archiver = tw;
    try {
      scanner.scan(baseDir, tw);
      if (archiver != null)
        archiver.close(); 
    } catch (Throwable throwable) {
      if (archiver != null)
        try {
          archiver.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    return Integer.valueOf(tw.countEntries());
  }
  
  private static void readFromTar(String name, File baseDir, InputStream in) throws IOException {
    try {
      TarArchiveInputStream t = new TarArchiveInputStream(in);
      try {
        TarArchiveEntry te;
        while ((te = t.getNextTarEntry()) != null) {
          File f = new File(baseDir, te.getName());
          if (!f.toPath().normalize().startsWith(baseDir.toPath()))
            throw new IOException("Tar " + name + " contains illegal file name that breaks out of the target directory: " + te
                .getName()); 
          if (te.isDirectory()) {
            mkdirs(f);
            continue;
          } 
          File parent = f.getParentFile();
          if (parent != null)
            mkdirs(parent); 
          if (te.isSymbolicLink()) {
            (new FilePath(f)).symlinkTo(te.getLinkName(), TaskListener.NULL);
            continue;
          } 
          IOUtils.copy(t, f);
          Files.setLastModifiedTime(Util.fileToPath(f), FileTime.from(te.getModTime().toInstant()));
          int mode = te.getMode() & 0x1FF;
          if (mode != 0 && !Functions.isWindows())
            _chmod(f, mode); 
        } 
        t.close();
      } catch (Throwable throwable) {
        try {
          t.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException e) {
      throw new IOException("Failed to extract " + name, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Failed to extract " + name, e);
    } 
  }
  
  public Launcher createLauncher(TaskListener listener) throws IOException, InterruptedException {
    if (this.channel == null)
      return new Launcher.LocalLauncher(listener); 
    return new Launcher.RemoteLauncher(listener, this.channel, ((Boolean)this.channel.call(new IsUnix())).booleanValue());
  }
  
  @Deprecated
  public String validateAntFileMask(String fileMasks) { return validateAntFileMask(fileMasks, 2147483647); }
  
  public String validateAntFileMask(String fileMasks, int bound) throws IOException, InterruptedException { return validateAntFileMask(fileMasks, bound, true); }
  
  public String validateAntFileMask(String fileMasks, boolean caseSensitive) throws IOException, InterruptedException { return validateAntFileMask(fileMasks, VALIDATE_ANT_FILE_MASK_BOUND, caseSensitive); }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static int VALIDATE_ANT_FILE_MASK_BOUND = SystemProperties.getInteger(FilePath.class.getName() + ".VALIDATE_ANT_FILE_MASK_BOUND", Integer.valueOf(10000)).intValue();
  
  @CheckForNull
  public String validateAntFileMask(String fileMasks, int bound, boolean caseSensitive) throws IOException, InterruptedException { return (String)act(new ValidateAntFileMask(fileMasks, caseSensitive, bound)); }
  
  private static final UrlFactory DEFAULT_URL_FACTORY = new UrlFactory();
  
  private UrlFactory urlFactory;
  
  private static final long serialVersionUID = 1L;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.328")
  public static final int SIDE_BUFFER_SIZE = 1024;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @VisibleForTesting
  void setUrlFactory(UrlFactory urlFactory) { this.urlFactory = urlFactory; }
  
  private UrlFactory getUrlFactory() {
    if (this.urlFactory != null)
      return this.urlFactory; 
    return DEFAULT_URL_FACTORY;
  }
  
  public static FormValidation validateFileMask(@CheckForNull FilePath path, String value) throws IOException { return validateFileMask(path, value, true); }
  
  public static FormValidation validateFileMask(@CheckForNull FilePath path, String value, boolean caseSensitive) throws IOException {
    if (path == null)
      return FormValidation.ok(); 
    return path.validateFileMask(value, true, caseSensitive);
  }
  
  public FormValidation validateFileMask(String value) throws IOException { return validateFileMask(value, true, true); }
  
  public FormValidation validateFileMask(String value, boolean errorIfNotExist) throws IOException { return validateFileMask(value, errorIfNotExist, true); }
  
  public FormValidation validateFileMask(String value, boolean errorIfNotExist, boolean caseSensitive) throws IOException {
    checkPermissionForValidate();
    value = Util.fixEmpty(value);
    if (value == null)
      return FormValidation.ok(); 
    try {
      if (!exists())
        return FormValidation.ok(); 
      String msg = validateAntFileMask(value, VALIDATE_ANT_FILE_MASK_BOUND, caseSensitive);
      if (errorIfNotExist)
        return FormValidation.error(msg); 
      return FormValidation.warning(msg);
    } catch (InterruptedException e) {
      return FormValidation.ok(Messages.FilePath_did_not_manage_to_validate_may_be_too_sl(value));
    } 
  }
  
  public FormValidation validateRelativePath(String value, boolean errorIfNotExist, boolean expectingFile) throws IOException {
    checkPermissionForValidate();
    value = Util.fixEmpty(value);
    if (value == null)
      return FormValidation.ok(); 
    if (value.contains("*"))
      return FormValidation.error(Messages.FilePath_validateRelativePath_wildcardNotAllowed()); 
    try {
      if (!exists())
        return FormValidation.ok(); 
      FilePath path = child(value);
      if (path.exists()) {
        if (expectingFile) {
          if (!path.isDirectory())
            return FormValidation.ok(); 
          return FormValidation.error(Messages.FilePath_validateRelativePath_notFile(value));
        } 
        if (path.isDirectory())
          return FormValidation.ok(); 
        return FormValidation.error(Messages.FilePath_validateRelativePath_notDirectory(value));
      } 
      String msg = expectingFile ? Messages.FilePath_validateRelativePath_noSuchFile(value) : Messages.FilePath_validateRelativePath_noSuchDirectory(value);
      if (errorIfNotExist)
        return FormValidation.error(msg); 
      return FormValidation.warning(msg);
    } catch (InterruptedException e) {
      return FormValidation.ok();
    } 
  }
  
  private static void checkPermissionForValidate() throws IOException, InterruptedException {
    subject = (AccessControlled)Stapler.getCurrentRequest().findAncestorObject(hudson.model.AbstractProject.class);
    if (subject == null) {
      Jenkins.get().checkPermission(Jenkins.MANAGE);
    } else {
      subject.checkPermission(Item.CONFIGURE);
    } 
  }
  
  public FormValidation validateRelativeDirectory(String value, boolean errorIfNotExist) throws IOException { return validateRelativePath(value, errorIfNotExist, false); }
  
  public FormValidation validateRelativeDirectory(String value) throws IOException { return validateRelativeDirectory(value, true); }
  
  @Deprecated
  public String toString() { return this.remote; }
  
  public VirtualChannel getChannel() {
    if (this.channel != null)
      return this.channel; 
    return localChannel;
  }
  
  public boolean isRemote() { return (this.channel != null); }
  
  private void writeObject(ObjectOutputStream oos) throws IOException {
    Channel target = _getChannelForSerialization();
    if (this.channel != null && this.channel != target)
      throw new IllegalStateException("Can't send a remote FilePath to a different remote channel (current=" + this.channel + ", target=" + target + ")"); 
    oos.defaultWriteObject();
    oos.writeBoolean((this.channel == null));
  }
  
  private Channel _getChannelForSerialization() {
    try {
      return getChannelForSerialization();
    } catch (NotSerializableException x) {
      LOGGER.log(Level.WARNING, "A FilePath object is being serialized when it should not be, indicating a bug in a plugin. See https://www.jenkins.io/redirect/filepath-serialization for details.", x);
      return null;
    } 
  }
  
  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    Channel channel = _getChannelForSerialization();
    ois.defaultReadObject();
    if (ois.readBoolean()) {
      this.channel = channel;
    } else {
      this.channel = null;
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(FilePath.class.getName());
  
  private static final Comparator<String> SHORTER_STRING_FIRST = Comparator.comparingInt(String::length);
  
  public static FilePath getHomeDirectory(VirtualChannel ch) throws InterruptedException, IOException { return (FilePath)ch.call(new GetHomeDirectory()); }
  
  private static final ExecutorService threadPoolForRemoting = new ContextResettingExecutorService(
      Executors.newCachedThreadPool(new ExceptionCatchingThreadFactory(new NamingThreadFactory(new DaemonThreadFactory(), "FilePath.localPool"))));
  
  @NonNull
  public static final LocalChannel localChannel = new LocalChannel(threadPoolForRemoting);
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static FileVisitor ignoringSymlinks(FileVisitor v, String verificationRoot, OpenOption... openOptions) { return validatingVisitor(FilePath::isNoFollowLink, visitorInfo -> 
        !isSymlink(visitorInfo), v, verificationRoot, openOptions); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static FileVisitor ignoringTmpDirs(FileVisitor v, String verificationRoot, OpenOption... openOptions) { return validatingVisitor(FilePath::isIgnoreTmpDirs, visitorInfo -> 
        !isTmpDir(visitorInfo), v, verificationRoot, openOptions); }
  
  private static FileVisitor validatingVisitor(Predicate<OpenOption[]> gater, Predicate<VisitorInfo> matcher, FileVisitor v, String verificationRoot, OpenOption... openOptions) {
    if (gater.test(openOptions))
      return new Object(verificationRoot, openOptions, matcher, v); 
    return v;
  }
  
  private static boolean mkdirs(File dir) {
    if (dir.exists())
      return false; 
    Files.createDirectories(Util.fileToPath(dir), new java.nio.file.attribute.FileAttribute[0]);
    return true;
  }
  
  private static File mkdirsE(File dir) throws IOException {
    if (dir.exists())
      return dir; 
    return IOUtils.mkdirs(dir);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isDescendant(@NonNull String potentialChildRelativePath) { return ((Boolean)act(new IsDescendant(potentialChildRelativePath))).booleanValue(); }
  
  private static Path getRealPath(Path path) throws IOException { return Functions.isWindows() ? windowsToRealPath(path) : path.toRealPath(new LinkOption[0]); }
  
  @NonNull
  private static Path windowsToRealPath(@NonNull Path path) throws IOException {
    try {
      return path.toRealPath(new LinkOption[0]);
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.log(Level.FINE, String.format("relaxedToRealPath cannot use the regular toRealPath on %s, trying with toRealPath(LinkOption.NOFOLLOW_LINKS)", new Object[] { path }), e); 
      return path.toRealPath(new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
    } 
  }
}
