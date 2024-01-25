package hudson.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamCopyThread extends Thread {
  private final InputStream in;
  
  private final OutputStream out;
  
  private final boolean closeOut;
  
  public StreamCopyThread(String threadName, InputStream in, OutputStream out, boolean closeOut) {
    super(threadName);
    this.in = in;
    if (out == null)
      throw new NullPointerException("out is null"); 
    this.out = out;
    this.closeOut = closeOut;
  }
  
  public StreamCopyThread(String threadName, InputStream in, OutputStream out) { this(threadName, in, out, false); }
  
  public void run() {
    try {
      try {
        byte[] buf = new byte[8192];
        int len;
        while ((len = this.in.read(buf)) >= 0)
          this.out.write(buf, 0, len); 
      } finally {
        this.in.close();
        if (this.closeOut) {
          this.out.close();
        } else {
          this.out.flush();
        } 
      } 
    } catch (IOException iOException) {}
  }
}
