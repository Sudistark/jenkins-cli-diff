package hudson.util;

import hudson.Functions;
import hudson.Util;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class IOUtils {
  private static final byte[] SKIP_BUFFER;
  
  public static void drain(InputStream in) throws IOException {
    InputStream inputStream = in;
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
  
  public static void copy(File src, OutputStream out) throws IOException {
    try {
      InputStream in = Files.newInputStream(src.toPath(), new java.nio.file.OpenOption[0]);
      try {
        IOUtils.copy(in, out);
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
  }
  
  public static void copy(InputStream in, File out) throws IOException {
    try {
      OutputStream fos = Files.newOutputStream(out.toPath(), new java.nio.file.OpenOption[0]);
      try {
        IOUtils.copy(in, fos);
        if (fos != null)
          fos.close(); 
      } catch (Throwable throwable) {
        if (fos != null)
          try {
            fos.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  public static File mkdirs(File dir) throws IOException {
    try {
      return Files.createDirectories(Util.fileToPath(dir), new java.nio.file.attribute.FileAttribute[0]).toFile();
    } catch (UnsupportedOperationException e) {
      throw new IOException(e);
    } 
  }
  
  @Deprecated
  public static InputStream skip(InputStream in, long size) throws IOException {
    DataInputStream di = new DataInputStream(in);
    while (size > 0L) {
      int chunk = (int)Math.min(SKIP_BUFFER.length, size);
      di.readFully(SKIP_BUFFER, 0, chunk);
      size -= chunk;
    } 
    return in;
  }
  
  public static File absolutize(File base, String path) {
    if (isAbsolute(path))
      return new File(path); 
    return new File(base, path);
  }
  
  public static boolean isAbsolute(String path) {
    Pattern DRIVE_PATTERN = Pattern.compile("[A-Za-z]:[\\\\/].*");
    return (path.startsWith("/") || DRIVE_PATTERN.matcher(path).matches());
  }
  
  public static int mode(File f) throws IOException {
    if (Functions.isWindows())
      return -1; 
    return Util.permissionsToMode(Files.getPosixFilePermissions(Util.fileToPath(f), new java.nio.file.LinkOption[0]));
  }
  
  public static String readFirstLine(InputStream is, String encoding) throws IOException {
    BufferedReader reader = new BufferedReader((encoding == null) ? new InputStreamReader(is, Charset.defaultCharset()) : new InputStreamReader(is, encoding));
    try {
      String str = reader.readLine();
      reader.close();
      return str;
    } catch (Throwable throwable) {
      try {
        reader.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  static  {
    buf = new StringWriter(4);
    PrintWriter out = new PrintWriter(buf);
    out.println();
    SKIP_BUFFER = new byte[8192];
  }
  
  @Deprecated
  public static void closeQuietly(Reader input) { IOUtils.closeQuietly(input); }
  
  @Deprecated
  public static void closeQuietly(Writer output) { IOUtils.closeQuietly(output); }
  
  @Deprecated
  public static void closeQuietly(InputStream input) throws IOException { IOUtils.closeQuietly(input); }
  
  @Deprecated
  public static void closeQuietly(OutputStream output) { IOUtils.closeQuietly(output); }
  
  @Deprecated
  public static byte[] toByteArray(InputStream input) throws IOException { return IOUtils.toByteArray(input); }
  
  @Deprecated
  public static byte[] toByteArray(Reader input) throws IOException { return IOUtils.toByteArray(input); }
  
  @Deprecated
  public static byte[] toByteArray(Reader input, String encoding) throws IOException { return IOUtils.toByteArray(input, encoding); }
  
  @Deprecated
  public static byte[] toByteArray(String input) throws IOException { return IOUtils.toByteArray(input); }
  
  @Deprecated
  public static char[] toCharArray(InputStream is) throws IOException { return IOUtils.toCharArray(is); }
  
  @Deprecated
  public static char[] toCharArray(InputStream is, String encoding) throws IOException { return IOUtils.toCharArray(is, encoding); }
  
  @Deprecated
  public static char[] toCharArray(Reader input) throws IOException { return IOUtils.toCharArray(input); }
  
  @Deprecated
  public static String toString(InputStream input) throws IOException { return IOUtils.toString(input); }
  
  @Deprecated
  public static String toString(InputStream input, String encoding) throws IOException { return IOUtils.toString(input, encoding); }
  
  @Deprecated
  public static String toString(Reader input) throws IOException { return IOUtils.toString(input); }
  
  @Deprecated
  public static String toString(byte[] input) throws IOException { return IOUtils.toString(input); }
  
  @Deprecated
  public static String toString(byte[] input, String encoding) throws IOException { return IOUtils.toString(input, encoding); }
  
  @Deprecated
  public static List readLines(InputStream input) throws IOException { return IOUtils.readLines(input); }
  
  @Deprecated
  public static List readLines(InputStream input, String encoding) throws IOException { return IOUtils.readLines(input, encoding); }
  
  @Deprecated
  public static List readLines(Reader input) throws IOException { return IOUtils.readLines(input); }
  
  @Deprecated
  public static LineIterator lineIterator(Reader reader) { return IOUtils.lineIterator(reader); }
  
  @Deprecated
  public static LineIterator lineIterator(InputStream input, String encoding) throws IOException { return IOUtils.lineIterator(input, encoding); }
  
  @Deprecated
  public static InputStream toInputStream(String input) { return IOUtils.toInputStream(input); }
  
  @Deprecated
  public static InputStream toInputStream(String input, String encoding) throws IOException { return IOUtils.toInputStream(input, encoding); }
  
  @Deprecated
  public static void write(byte[] data, OutputStream output) throws IOException { IOUtils.write(data, output); }
  
  @Deprecated
  public static void write(byte[] data, Writer output) throws IOException { IOUtils.write(data, output); }
  
  @Deprecated
  public static void write(byte[] data, Writer output, String encoding) throws IOException { IOUtils.write(data, output, encoding); }
  
  @Deprecated
  public static void write(char[] data, Writer output) throws IOException { IOUtils.write(data, output); }
  
  @Deprecated
  public static void write(char[] data, OutputStream output) throws IOException { IOUtils.write(data, output); }
  
  @Deprecated
  public static void write(char[] data, OutputStream output, String encoding) throws IOException { IOUtils.write(data, output, encoding); }
  
  @Deprecated
  public static void write(String data, Writer output) throws IOException { IOUtils.write(data, output); }
  
  @Deprecated
  public static void write(String data, OutputStream output) throws IOException { IOUtils.write(data, output); }
  
  @Deprecated
  public static void write(String data, OutputStream output, String encoding) throws IOException { IOUtils.write(data, output, encoding); }
  
  @Deprecated
  public static void write(StringBuffer data, Writer output) throws IOException { IOUtils.write(data, output); }
  
  @Deprecated
  public static void write(StringBuffer data, OutputStream output) throws IOException { IOUtils.write(data, output); }
  
  @Deprecated
  public static void write(StringBuffer data, OutputStream output, String encoding) throws IOException { IOUtils.write(data, output, encoding); }
  
  @Deprecated
  public static void writeLines(Collection lines, String lineEnding, OutputStream output) throws IOException { IOUtils.writeLines(lines, lineEnding, output); }
  
  @Deprecated
  public static void writeLines(Collection lines, String lineEnding, OutputStream output, String encoding) throws IOException { IOUtils.writeLines(lines, lineEnding, output, encoding); }
  
  @Deprecated
  public static void writeLines(Collection lines, String lineEnding, Writer writer) throws IOException { IOUtils.writeLines(lines, lineEnding, writer); }
  
  @Deprecated
  public static int copy(InputStream input, OutputStream output) throws IOException { return IOUtils.copy(input, output); }
  
  @Deprecated
  public static long copyLarge(InputStream input, OutputStream output) throws IOException { return IOUtils.copyLarge(input, output); }
  
  @Deprecated
  public static void copy(InputStream input, Writer output) throws IOException { IOUtils.copy(input, output); }
  
  @Deprecated
  public static void copy(InputStream input, Writer output, String encoding) throws IOException { IOUtils.copy(input, output, encoding); }
  
  @Deprecated
  public static int copy(Reader input, Writer output) throws IOException { return IOUtils.copy(input, output); }
  
  @Deprecated
  public static long copyLarge(Reader input, Writer output) throws IOException { return IOUtils.copyLarge(input, output); }
  
  @Deprecated
  public static void copy(Reader input, OutputStream output) throws IOException { IOUtils.copy(input, output); }
  
  @Deprecated
  public static void copy(Reader input, OutputStream output, String encoding) throws IOException { IOUtils.copy(input, output, encoding); }
  
  @Deprecated
  public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException { return IOUtils.contentEquals(input1, input2); }
  
  @Deprecated
  public static boolean contentEquals(Reader input1, Reader input2) throws IOException { return IOUtils.contentEquals(input1, input2); }
}
