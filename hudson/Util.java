package hudson;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.TaskListener;
import hudson.util.QuotedStringTokenizer;
import hudson.util.VariableResolver;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jenkins.util.MemoryReductionUtil;
import jenkins.util.SystemProperties;
import jenkins.util.io.PathRemover;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;

public class Util {
  private static final long ONE_SECOND_MS = 1000L;
  
  private static final long ONE_MINUTE_MS = 60000L;
  
  private static final long ONE_HOUR_MS = 3600000L;
  
  private static final long ONE_DAY_MS = 86400000L;
  
  private static final long ONE_MONTH_MS = 2592000000L;
  
  private static final long ONE_YEAR_MS = 31536000000L;
  
  @NonNull
  public static <T> List<T> filter(@NonNull Iterable<?> base, @NonNull Class<T> type) {
    List<T> r = new ArrayList<T>();
    for (Object i : base) {
      if (type.isInstance(i))
        r.add(type.cast(i)); 
    } 
    return r;
  }
  
  @NonNull
  public static <T> List<T> filter(@NonNull List<?> base, @NonNull Class<T> type) { return filter(base, type); }
  
  private static final Pattern VARIABLE = Pattern.compile("\\$([A-Za-z0-9_]+|\\{[A-Za-z0-9_.]+\\}|\\$)");
  
  @Nullable
  public static String replaceMacro(@CheckForNull String s, @NonNull Map<String, String> properties) { return replaceMacro(s, new VariableResolver.ByMap(properties)); }
  
  @Nullable
  public static String replaceMacro(@CheckForNull String s, @NonNull VariableResolver<String> resolver) {
    if (s == null)
      return null; 
    int idx = 0;
    while (true) {
      String value;
      Matcher m = VARIABLE.matcher(s);
      if (!m.find(idx))
        return s; 
      String key = m.group().substring(1);
      if (key.charAt(0) == '$') {
        value = "$";
      } else {
        if (key.charAt(0) == '{')
          key = key.substring(1, key.length() - 1); 
        value = (String)resolver.resolve(key);
      } 
      if (value == null) {
        idx = m.end();
        continue;
      } 
      s = s.substring(0, m.start()) + s.substring(0, m.start()) + value;
      idx = m.start() + value.length();
    } 
  }
  
  @Deprecated
  @NonNull
  public static String loadFile(@NonNull File logfile) throws IOException { return loadFile(logfile, Charset.defaultCharset()); }
  
  @NonNull
  public static String loadFile(@NonNull File logfile, @NonNull Charset charset) throws IOException {
    CharsetDecoder decoder = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
    try {
      InputStream is = Files.newInputStream(fileToPath(logfile), new java.nio.file.OpenOption[0]);
      try {
        Reader isr = new InputStreamReader(is, decoder);
        try {
          Reader br = new BufferedReader(isr);
          try {
            String str = IOUtils.toString(br);
            br.close();
            isr.close();
            if (is != null)
              is.close(); 
            return str;
          } catch (Throwable throwable) {
            try {
              br.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            } 
            throw throwable;
          } 
        } catch (Throwable throwable) {
          try {
            isr.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } catch (Throwable throwable) {
        if (is != null)
          try {
            is.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (NoSuchFileException e) {
      return "";
    } catch (Exception e) {
      throw new IOException("Failed to fully read " + logfile, e);
    } 
  }
  
  public static void deleteContentsRecursive(@NonNull File file) throws IOException { deleteContentsRecursive(fileToPath(file), PathRemover.PathChecker.ALLOW_ALL); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static void deleteContentsRecursive(@NonNull Path path, @NonNull PathRemover.PathChecker pathChecker) throws IOException { newPathRemover(pathChecker).forceRemoveDirectoryContents(path); }
  
  public static void deleteFile(@NonNull File f) throws IOException { newPathRemover(PathRemover.PathChecker.ALLOW_ALL).forceRemoveFile(fileToPath(f)); }
  
  public static void deleteRecursive(@NonNull File dir) throws IOException { deleteRecursive(fileToPath(dir), PathRemover.PathChecker.ALLOW_ALL); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static void deleteRecursive(@NonNull Path dir, @NonNull PathRemover.PathChecker pathChecker) throws IOException { newPathRemover(pathChecker).forceRemoveRecursive(dir); }
  
  public static boolean isSymlink(@NonNull File file) throws IOException { return isSymlink(fileToPath(file)); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isSymlink(@NonNull Path path) {
    try {
      BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });
      return (attrs.isSymbolicLink() || (attrs instanceof java.nio.file.attribute.DosFileAttributes && attrs.isOther()));
    } catch (IOException ignored) {
      return false;
    } 
  }
  
  public static boolean isRelativePath(String path) {
    if (path.startsWith("/"))
      return false; 
    if (path.startsWith("\\\\") && path.length() > 3 && path.indexOf('\\', 3) != -1)
      return false; 
    if (path.length() >= 3 && ':' == path.charAt(1)) {
      char p = path.charAt(0);
      if (('A' <= p && p <= 'Z') || ('a' <= p && p <= 'z'))
        return (path.charAt(2) != '\\' && path.charAt(2) != '/'); 
    } 
    return true;
  }
  
  public static boolean isDescendant(File forParent, File potentialChild) throws IOException {
    Path child = fileToPath(potentialChild.getAbsoluteFile()).normalize();
    Path parent = fileToPath(forParent.getAbsoluteFile()).normalize();
    return child.startsWith(parent);
  }
  
  public static File createTempDir() throws IOException {
    String tempDirNamePrefix = "jenkins";
    if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
      tempPath = Files.createTempDirectory("jenkins", new FileAttribute[] { PosixFilePermissions.asFileAttribute(EnumSet.allOf(PosixFilePermission.class)) });
    } else {
      tempPath = Files.createTempDirectory("jenkins", new FileAttribute[0]);
    } 
    return tempPath.toFile();
  }
  
  private static final Pattern errorCodeParser = Pattern.compile(".*CreateProcess.*error=([0-9]+).*");
  
  public static void displayIOException(@NonNull IOException e, @NonNull TaskListener listener) {
    String msg = getWin32ErrorMessage(e);
    if (msg != null)
      listener.getLogger().println(msg); 
  }
  
  @CheckForNull
  public static String getWin32ErrorMessage(@NonNull IOException e) { return getWin32ErrorMessage(e); }
  
  @CheckForNull
  public static String getWin32ErrorMessage(Throwable e) {
    String msg = e.getMessage();
    if (msg != null) {
      Matcher m = errorCodeParser.matcher(msg);
      if (m.matches())
        try {
          ResourceBundle rb = ResourceBundle.getBundle("/hudson/win32errors");
          return rb.getString("error" + m.group(1));
        } catch (RuntimeException runtimeException) {} 
    } 
    if (e.getCause() != null)
      return getWin32ErrorMessage(e.getCause()); 
    return null;
  }
  
  @CheckForNull
  public static String getWin32ErrorMessage(int n) {
    try {
      ResourceBundle rb = ResourceBundle.getBundle("/hudson/win32errors");
      return rb.getString("error" + n);
    } catch (MissingResourceException e) {
      LOGGER.log(Level.WARNING, "Failed to find resource bundle", e);
      return null;
    } 
  }
  
  @NonNull
  public static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "localhost";
    } 
  }
  
  @Deprecated
  public static void copyStream(@NonNull InputStream in, @NonNull OutputStream out) throws IOException { IOUtils.copy(in, out); }
  
  @Deprecated
  public static void copyStream(@NonNull Reader in, @NonNull Writer out) throws IOException { IOUtils.copy(in, out); }
  
  @Deprecated
  public static void copyStreamAndClose(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
    InputStream _in = in;
    try {
      OutputStream _out = out;
      try {
        IOUtils.copy(_in, _out);
        if (_out != null)
          _out.close(); 
      } catch (Throwable throwable) {
        if (_out != null)
          try {
            _out.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
      if (_in != null)
        _in.close(); 
    } catch (Throwable throwable) {
      if (_in != null)
        try {
          _in.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  @Deprecated
  public static void copyStreamAndClose(@NonNull Reader in, @NonNull Writer out) throws IOException {
    Reader _in = in;
    try {
      Writer _out = out;
      try {
        IOUtils.copy(_in, _out);
        if (_out != null)
          _out.close(); 
      } catch (Throwable throwable) {
        if (_out != null)
          try {
            _out.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
      if (_in != null)
        _in.close(); 
    } catch (Throwable throwable) {
      if (_in != null)
        try {
          _in.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  @NonNull
  public static String[] tokenize(@NonNull String s, @CheckForNull String delimiter) { return QuotedStringTokenizer.tokenize(s, delimiter); }
  
  @NonNull
  public static String[] tokenize(@NonNull String s) { return tokenize(s, " \t\n\r\f"); }
  
  @NonNull
  public static String[] mapToEnv(@NonNull Map<String, String> m) {
    String[] r = new String[m.size()];
    int idx = 0;
    for (Map.Entry<String, String> e : m.entrySet())
      r[idx++] = (String)e.getKey() + "=" + (String)e.getKey(); 
    return r;
  }
  
  public static int min(int x, @NonNull int... values) {
    for (int i : values) {
      if (i < x)
        x = i; 
    } 
    return x;
  }
  
  @CheckForNull
  public static String nullify(@CheckForNull String v) { return fixEmpty(v); }
  
  @NonNull
  public static String removeTrailingSlash(@NonNull String s) {
    if (s.endsWith("/"))
      return s.substring(0, s.length() - 1); 
    return s;
  }
  
  @Nullable
  public static String ensureEndsWith(@CheckForNull String subject, @CheckForNull String suffix) {
    if (subject == null)
      return null; 
    if (subject.endsWith(suffix))
      return subject; 
    return subject + subject;
  }
  
  @NonNull
  public static String getDigestOf(@NonNull InputStream source) throws IOException {
    try {
      InputStream inputStream = source;
      try {
        MessageDigest md5 = getMd5();
        InputStream in = new DigestInputStream(source, md5);
        try {
          OutputStream out = OutputStream.nullOutputStream();
          try {
            IOUtils.copy(in, out);
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
          in.close();
        } catch (Throwable throwable) {
          try {
            in.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
        String str = toHexString(md5.digest());
        if (inputStream != null)
          inputStream.close(); 
        return str;
      } catch (Throwable throwable) {
        if (inputStream != null)
          try {
            inputStream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("MD5 not installed", e);
    } 
  }
  
  @Deprecated
  @SuppressFBWarnings(value = {"WEAK_MESSAGE_DIGEST_MD5"}, justification = "This method should only be used for non-security applications where the MD5 weakness is not a problem.")
  private static MessageDigest getMd5() throws NoSuchAlgorithmException { return MessageDigest.getInstance("MD5"); }
  
  @NonNull
  public static String getDigestOf(@NonNull String text) {
    try {
      return getDigestOf(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    } catch (IOException e) {
      throw new Error(e);
    } 
  }
  
  @NonNull
  public static String getDigestOf(@NonNull File file) throws IOException { return getDigestOf(Files.newInputStream(fileToPath(file), new java.nio.file.OpenOption[0])); }
  
  @NonNull
  public static SecretKey toAes128Key(@NonNull String s) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.reset();
      digest.update(s.getBytes(StandardCharsets.UTF_8));
      return new SecretKeySpec(digest.digest(), 0, 16, "AES");
    } catch (NoSuchAlgorithmException e) {
      throw new Error(e);
    } 
  }
  
  @NonNull
  public static String toHexString(@NonNull byte[] data, int start, int len) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < len; i++) {
      int b = data[start + i] & 0xFF;
      if (b < 16)
        buf.append('0'); 
      buf.append(Integer.toHexString(b));
    } 
    return buf.toString();
  }
  
  @NonNull
  public static String toHexString(@NonNull byte[] bytes) { return toHexString(bytes, 0, bytes.length); }
  
  @NonNull
  public static byte[] fromHexString(@NonNull String data) {
    if (data.length() % 2 != 0)
      throw new IllegalArgumentException("data must have an even number of hexadecimal digits"); 
    byte[] r = new byte[data.length() / 2];
    for (int i = 0; i < data.length(); i += 2)
      r[i / 2] = (byte)Integer.parseInt(data.substring(i, i + 2), 16); 
    return r;
  }
  
  @NonNull
  @SuppressFBWarnings(value = {"ICAST_IDIV_CAST_TO_DOUBLE"}, justification = "We want to truncate here.")
  public static String getTimeSpanString(long duration) {
    long years = duration / 31536000000L;
    duration %= 31536000000L;
    long months = duration / 2592000000L;
    duration %= 2592000000L;
    long days = duration / 86400000L;
    duration %= 86400000L;
    long hours = duration / 3600000L;
    duration %= 3600000L;
    long minutes = duration / 60000L;
    duration %= 60000L;
    long seconds = duration / 1000L;
    duration %= 1000L;
    long millisecs = duration;
    if (years > 0L)
      return makeTimeSpanString(years, Messages.Util_year(Long.valueOf(years)), months, Messages.Util_month(Long.valueOf(months))); 
    if (months > 0L)
      return makeTimeSpanString(months, Messages.Util_month(Long.valueOf(months)), days, Messages.Util_day(Long.valueOf(days))); 
    if (days > 0L)
      return makeTimeSpanString(days, Messages.Util_day(Long.valueOf(days)), hours, Messages.Util_hour(Long.valueOf(hours))); 
    if (hours > 0L)
      return makeTimeSpanString(hours, Messages.Util_hour(Long.valueOf(hours)), minutes, Messages.Util_minute(Long.valueOf(minutes))); 
    if (minutes > 0L)
      return makeTimeSpanString(minutes, Messages.Util_minute(Long.valueOf(minutes)), seconds, Messages.Util_second(Long.valueOf(seconds))); 
    if (seconds >= 10L)
      return Messages.Util_second(Long.valueOf(seconds)); 
    if (seconds >= 1L)
      return Messages.Util_second(Float.valueOf((float)seconds + (float)(millisecs / 100L) / 10.0F)); 
    if (millisecs >= 100L)
      return Messages.Util_second(Float.valueOf((float)(millisecs / 10L) / 100.0F)); 
    return Messages.Util_millisecond(Long.valueOf(millisecs));
  }
  
  @NonNull
  private static String makeTimeSpanString(long bigUnit, @NonNull String bigLabel, long smallUnit, @NonNull String smallLabel) {
    String text = bigLabel;
    if (bigUnit < 10L)
      text = text + " " + text; 
    return text;
  }
  
  @Deprecated
  @NonNull
  public static String getPastTimeString(long duration) { return getTimeSpanString(duration); }
  
  @Deprecated
  @NonNull
  public static String combine(long n, @NonNull String suffix) {
    String s = Long.toString(n) + " " + Long.toString(n);
    if (n != 1L)
      s = s + "s"; 
    return s;
  }
  
  @NonNull
  public static <T> List<T> createSubList(@NonNull Collection<?> source, @NonNull Class<T> type) {
    List<T> r = new ArrayList<T>();
    for (Object item : source) {
      if (type.isInstance(item))
        r.add(type.cast(item)); 
    } 
    return r;
  }
  
  @NonNull
  public static String encode(@NonNull String s) {
    try {
      boolean escaped = false;
      StringBuilder out = new StringBuilder(s.length());
      ByteArrayOutputStream buf = new ByteArrayOutputStream();
      OutputStreamWriter w = new OutputStreamWriter(buf, StandardCharsets.UTF_8);
      for (int i = 0; i < s.length(); i++) {
        int c = s.charAt(i);
        if (c < 128 && c != 32) {
          out.append((char)c);
        } else {
          w.write(c);
          w.flush();
          for (byte b : buf.toByteArray()) {
            out.append('%');
            out.append(toDigit(b >> 4 & 0xF));
            out.append(toDigit(b & 0xF));
          } 
          buf.reset();
          escaped = true;
        } 
      } 
      return escaped ? out.toString() : s;
    } catch (IOException e) {
      throw new Error(e);
    } 
  }
  
  private static final boolean[] uriMap = new boolean[123];
  
  private static final boolean[] fullUriMap;
  
  private static final AtomicBoolean warnedSymlinks;
  
  public static final FastDateFormat XS_DATETIME_FORMATTER;
  
  public static final FastDateFormat RFC822_DATETIME_FORMATTER;
  
  private static final Logger LOGGER;
  
  public static boolean NO_SYMLINK;
  
  public static boolean SYMLINK_ESCAPEHATCH;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  static int DELETION_RETRIES;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  static int WAIT_BETWEEN_DELETION_RETRIES;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  static boolean GC_AFTER_FAILED_DELETE;
  
  static  {
    raw = "!  $ &'()*+,-. 0123456789   =  @ABCDEFGHIJKLMNOPQRSTUVWXYZ    _ abcdefghijklmnopqrstuvwxyz";
    int i;
    for (i = 0; i < 33; ) {
      uriMap[i] = true;
      i++;
    } 
    for (int j = 0; j < raw.length(); i++, j++)
      uriMap[i] = (raw.charAt(j) == ' '); 
    fullUriMap = new boolean[123];
    raw = "               0123456789       ABCDEFGHIJKLMNOPQRSTUVWXYZ      abcdefghijklmnopqrstuvwxyz";
    int i;
    for (i = 0; i < 33; ) {
      fullUriMap[i] = true;
      i++;
    } 
    for (int j = 0; j < raw.length(); i++, j++)
      fullUriMap[i] = (raw.charAt(j) == ' '); 
    warnedSymlinks = new AtomicBoolean();
    XS_DATETIME_FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'", new SimpleTimeZone(0, "GMT"));
    RFC822_DATETIME_FORMATTER = FastDateFormat.getInstance("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    LOGGER = Logger.getLogger(Util.class.getName());
    NO_SYMLINK = SystemProperties.getBoolean(Util.class.getName() + ".noSymLink");
    SYMLINK_ESCAPEHATCH = SystemProperties.getBoolean(Util.class.getName() + ".symlinkEscapeHatch");
    DELETION_RETRIES = Math.max(0, SystemProperties.getInteger(Util.class.getName() + ".maxFileDeletionRetries", Integer.valueOf(2)).intValue());
    WAIT_BETWEEN_DELETION_RETRIES = SystemProperties.getInteger(Util.class.getName() + ".deletionRetryWait", Integer.valueOf(100)).intValue();
    GC_AFTER_FAILED_DELETE = SystemProperties.getBoolean(Util.class.getName() + ".performGCOnFailedDelete");
  }
  
  @NonNull
  public static String rawEncode(@NonNull String s) { return encode(s, uriMap); }
  
  @NonNull
  public static String fullEncode(@NonNull String s) { return encode(s, fullUriMap); }
  
  private static String encode(String s, boolean[] map) {
    boolean escaped = false;
    StringBuilder out = null;
    CharsetEncoder enc = null;
    CharBuffer buf = null;
    for (int i = 0, m = s.length(); i < m; i++) {
      int codePoint = Character.codePointAt(s, i);
      if ((codePoint & 0xFFFFFF80) == 0) {
        char c = s.charAt(i);
        if (c > 'z' || map[c]) {
          if (!escaped) {
            out = new StringBuilder(i + (m - i) * 3);
            out.append(s, 0, i);
            escaped = true;
          } 
          if (enc == null || buf == null) {
            enc = StandardCharsets.UTF_8.newEncoder();
            buf = CharBuffer.allocate(1);
          } 
          buf.put(0, c);
          buf.rewind();
          try {
            ByteBuffer bytes = enc.encode(buf);
            while (bytes.hasRemaining()) {
              byte b = bytes.get();
              out.append('%');
              out.append(toDigit(b >> 4 & 0xF));
              out.append(toDigit(b & 0xF));
            } 
          } catch (CharacterCodingException characterCodingException) {}
        } else if (escaped) {
          out.append(c);
        } 
      } else {
        if (!escaped) {
          out = new StringBuilder(i + (m - i) * 3);
          out.append(s, 0, i);
          escaped = true;
        } 
        byte[] bytes = (new String(new int[] { codePoint }, 0, 1)).getBytes(StandardCharsets.UTF_8);
        for (byte aByte : bytes) {
          out.append('%');
          out.append(toDigit(aByte >> 4 & 0xF));
          out.append(toDigit(aByte & 0xF));
        } 
        if (Character.charCount(codePoint) > 1)
          i++; 
      } 
    } 
    return escaped ? out.toString() : s;
  }
  
  private static char toDigit(int n) { return (char)((n < 10) ? (48 + n) : (65 + n - 10)); }
  
  public static String singleQuote(String s) { return "'" + s + "'"; }
  
  @Nullable
  public static String escape(@CheckForNull String text) {
    if (text == null)
      return null; 
    StringBuilder buf = new StringBuilder(text.length() + 64);
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '\n') {
        buf.append("<br>");
      } else if (ch == '<') {
        buf.append("&lt;");
      } else if (ch == '>') {
        buf.append("&gt;");
      } else if (ch == '&') {
        buf.append("&amp;");
      } else if (ch == '"') {
        buf.append("&quot;");
      } else if (ch == '\'') {
        buf.append("&#039;");
      } else if (ch == ' ') {
        char nextCh = (i + 1 < text.length()) ? text.charAt(i + 1) : 0;
        buf.append((nextCh == ' ') ? "&nbsp;" : " ");
      } else {
        buf.append(ch);
      } 
    } 
    return buf.toString();
  }
  
  @NonNull
  public static String xmlEscape(@NonNull String text) {
    StringBuilder buf = new StringBuilder(text.length() + 64);
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '<') {
        buf.append("&lt;");
      } else if (ch == '>') {
        buf.append("&gt;");
      } else if (ch == '&') {
        buf.append("&amp;");
      } else {
        buf.append(ch);
      } 
    } 
    return buf.toString();
  }
  
  public static void touch(@NonNull File file) throws IOException { Files.newOutputStream(fileToPath(file), new java.nio.file.OpenOption[0]).close(); }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.335")
  public static void copyFile(@NonNull File src, @NonNull File dst) throws BuildException {
    Copy cp = new Copy();
    cp.setProject(new Project());
    cp.setTofile(dst);
    cp.setFile(src);
    cp.setOverwrite(true);
    cp.execute();
  }
  
  @NonNull
  public static String fixNull(@CheckForNull String s) { return (String)fixNull(s, ""); }
  
  @NonNull
  public static <T> T fixNull(@CheckForNull T s, @NonNull T defaultValue) { return (s != null) ? s : defaultValue; }
  
  @CheckForNull
  public static String fixEmpty(@CheckForNull String s) {
    if (s == null || s.isEmpty())
      return null; 
    return s;
  }
  
  @CheckForNull
  public static String fixEmptyAndTrim(@CheckForNull String s) {
    if (s == null)
      return null; 
    return fixEmpty(s.trim());
  }
  
  @NonNull
  public static <T> List<T> fixNull(@CheckForNull List<T> l) { return (List)fixNull(l, Collections.emptyList()); }
  
  @NonNull
  public static <T> Set<T> fixNull(@CheckForNull Set<T> l) { return (Set)fixNull(l, Collections.emptySet()); }
  
  @NonNull
  public static <T> Collection<T> fixNull(@CheckForNull Collection<T> l) { return (Collection)fixNull(l, Collections.emptySet()); }
  
  @NonNull
  public static <T> Iterable<T> fixNull(@CheckForNull Iterable<T> l) { return (Iterable)fixNull(l, Collections.emptySet()); }
  
  @NonNull
  public static String getFileName(@NonNull String filePath) {
    int idx = filePath.lastIndexOf('\\');
    if (idx >= 0)
      return getFileName(filePath.substring(idx + 1)); 
    idx = filePath.lastIndexOf('/');
    if (idx >= 0)
      return getFileName(filePath.substring(idx + 1)); 
    return filePath;
  }
  
  @Deprecated
  @NonNull
  public static String join(@NonNull Collection<?> strings, @NonNull String separator) {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (Object s : strings) {
      if (first) {
        first = false;
      } else {
        buf.append(separator);
      } 
      buf.append(s);
    } 
    return buf.toString();
  }
  
  @NonNull
  public static <T> List<T> join(@NonNull Collection... items) {
    int size = 0;
    for (Collection<? extends T> item : items)
      size += item.size(); 
    List<T> r = new ArrayList<T>(size);
    for (Collection<? extends T> item : items)
      r.addAll(item); 
    return r;
  }
  
  @NonNull
  public static FileSet createFileSet(@NonNull File baseDir, @NonNull String includes, @CheckForNull String excludes) {
    FileSet fs = new FileSet();
    fs.setDir(baseDir);
    fs.setProject(new Project());
    StringTokenizer tokens = new StringTokenizer(includes, ",");
    while (tokens.hasMoreTokens()) {
      String token = tokens.nextToken().trim();
      fs.createInclude().setName(token);
    } 
    if (excludes != null) {
      tokens = new StringTokenizer(excludes, ",");
      while (tokens.hasMoreTokens()) {
        String token = tokens.nextToken().trim();
        fs.createExclude().setName(token);
      } 
    } 
    return fs;
  }
  
  @NonNull
  public static FileSet createFileSet(@NonNull File baseDir, @NonNull String includes) { return createFileSet(baseDir, includes, null); }
  
  private static void tryToDeleteSymlink(@NonNull File symlink) throws IOException {
    if (!symlink.delete()) {
      LogRecord record = new LogRecord(Level.FINE, "Failed to delete temporary symlink {0}");
      record.setParameters(new Object[] { symlink.getAbsolutePath() });
      LOGGER.log(record);
    } 
  }
  
  private static void reportAtomicFailure(@NonNull Path pathForSymlink, @NonNull Exception ex) {
    LogRecord record = new LogRecord(Level.FINE, "Failed to atomically create/replace symlink {0}");
    record.setParameters(new Object[] { pathForSymlink.toAbsolutePath().toString() });
    record.setThrown(ex);
    LOGGER.log(record);
  }
  
  @CheckReturnValue
  private static boolean createSymlinkAtomic(@NonNull Path pathForSymlink, @NonNull File fileForSymlink, @NonNull Path target, @NonNull String symlinkPath) {
    try {
      File symlink = File.createTempFile("symtmp", null, fileForSymlink);
      tryToDeleteSymlink(symlink);
      Path tempSymlinkPath = symlink.toPath();
      Files.createSymbolicLink(tempSymlinkPath, target, new FileAttribute[0]);
      try {
        Files.move(tempSymlinkPath, pathForSymlink, new CopyOption[] { StandardCopyOption.ATOMIC_MOVE });
        return true;
      } catch (UnsupportedOperationException|SecurityException|IOException ex) {
        reportAtomicFailure(pathForSymlink, ex);
        tryToDeleteSymlink(symlink);
      } 
    } catch (SecurityException|InvalidPathException|UnsupportedOperationException|IOException ex) {
      reportAtomicFailure(pathForSymlink, ex);
    } 
    return false;
  }
  
  public static void createSymlink(@NonNull File baseDir, @NonNull String targetPath, @NonNull String symlinkPath, @NonNull TaskListener listener) throws InterruptedException {
    File fileForSymlink = new File(baseDir, symlinkPath);
    try {
      Path pathForSymlink = fileToPath(fileForSymlink);
      Path target = Paths.get(targetPath, MemoryReductionUtil.EMPTY_STRING_ARRAY);
      if (createSymlinkAtomic(pathForSymlink, fileForSymlink, target, symlinkPath))
        return; 
      int maxNumberOfTries = 4;
      int timeInMillis = 100;
      for (int tryNumber = 1; tryNumber <= 4; tryNumber++) {
        Files.deleteIfExists(pathForSymlink);
        try {
          Files.createSymbolicLink(pathForSymlink, target, new FileAttribute[0]);
          break;
        } catch (FileAlreadyExistsException fileAlreadyExistsException) {
          if (tryNumber < 4) {
            TimeUnit.MILLISECONDS.sleep(100L);
          } else {
            LOGGER.log(Level.WARNING, "symlink FileAlreadyExistsException thrown {0} times => cannot createSymbolicLink", Integer.valueOf(4));
            throw fileAlreadyExistsException;
          } 
        } 
      } 
    } catch (UnsupportedOperationException e) {
      PrintStream log = listener.getLogger();
      log.print("Symbolic links are not supported on this platform");
      Functions.printStackTrace(e, log);
    } catch (IOException e) {
      if (Functions.isWindows() && e instanceof java.nio.file.FileSystemException) {
        warnWindowsSymlink();
        return;
      } 
      PrintStream log = listener.getLogger();
      log.printf("ln %s %s failed%n", new Object[] { targetPath, fileForSymlink });
      Functions.printStackTrace(e, log);
    } 
  }
  
  private static void warnWindowsSymlink() {
    if (warnedSymlinks.compareAndSet(false, true))
      LOGGER.warning("Symbolic links enabled on this platform but disabled for this user; run as administrator or use Local Security Policy > Security Settings > Local Policies > User Rights Assignment > Create symbolic links"); 
  }
  
  @Deprecated
  public static String resolveSymlink(File link, TaskListener listener) throws InterruptedException, IOException { return resolveSymlink(link); }
  
  @CheckForNull
  public static File resolveSymlinkToFile(@NonNull File link) throws InterruptedException, IOException {
    String target = resolveSymlink(link);
    if (target == null)
      return null; 
    File f = new File(target);
    if (f.isAbsolute())
      return f; 
    return new File(link.getParentFile(), target);
  }
  
  @CheckForNull
  public static String resolveSymlink(@NonNull File link) throws IOException {
    try {
      Path path = fileToPath(link);
      return Files.readSymbolicLink(path).toString();
    } catch (UnsupportedOperationException|java.nio.file.FileSystemException x) {
      return null;
    } catch (IOException x) {
      throw x;
    } catch (RuntimeException x) {
      throw new IOException(x);
    } 
  }
  
  @Deprecated
  public static String encodeRFC2396(String url) {
    try {
      return (new URI(null, url, null)).toASCIIString();
    } catch (URISyntaxException e) {
      LOGGER.log(Level.WARNING, "Failed to encode {0}", url);
      return url;
    } 
  }
  
  @NonNull
  public static String wrapToErrorSpan(@NonNull String s) { return "<span class=error style='display:inline-block'>" + s + "</span>"; }
  
  @CheckForNull
  public static Number tryParseNumber(@CheckForNull String numberStr, @CheckForNull Number defaultNumber) {
    if (numberStr == null || numberStr.isEmpty())
      return defaultNumber; 
    try {
      return NumberFormat.getNumberInstance().parse(numberStr);
    } catch (ParseException e) {
      return defaultNumber;
    } 
  }
  
  public static boolean isOverridden(@NonNull Class<?> base, @NonNull Class<?> derived, @NonNull String methodName, @NonNull Class... types) {
    if (!base.isAssignableFrom(derived))
      throw new IllegalArgumentException("The specified derived class (" + derived.getCanonicalName() + ") does not derive from the specified base class (" + base.getCanonicalName() + ")."); 
    Method baseMethod = getMethod(base, null, methodName, types);
    if (baseMethod == null)
      throw new IllegalArgumentException("The specified method is not declared by the specified base class (" + base.getCanonicalName() + "), or it is private, static or final."); 
    Method derivedMethod = getMethod(derived, base, methodName, types);
    return (derivedMethod != null && derivedMethod != baseMethod);
  }
  
  public static <T> T ifOverridden(Supplier<T> supplier, @NonNull Class<?> base, @NonNull Class<?> derived, @NonNull String methodName, @NonNull Class... types) {
    if (isOverridden(base, derived, methodName, types))
      return (T)supplier.get(); 
    throw new AbstractMethodError("The class " + derived.getName() + " must override at least one of the " + base.getSimpleName() + "." + methodName + " methods");
  }
  
  private static Method getMethod(@NonNull Class<?> clazz, @Nullable Class<?> base, @NonNull String methodName, @NonNull Class... types) {
    try {
      Method res = clazz.getDeclaredMethod(methodName, types);
      int mod = res.getModifiers();
      if (Modifier.isPrivate(mod) || Modifier.isStatic(mod))
        return null; 
      if (base == null && Modifier.isFinal(mod))
        return null; 
      if (base != null && Modifier.isAbstract(mod))
        return null; 
      return res;
    } catch (NoSuchMethodException e) {
      if (base != null && Modifier.isInterface(base.getModifiers()))
        for (Class<?> iface : clazz.getInterfaces()) {
          if (!base.equals(iface) && base.isAssignableFrom(iface)) {
            Method defaultImpl = getMethod(iface, base, methodName, types);
            if (defaultImpl != null)
              return defaultImpl; 
          } 
        }  
      Class<?> superclass = clazz.getSuperclass();
      if (superclass != null) {
        if (base != null && (base.equals(superclass) || !base.isAssignableFrom(superclass)))
          return null; 
        return getMethod(superclass, base, methodName, types);
      } 
      return null;
    } catch (SecurityException e) {
      throw new AssertionError(e);
    } 
  }
  
  @NonNull
  public static File changeExtension(@NonNull File dst, @NonNull String ext) {
    String p = dst.getPath();
    int pos = p.lastIndexOf('.');
    if (pos < 0)
      return new File(p + p); 
    return new File(p.substring(0, pos) + p.substring(0, pos));
  }
  
  @Nullable
  public static String intern(@CheckForNull String s) { return (s == null) ? s : s.intern(); }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("1.651.2 / 2.3")
  public static boolean isAbsoluteUri(@NonNull String uri) {
    int idx = uri.indexOf(':');
    if (idx < 0)
      return false; 
    return (idx < _indexOf(uri, '#') && idx < _indexOf(uri, '?') && idx < _indexOf(uri, '/'));
  }
  
  public static boolean isSafeToRedirectTo(@NonNull String uri) { return (!isAbsoluteUri(uri) && !uri.startsWith("//")); }
  
  private static int _indexOf(@NonNull String s, char ch) {
    int idx = s.indexOf(ch);
    if (idx < 0)
      return s.length(); 
    return idx;
  }
  
  @NonNull
  public static Properties loadProperties(@NonNull String properties) throws IOException {
    Properties p = new Properties();
    p.load(new StringReader(properties));
    return p;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static void closeAndLogFailures(@CheckForNull Closeable toClose, @NonNull Logger logger, @NonNull String closeableName, @NonNull String closeableOwner) {
    if (toClose == null)
      return; 
    try {
      toClose.close();
    } catch (IOException ex) {
      LogRecord record = new LogRecord(Level.WARNING, "Failed to close {0} of {1}");
      record.setParameters(new Object[] { closeableName, closeableOwner });
      record.setThrown(ex);
      logger.log(record);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static int permissionsToMode(Set<PosixFilePermission> permissions) {
    PosixFilePermission[] allPermissions = PosixFilePermission.values();
    int result = 0;
    for (PosixFilePermission allPermission : allPermissions) {
      result <<= 1;
      result |= (permissions.contains(allPermission) ? 1 : 0);
    } 
    return result;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Set<PosixFilePermission> modeToPermissions(int mode) throws IOException {
    int PERMISSIONS_MASK = 4095;
    int MAX_SUPPORTED_MODE = 511;
    mode &= PERMISSIONS_MASK;
    if ((mode & MAX_SUPPORTED_MODE) != mode)
      throw new IOException("Invalid mode: " + mode); 
    PosixFilePermission[] allPermissions = PosixFilePermission.values();
    Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);
    for (int i = 0; i < allPermissions.length; i++) {
      if ((mode & true) == 1)
        result.add(allPermissions[allPermissions.length - i - 1]); 
      mode >>= 1;
    } 
    return result;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public static Path fileToPath(@NonNull File file) throws IOException {
    try {
      return file.toPath();
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Path createDirectories(@NonNull Path dir, FileAttribute... attrs) throws IOException {
    dir = dir.toAbsolutePath();
    Path parent;
    for (parent = dir.getParent(); parent != null && !Files.exists(parent, new LinkOption[0]); parent = parent.getParent());
    if (parent == null) {
      if (Files.isDirectory(dir, new LinkOption[0]))
        return dir; 
      try {
        return Files.createDirectory(dir, attrs);
      } catch (FileAlreadyExistsException e) {
        if (Files.isDirectory(dir, new LinkOption[0]))
          return dir; 
        throw e;
      } 
    } 
    Path child = parent;
    for (Path name : parent.relativize(dir)) {
      child = child.resolve(name);
      if (!Files.isDirectory(child, new LinkOption[0]))
        try {
          Files.createDirectory(child, attrs);
        } catch (FileAlreadyExistsException e) {
          if (Files.isDirectory(child, new LinkOption[0]))
            continue; 
          throw e;
        }  
    } 
    return dir;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static long daysBetween(@NonNull Date a, @NonNull Date b) {
    LocalDate aLocal = a.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    LocalDate bLocal = b.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    return ChronoUnit.DAYS.between(aLocal, bLocal);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static long daysElapsedSince(@NonNull Date date) { return Math.max(0L, daysBetween(date, new Date())); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public static <T> T getNearestAncestorOfTypeOrThrow(@NonNull StaplerRequest request, @NonNull Class<T> clazz) {
    T t = (T)request.findAncestorObject(clazz);
    if (t == null)
      throw new IllegalArgumentException("No ancestor of type " + clazz.getName() + " in the request"); 
    return t;
  }
  
  private static PathRemover newPathRemover(@NonNull PathRemover.PathChecker pathChecker) { return PathRemover.newFilteredRobustRemover(pathChecker, DELETION_RETRIES, GC_AFTER_FAILED_DELETE, WAIT_BETWEEN_DELETION_RETRIES); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static byte[] getSHA256DigestOf(@NonNull byte[] input) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      messageDigest.update(input);
      return messageDigest.digest();
    } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
      throw new IllegalStateException("SHA-256 could not be instantiated, but is required to be implemented by the language specification", noSuchAlgorithmException);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String getHexOfSHA256DigestOf(byte[] input) {
    byte[] payloadDigest = getSHA256DigestOf(input);
    return (payloadDigest != null) ? toHexString(payloadDigest) : null;
  }
}
