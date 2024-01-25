package hudson.console;

import hudson.MarkupText;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import org.kohsuke.stapler.framework.io.WriterOutputStream;

public class ConsoleAnnotationOutputStream<T> extends LineTransformationOutputStream {
  private final Writer out;
  
  private final T context;
  
  private ConsoleAnnotator<T> ann;
  
  private final LineBuffer line;
  
  private final WriterOutputStream lineOut;
  
  public ConsoleAnnotationOutputStream(Writer out, ConsoleAnnotator<? super T> ann, T context, Charset charset) {
    this.line = new LineBuffer(256);
    this.out = out;
    this.ann = ConsoleAnnotator.cast(ann);
    this.context = context;
    this.lineOut = new WriterOutputStream(this.line, charset);
  }
  
  public ConsoleAnnotator<T> getConsoleAnnotator() { return this.ann; }
  
  protected void eol(byte[] in, int sz) throws IOException {
    this.line.reset();
    StringBuffer strBuf = this.line.getStringBuffer();
    int next = ConsoleNote.findPreamble(in, 0, sz);
    List<ConsoleAnnotator<T>> annotators = null;
    int written = 0;
    while (next >= 0) {
      if (next > written) {
        this.lineOut.write(in, written, next - written);
        this.lineOut.flush();
        written = next;
      } else {
        assert next == written;
      } 
      int charPos = strBuf.length();
      int rest = sz - next;
      ByteArrayInputStream b = new ByteArrayInputStream(in, next, rest);
      try {
        ConsoleNote a = ConsoleNote.readFrom(new DataInputStream(b));
        if (a != null) {
          if (annotators == null)
            annotators = new ArrayList<ConsoleAnnotator<T>>(); 
          annotators.add(new Object(this, a, charPos));
        } 
      } catch (IOException|ClassNotFoundException e) {
        LOGGER.log(Level.FINE, "Failed to resurrect annotation from \"" + StringEscapeUtils.escapeJava(new String(in, next, rest, Charset.defaultCharset())) + "\"", e);
      } 
      int bytesUsed = rest - b.available();
      written += bytesUsed;
      next = ConsoleNote.findPreamble(in, written, sz - written);
    } 
    this.lineOut.write(in, written, sz - written);
    if (annotators != null) {
      if (this.ann != null)
        annotators.add(this.ann); 
      this.ann = ConsoleAnnotator.combine(annotators);
    } 
    this.lineOut.flush();
    MarkupText mt = new MarkupText(strBuf.toString());
    if (this.ann != null)
      this.ann = this.ann.annotate(this.context, mt); 
    this.out.write(mt.toString(true));
  }
  
  public void flush() throws IOException { this.out.flush(); }
  
  public void close() throws IOException {
    super.close();
    this.out.close();
  }
  
  private static final Logger LOGGER = Logger.getLogger(ConsoleAnnotationOutputStream.class.getName());
}
