package jenkins.util.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import hudson.RestrictedSince;
import hudson.Util;
import hudson.util.VariableResolver;
import hudson.util.XStream2;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.kohsuke.accmod.Restricted;

public class XStreamDOM {
  private final String tagName;
  
  private final String[] attributes;
  
  private final String value;
  
  private final List<XStreamDOM> children;
  
  public XStreamDOM(String tagName, Map<String, String> attributes, String value) {
    this.tagName = tagName;
    this.attributes = toAttributeList(attributes);
    this.value = value;
    this.children = null;
  }
  
  public XStreamDOM(String tagName, Map<String, String> attributes, List<XStreamDOM> children) {
    this.tagName = tagName;
    this.attributes = toAttributeList(attributes);
    this.value = null;
    this.children = children;
  }
  
  private XStreamDOM(String tagName, String[] attributes, List<XStreamDOM> children, String value) {
    this.tagName = tagName;
    this.attributes = attributes;
    this.children = children;
    this.value = value;
  }
  
  private String[] toAttributeList(Map<String, String> attributes) {
    String[] r = new String[attributes.size() * 2];
    int i = 0;
    for (Map.Entry<String, String> e : attributes.entrySet()) {
      r[i++] = (String)e.getKey();
      r[i++] = (String)e.getValue();
    } 
    return r;
  }
  
  public String getTagName() { return this.tagName; }
  
  public <T> T unmarshal(XStream xs) { return (T)xs.unmarshal(newReader()); }
  
  public <T> T unmarshal(XStream xs, T root) { return (T)xs.unmarshal(newReader(), root); }
  
  public XStreamDOM expandMacro(VariableResolver<String> vars) {
    String[] newAttributes = new String[this.attributes.length];
    for (int i = 0; i < this.attributes.length; i += 2) {
      newAttributes[i + 0] = this.attributes[i];
      newAttributes[i + 1] = Util.replaceMacro(this.attributes[i + 1], vars);
    } 
    List<XStreamDOM> newChildren = null;
    if (this.children != null) {
      newChildren = new ArrayList<XStreamDOM>(this.children.size());
      for (XStreamDOM d : this.children)
        newChildren.add(d.expandMacro(vars)); 
    } 
    return new XStreamDOM(this.tagName, newAttributes, newChildren, Util.replaceMacro(this.value, vars));
  }
  
  public String getAttribute(String name) {
    for (int i = 0; i < this.attributes.length; i += 2) {
      if (this.attributes[i].equals(name))
        return this.attributes[i + 1]; 
    } 
    return null;
  }
  
  public int getAttributeCount() { return this.attributes.length / 2; }
  
  String getAttributeName(int index) { return this.attributes[index * 2]; }
  
  public String getAttribute(int index) { return this.attributes[index * 2 + 1]; }
  
  public String getValue() { return this.value; }
  
  public List<XStreamDOM> getChildren() { return this.children; }
  
  public HierarchicalStreamReader newReader() { return new ReaderImpl(this); }
  
  public static WriterImpl newWriter() { return new WriterImpl(); }
  
  public void writeTo(OutputStream os) { writeTo(XStream2.getDefaultDriver().createWriter(os)); }
  
  public void writeTo(Writer w) { writeTo(XStream2.getDefaultDriver().createWriter(w)); }
  
  public void writeTo(HierarchicalStreamWriter w) { (new ConverterImpl()).marshal(this, w, null); }
  
  public static XStreamDOM from(XStream xs, Object obj) {
    WriterImpl w = newWriter();
    xs.marshal(obj, w);
    return w.getOutput();
  }
  
  public static XStreamDOM from(InputStream in) { return from(XStream2.getDefaultDriver().createReader(in)); }
  
  public static XStreamDOM from(Reader in) { return from(XStream2.getDefaultDriver().createReader(in)); }
  
  public static XStreamDOM from(HierarchicalStreamReader in) { return (new ConverterImpl()).unmarshalElement(in, null); }
  
  public Map<String, String> getAttributeMap() {
    Map<String, String> r = new HashMap<String, String>();
    for (int i = 0; i < this.attributes.length; i += 2)
      r.put(this.attributes[i], this.attributes[i + 1]); 
    return r;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.301")
  public static final XmlFriendlyReplacer REPLACER = new XmlFriendlyReplacer();
}
