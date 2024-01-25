package hudson.util;

import com.thoughtworks.xstream.core.util.FastStack;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.AbstractXmlWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import java.io.Writer;

class PrettyPrintWriter extends AbstractXmlWriter {
  public static int XML_QUIRKS = -1;
  
  public static int XML_1_0 = 0;
  
  public static int XML_1_1 = 1;
  
  private final QuickWriter writer;
  
  private final FastStack elementStack = new FastStack(16);
  
  private final char[] lineIndenter;
  
  private final int mode;
  
  private boolean tagInProgress;
  
  protected int depth;
  
  private boolean readyForNewLine;
  
  private boolean tagIsEmpty;
  
  private static final char[] NULL = "&#x0;".toCharArray();
  
  private static final char[] AMP = "&amp;".toCharArray();
  
  private static final char[] LT = "&lt;".toCharArray();
  
  private static final char[] GT = "&gt;".toCharArray();
  
  private static final char[] CR = "&#xd;".toCharArray();
  
  private static final char[] QUOT = "&quot;".toCharArray();
  
  private static final char[] APOS = "&apos;".toCharArray();
  
  private static final char[] CLOSE = "</".toCharArray();
  
  PrettyPrintWriter(Writer writer, int mode, char[] lineIndenter, NameCoder nameCoder) {
    super(nameCoder);
    this.writer = new QuickWriter(writer);
    this.lineIndenter = lineIndenter;
    this.mode = mode;
    if (mode < XML_QUIRKS || mode > XML_1_1)
      throw new IllegalArgumentException("Not a valid XML mode"); 
  }
  
  @Deprecated
  PrettyPrintWriter(Writer writer, int mode, char[] lineIndenter, XmlFriendlyReplacer replacer) { this(writer, mode, lineIndenter, replacer); }
  
  PrettyPrintWriter(Writer writer, int mode, char[] lineIndenter) { this(writer, mode, lineIndenter, new XmlFriendlyNameCoder()); }
  
  PrettyPrintWriter(Writer writer, char[] lineIndenter) { this(writer, XML_QUIRKS, lineIndenter); }
  
  PrettyPrintWriter(Writer writer, int mode, String lineIndenter) { this(writer, mode, lineIndenter.toCharArray()); }
  
  PrettyPrintWriter(Writer writer, String lineIndenter) { this(writer, lineIndenter.toCharArray()); }
  
  PrettyPrintWriter(Writer writer, int mode, NameCoder nameCoder) { this(writer, mode, new char[] { ' ', ' ' }, nameCoder); }
  
  @Deprecated
  PrettyPrintWriter(Writer writer, int mode, XmlFriendlyReplacer replacer) { this(writer, mode, new char[] { ' ', ' ' }, replacer); }
  
  PrettyPrintWriter(Writer writer, NameCoder nameCoder) { this(writer, XML_QUIRKS, new char[] { ' ', ' ' }, nameCoder); }
  
  @Deprecated
  PrettyPrintWriter(Writer writer, XmlFriendlyReplacer replacer) { this(writer, XML_QUIRKS, new char[] { ' ', ' ' }, replacer); }
  
  PrettyPrintWriter(Writer writer, int mode) { this(writer, mode, new char[] { ' ', ' ' }); }
  
  PrettyPrintWriter(Writer writer) { this(writer, new char[] { ' ', ' ' }); }
  
  public void startNode(String name) {
    String escapedName = encodeNode(name);
    this.tagIsEmpty = false;
    finishTag();
    this.writer.write('<');
    this.writer.write(escapedName);
    this.elementStack.push(escapedName);
    this.tagInProgress = true;
    this.depth++;
    this.readyForNewLine = true;
    this.tagIsEmpty = true;
  }
  
  public void startNode(String name, Class clazz) { startNode(name); }
  
  public void setValue(String text) {
    this.readyForNewLine = false;
    this.tagIsEmpty = false;
    finishTag();
    writeText(this.writer, text);
  }
  
  public void addAttribute(String key, String value) {
    this.writer.write(' ');
    this.writer.write(encodeAttribute(key));
    this.writer.write('=');
    this.writer.write('"');
    writeAttributeValue(this.writer, value);
    this.writer.write('"');
  }
  
  protected void writeAttributeValue(QuickWriter writer, String text) { writeText(text, true); }
  
  protected void writeText(QuickWriter writer, String text) { writeText(text, false); }
  
  private void writeText(String text, boolean isAttribute) {
    text.codePoints().forEach(c -> {
          switch (c) {
            case 0:
              if (this.mode == XML_QUIRKS) {
                this.writer.write(NULL);
              } else {
                throw new StreamException("Invalid character 0x0 in XML stream");
              } 
              return;
            case 38:
              this.writer.write(AMP);
              return;
            case 60:
              this.writer.write(LT);
              return;
            case 62:
              this.writer.write(GT);
              return;
            case 34:
              this.writer.write(QUOT);
              return;
            case 39:
              this.writer.write(APOS);
              return;
            case 13:
              this.writer.write(CR);
              return;
            case 9:
            case 10:
              if (!isAttribute) {
                this.writer.write(Character.toChars(c));
                return;
              } 
              break;
          } 
          if (Character.isDefined(c) && !Character.isISOControl(c)) {
            if (this.mode != XML_QUIRKS && 
              c > 55295 && c < 57344)
              throw new StreamException("Invalid character 0x" + 
                  Integer.toHexString(c) + " in XML stream"); 
            this.writer.write(Character.toChars(c));
          } else {
            if (this.mode == XML_1_0 && (
              c < 9 || c == 11 || c == 12 || c == 14 || (c >= 15 && c <= 31)))
              throw new StreamException("Invalid character 0x" + 
                  Integer.toHexString(c) + " in XML 1.0 stream"); 
            if (this.mode != XML_QUIRKS && (
              c == 65534 || c == 65535))
              throw new StreamException("Invalid character 0x" + 
                  Integer.toHexString(c) + " in XML stream"); 
            this.writer.write("&#x");
            this.writer.write(Integer.toHexString(c));
            this.writer.write(';');
          } 
        });
  }
  
  public void endNode() {
    this.depth--;
    if (this.tagIsEmpty) {
      this.writer.write('/');
      this.readyForNewLine = false;
      finishTag();
      this.elementStack.popSilently();
    } else {
      finishTag();
      this.writer.write(CLOSE);
      this.writer.write((String)this.elementStack.pop());
      this.writer.write('>');
    } 
    this.readyForNewLine = true;
    if (this.depth == 0)
      this.writer.flush(); 
  }
  
  private void finishTag() {
    if (this.tagInProgress)
      this.writer.write('>'); 
    this.tagInProgress = false;
    if (this.readyForNewLine)
      endOfLine(); 
    this.readyForNewLine = false;
    this.tagIsEmpty = false;
  }
  
  protected void endOfLine() {
    this.writer.write(getNewLine());
    for (int i = 0; i < this.depth; i++)
      this.writer.write(this.lineIndenter); 
  }
  
  public void flush() { this.writer.flush(); }
  
  public void close() { this.writer.close(); }
  
  protected String getNewLine() { return "\n"; }
}
