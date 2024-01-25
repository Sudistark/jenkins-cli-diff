package hudson.util;

import java.io.IOException;

@Deprecated
public class IOException2 extends IOException {
  public IOException2(Throwable cause) { super(cause); }
  
  public IOException2(String s, Throwable cause) { super(s, cause); }
}
