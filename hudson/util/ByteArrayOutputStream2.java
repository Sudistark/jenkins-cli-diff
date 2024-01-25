package hudson.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayOutputStream2 extends ByteArrayOutputStream {
  public ByteArrayOutputStream2() {}
  
  public ByteArrayOutputStream2(int size) { super(size); }
  
  public byte[] getBuffer() { return this.buf; }
  
  public void readFrom(InputStream is) throws IOException {
    while (true) {
      if (this.count == this.buf.length) {
        byte[] data = new byte[this.buf.length * 2];
        System.arraycopy(this.buf, 0, data, 0, this.buf.length);
        this.buf = data;
      } 
      int sz = is.read(this.buf, this.count, this.buf.length - this.count);
      if (sz < 0)
        return; 
      this.count += sz;
    } 
  }
}
