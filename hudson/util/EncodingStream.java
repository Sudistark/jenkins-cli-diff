package hudson.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EncodingStream extends FilterOutputStream {
  private static final String chars = "0123456789ABCDEF";
  
  public EncodingStream(OutputStream out) { super(out); }
  
  public void write(int b) throws IOException {
    this.out.write("0123456789ABCDEF".charAt(b >> 4 & 0xF));
    this.out.write("0123456789ABCDEF".charAt(b & 0xF));
  }
}
