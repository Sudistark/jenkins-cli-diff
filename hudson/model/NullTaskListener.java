package hudson.model;

import java.io.PrintStream;
import org.apache.commons.io.output.NullPrintStream;

class NullTaskListener implements TaskListener {
  private static final long serialVersionUID = 1L;
  
  public PrintStream getLogger() { return new NullPrintStream(); }
}
