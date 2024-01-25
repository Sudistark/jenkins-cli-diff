package hudson.util;

import java.io.IOException;
import java.io.OutputStream;

public class DualOutputStream extends OutputStream {
  private final OutputStream lhs;
  
  private final OutputStream rhs;
  
  public DualOutputStream(OutputStream lhs, OutputStream rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public void write(int b) throws IOException {
    this.lhs.write(b);
    this.rhs.write(b);
  }
  
  public void write(byte[] b) throws IOException {
    this.lhs.write(b);
    this.rhs.write(b);
  }
  
  public void write(byte[] b, int off, int len) throws IOException {
    this.lhs.write(b, off, len);
    this.rhs.write(b, off, len);
  }
  
  public void flush() throws IOException {
    this.lhs.flush();
    this.rhs.flush();
  }
  
  public void close() throws IOException {
    this.lhs.close();
    this.rhs.close();
  }
}
