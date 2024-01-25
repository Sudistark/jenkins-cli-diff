package hudson.util;

import java.io.IOException;
import java.io.OutputStream;

public abstract class DelegatingOutputStream extends OutputStream {
  protected OutputStream out;
  
  protected DelegatingOutputStream(OutputStream out) {
    if (out == null)
      throw new IllegalArgumentException("null stream"); 
    this.out = out;
  }
  
  public void write(int b) throws IOException { this.out.write(b); }
  
  public void write(byte[] b) throws IOException { this.out.write(b); }
  
  public void write(byte[] b, int off, int len) throws IOException { this.out.write(b, off, len); }
  
  public void flush() throws IOException { this.out.flush(); }
  
  public void close() throws IOException { this.out.close(); }
}
