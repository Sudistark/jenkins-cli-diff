package jenkins.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class MarkFindingOutputStream extends OutputStream {
  private final OutputStream base;
  
  private int match;
  
  public static final String MARK = "[Jenkins:SYNC-MARK]\n";
  
  protected MarkFindingOutputStream(OutputStream base) {
    this.match = 0;
    this.base = base;
  }
  
  public void write(int b) throws IOException {
    if (MBYTES[this.match] == b) {
      this.match++;
      if (this.match == MBYTES.length) {
        onMarkFound();
        this.match = 0;
      } 
    } else if (this.match > 0) {
      this.base.write(MBYTES, 0, this.match);
      this.match = 0;
      write(b);
    } else {
      this.base.write(b);
    } 
  }
  
  public void write(byte[] b, int off, int len) throws IOException {
    int start = off;
    int end = off + len;
    for (int i = off; i < end; ) {
      if (MBYTES[this.match] == b[i]) {
        this.match++;
        i++;
        if (this.match == MBYTES.length) {
          this.base.write(b, off, i - off - MBYTES.length);
          onMarkFound();
          this.match = 0;
          off = i;
          len = end - i;
        } 
        continue;
      } 
      if (this.match > 0) {
        int extra = this.match - i - start;
        if (extra > 0)
          this.base.write(MBYTES, 0, extra); 
        this.match = 0;
        continue;
      } 
      i++;
    } 
    if (len - this.match > 0)
      this.base.write(b, off, len - this.match); 
  }
  
  public void flush() throws IOException {
    flushPartialMatch();
    this.base.flush();
  }
  
  public void close() throws IOException {
    flushPartialMatch();
    this.base.close();
  }
  
  private void flushPartialMatch() throws IOException {
    if (this.match > 0) {
      this.base.write(MBYTES, 0, this.match);
      this.match = 0;
    } 
  }
  
  private static final byte[] MBYTES = toUTF8("[Jenkins:SYNC-MARK]\n");
  
  protected abstract void onMarkFound() throws IOException;
  
  private static byte[] toUTF8(String s) { return s.getBytes(StandardCharsets.UTF_8); }
}
