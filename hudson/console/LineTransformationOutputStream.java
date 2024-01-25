package hudson.console;

import hudson.util.ByteArrayOutputStream2;
import java.io.IOException;
import java.io.OutputStream;

public abstract class LineTransformationOutputStream extends OutputStream {
  private ByteArrayOutputStream2 buf = new ByteArrayOutputStream2();
  
  private static final int LF = 10;
  
  protected abstract void eol(byte[] paramArrayOfByte, int paramInt) throws IOException;
  
  public void write(int b) throws IOException {
    this.buf.write(b);
    if (b == 10)
      eol(); 
  }
  
  private void eol() {
    eol(this.buf.getBuffer(), this.buf.size());
    if (this.buf.size() > 4096) {
      this.buf = new ByteArrayOutputStream2();
    } else {
      this.buf.reset();
    } 
  }
  
  public void write(byte[] b, int off, int len) throws IOException {
    int end = off + len;
    for (int i = off; i < end; i++)
      write(b[i]); 
  }
  
  public void close() { forceEol(); }
  
  public void forceEol() {
    if (this.buf.size() > 0)
      eol(); 
  }
  
  protected String trimEOL(String line) {
    int slen = line.length();
    while (slen > 0) {
      char ch = line.charAt(slen - 1);
      if (ch == '\r' || ch == '\n')
        slen--; 
    } 
    return line.substring(0, slen);
  }
}
