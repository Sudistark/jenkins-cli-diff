package hudson.console;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringEscapeUtils;

public class PlainTextConsoleOutputStream extends LineTransformationOutputStream.Delegating {
  private static final Logger LOGGER = Logger.getLogger(PlainTextConsoleOutputStream.class.getName());
  
  public PlainTextConsoleOutputStream(OutputStream out) { super(out); }
  
  protected void eol(byte[] in, int sz) throws IOException {
    int next = ConsoleNote.findPreamble(in, 0, sz);
    int written = 0;
    while (next >= 0) {
      if (next > written) {
        this.out.write(in, written, next - written);
        written = next;
      } else {
        assert next == written;
      } 
      int rest = sz - next;
      ByteArrayInputStream b = new ByteArrayInputStream(in, next, rest);
      try {
        ConsoleNote.skip(new DataInputStream(b));
      } catch (IOException x) {
        LOGGER.log(Level.FINE, "Failed to skip annotation from \"" + StringEscapeUtils.escapeJava(new String(in, next, rest, Charset.defaultCharset())) + "\"", x);
      } 
      int bytesUsed = rest - b.available();
      written += bytesUsed;
      next = ConsoleNote.findPreamble(in, written, sz - written);
    } 
    this.out.write(in, written, sz - written);
  }
}
