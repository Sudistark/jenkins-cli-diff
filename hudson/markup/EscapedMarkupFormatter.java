package hudson.markup;

import hudson.Util;
import java.io.IOException;
import java.io.Writer;

public class EscapedMarkupFormatter extends MarkupFormatter {
  public void translate(String markup, Writer output) throws IOException {
    if (markup != null)
      output.write(Util.escape(markup)); 
  }
}
