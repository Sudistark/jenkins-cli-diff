package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.console.ConsoleNote;
import hudson.console.HyperlinkNote;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.jenkinsci.remoting.SerializableOnlyOverRemoting;
import org.kohsuke.accmod.Restricted;

public interface TaskListener extends SerializableOnlyOverRemoting {
  @NonNull
  PrintStream getLogger();
  
  @Restricted({org.kohsuke.accmod.restrictions.ProtectedExternally.class})
  @NonNull
  default Charset getCharset() { return StandardCharsets.UTF_8; }
  
  private PrintWriter _error(String prefix, String msg) {
    PrintStream out = getLogger();
    out.print(prefix);
    out.println(msg);
    Charset charset = getCharset();
    return new PrintWriter(new OutputStreamWriter(out, charset), true);
  }
  
  default void annotate(ConsoleNote ann) throws IOException { ann.encodeTo(getLogger()); }
  
  default void hyperlink(String url, String text) throws IOException {
    annotate(new HyperlinkNote(url, text.length()));
    getLogger().print(text);
  }
  
  @NonNull
  default PrintWriter error(String msg) { return _error("ERROR: ", msg); }
  
  @NonNull
  PrintWriter error(String format, Object... args) { return error(String.format(format, args)); }
  
  @NonNull
  default PrintWriter fatalError(String msg) { return _error("FATAL: ", msg); }
  
  @NonNull
  PrintWriter fatalError(String format, Object... args) { return fatalError(String.format(format, args)); }
  
  public static final TaskListener NULL = new NullTaskListener();
}
