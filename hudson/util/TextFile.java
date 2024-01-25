package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

public class TextFile {
  @NonNull
  public final File file;
  
  public TextFile(@NonNull File file) { this.file = file; }
  
  public boolean exists() { return this.file.exists(); }
  
  public void delete() throws IOException { Files.deleteIfExists(Util.fileToPath(this.file)); }
  
  public String read() throws IOException {
    StringWriter out = new StringWriter();
    PrintWriter w = new PrintWriter(out);
    try {
      BufferedReader in = Files.newBufferedReader(Util.fileToPath(this.file), StandardCharsets.UTF_8);
      try {
        String line;
        while ((line = in.readLine()) != null)
          w.println(line); 
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
    } catch (Exception e) {
      throw new IOException("Failed to fully read " + this.file, e);
    } 
    return out.toString();
  }
  
  @NonNull
  public Stream<String> lines() throws IOException { return Files.lines(Util.fileToPath(this.file)); }
  
  public void write(String text) throws IOException {
    Util.createDirectories(Util.fileToPath(this.file.getParentFile()), new java.nio.file.attribute.FileAttribute[0]);
    w = new AtomicFileWriter(this.file);
    try {
      try {
        w.write(text);
        w.commit();
      } finally {
        w.abort();
      } 
      w.close();
    } catch (Throwable throwable) {
      try {
        w.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  @NonNull
  public String head(int numChars) throws IOException {
    char[] buf = new char[numChars];
    int read = 0;
    Reader r = new FileReader(this.file);
    try {
      while (read < numChars) {
        int d = r.read(buf, read, buf.length - read);
        if (d < 0)
          break; 
        read += d;
      } 
      String str = new String(buf, 0, read);
      r.close();
      return str;
    } catch (Throwable throwable) {
      try {
        r.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  @NonNull
  public String fastTail(int numChars, Charset cs) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(this.file, "r");
    try {
      long len = raf.length();
      long pos = Math.max(0L, len - (numChars * 4 + 1024));
      raf.seek(pos);
      byte[] tail = new byte[(int)(len - pos)];
      raf.readFully(tail);
      String tails = cs.decode(ByteBuffer.wrap(tail)).toString();
      String str = tails.substring(Math.max(0, tails.length() - numChars));
      raf.close();
      return str;
    } catch (Throwable throwable) {
      try {
        raf.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  @NonNull
  public String fastTail(int numChars) throws IOException { return fastTail(numChars, Charset.defaultCharset()); }
  
  public String readTrim() throws IOException { return read().trim(); }
  
  public String toString() throws IOException { return this.file.toString(); }
}
