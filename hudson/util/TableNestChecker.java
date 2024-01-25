package hudson.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.jelly.XMLOutput;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class TableNestChecker extends XMLFilterImpl {
  private final Stack<Checker> elements;
  
  private final Stack<String> tagNames;
  
  public static void applyTo(XMLOutput xo) { xo.setContentHandler(new TableNestChecker(xo.getContentHandler())); }
  
  public TableNestChecker() {
    this.elements = new Stack();
    this.tagNames = new Stack();
    this.elements.push(ALL_ALLOWED);
  }
  
  public TableNestChecker(ContentHandler target) {
    this();
    setContentHandler(target);
  }
  
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    String tagName = localName.toUpperCase(Locale.ENGLISH);
    if (!((Checker)this.elements.peek()).isAllowed(tagName))
      throw new SAXException(tagName + " is not allowed inside " + tagName); 
    Checker next = (Checker)CHECKERS.get(tagName);
    if (next == null)
      next = ALL_ALLOWED; 
    this.elements.push(next);
    this.tagNames.push(tagName);
    super.startElement(uri, localName, qName, atts);
  }
  
  public void endElement(String uri, String localName, String qName) throws SAXException {
    this.elements.pop();
    this.tagNames.pop();
    super.endElement(uri, localName, qName);
  }
  
  private static final Checker ALL_ALLOWED = childTag -> true;
  
  private static final Map<String, Checker> CHECKERS = new HashMap();
  
  static  {
    CHECKERS.put("TABLE", new InList(new String[] { "TR", "THEAD", "TBODY" }));
    rows = new InList(new String[] { "TR" });
    CHECKERS.put("THEAD", rows);
    CHECKERS.put("TR", new InList(new String[] { "TD", "TH" }));
  }
}
