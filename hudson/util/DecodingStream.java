package hudson.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DecodingStream extends FilterOutputStream {
  private int last = -1;
  
  public DecodingStream(OutputStream out) { super(out); }
  
  public void write(int b) throws IOException {
    if (this.last == -1) {
      this.last = b;
      return;
    } 
    this.out.write(Character.getNumericValue(this.last) * 16 + Character.getNumericValue(b));
    this.last = -1;
  }
}
