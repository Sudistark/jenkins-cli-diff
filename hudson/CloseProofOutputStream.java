package hudson;

import hudson.util.DelegatingOutputStream;
import java.io.OutputStream;

public class CloseProofOutputStream extends DelegatingOutputStream {
  public CloseProofOutputStream(OutputStream out) { super(out); }
  
  public void close() {}
}
