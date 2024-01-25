package hudson.util;

import java.io.IOException;
import java.io.OutputStream;

public class FlushProofOutputStream extends DelegatingOutputStream {
  public FlushProofOutputStream(OutputStream out) { super(out); }
  
  public void flush() throws IOException {}
}
