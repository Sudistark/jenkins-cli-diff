package hudson.util;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HeadBufferingStream extends FilterInputStream {
  private final ByteArrayOutputStream side;
  
  private final int sideBufferSize;
  
  public HeadBufferingStream(InputStream in, int sideBufferSize) {
    super(in);
    this.sideBufferSize = sideBufferSize;
    this.side = new ByteArrayOutputStream(sideBufferSize);
  }
  
  public int read() throws IOException {
    int i = this.in.read();
    if (i >= 0 && space() > 0)
      this.side.write(i); 
    return i;
  }
  
  public int read(byte[] b, int off, int len) throws IOException {
    int r = this.in.read(b, off, len);
    if (r > 0) {
      int sp = space();
      if (sp > 0)
        this.side.write(b, off, Math.min(r, sp)); 
    } 
    return r;
  }
  
  private int space() throws IOException { return this.sideBufferSize - this.side.size(); }
  
  public void fillSide() throws IOException {
    byte[] buf = new byte[space()];
    while (space() > 0) {
      if (read(buf) < 0)
        return; 
    } 
  }
  
  public byte[] getSideBuffer() { return this.side.toByteArray(); }
}
