package hudson.util;

import java.io.OutputStream;

@Deprecated
public final class NullStream extends OutputStream {
  public void write(byte[] b) {}
  
  public void write(byte[] b, int off, int len) {}
  
  public void write(int b) {}
}
