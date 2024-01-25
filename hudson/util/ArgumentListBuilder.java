package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArgumentListBuilder implements Serializable, Cloneable {
  private List<String> args = new ArrayList();
  
  private BitSet mask = new BitSet();
  
  private static final long serialVersionUID = 1L;
  
  public ArgumentListBuilder(String... args) { add(args); }
  
  public ArgumentListBuilder add(Object a) { return add(a.toString(), false); }
  
  public ArgumentListBuilder add(Object a, boolean mask) { return add(a.toString(), mask); }
  
  public ArgumentListBuilder add(File f) { return add(f.getAbsolutePath(), false); }
  
  public ArgumentListBuilder add(String a) { return add(a, false); }
  
  public ArgumentListBuilder add(String a, boolean mask) {
    if (a != null) {
      if (mask)
        this.mask.set(this.args.size()); 
      this.args.add(a);
    } 
    return this;
  }
  
  public ArgumentListBuilder prepend(String... args) {
    BitSet nm = new BitSet(this.args.size() + args.length);
    for (int i = 0; i < this.args.size(); i++)
      nm.set(i + args.length, this.mask.get(i)); 
    this.mask = nm;
    this.args.addAll(0, Arrays.asList(args));
    return this;
  }
  
  public ArgumentListBuilder addQuoted(String a) { return add("\"" + a + "\"", false); }
  
  public ArgumentListBuilder addQuoted(String a, boolean mask) { return add("\"" + a + "\"", mask); }
  
  public ArgumentListBuilder add(String... args) {
    for (String arg : args)
      add(arg); 
    return this;
  }
  
  public ArgumentListBuilder add(@NonNull Iterable<String> args) {
    for (String arg : args)
      add(arg); 
    return this;
  }
  
  public ArgumentListBuilder addTokenized(String s) {
    if (s == null)
      return this; 
    add(Util.tokenize(s));
    return this;
  }
  
  public ArgumentListBuilder addKeyValuePair(String prefix, String key, String value, boolean mask) {
    if (key == null)
      return this; 
    add(((prefix == null) ? "-D" : prefix) + ((prefix == null) ? "-D" : prefix) + "=" + key, mask);
    return this;
  }
  
  public ArgumentListBuilder addKeyValuePairs(String prefix, Map<String, String> props) {
    for (Map.Entry<String, String> e : props.entrySet())
      addKeyValuePair(prefix, (String)e.getKey(), (String)e.getValue(), false); 
    return this;
  }
  
  public ArgumentListBuilder addKeyValuePairs(String prefix, Map<String, String> props, Set<String> propsToMask) {
    for (Map.Entry<String, String> e : props.entrySet())
      addKeyValuePair(prefix, (String)e.getKey(), (String)e.getValue(), (propsToMask != null && propsToMask.contains(e.getKey()))); 
    return this;
  }
  
  public ArgumentListBuilder addKeyValuePairsFromPropertyString(String prefix, String properties, VariableResolver<String> vr) throws IOException { return addKeyValuePairsFromPropertyString(prefix, properties, vr, null); }
  
  public ArgumentListBuilder addKeyValuePairsFromPropertyString(String prefix, String properties, VariableResolver<String> vr, Set<String> propsToMask) throws IOException {
    if (properties == null)
      return this; 
    properties = Util.replaceMacro(properties, propertiesGeneratingResolver(vr));
    for (Map.Entry<Object, Object> entry : Util.loadProperties(properties).entrySet())
      addKeyValuePair(prefix, (String)entry.getKey(), entry.getValue().toString(), (propsToMask != null && propsToMask.contains(entry.getKey()))); 
    return this;
  }
  
  private static VariableResolver<String> propertiesGeneratingResolver(VariableResolver<String> original) { return new Object(original); }
  
  public String[] toCommandArray() { return (String[])this.args.toArray(new String[0]); }
  
  public ArgumentListBuilder clone() {
    try {
      ArgumentListBuilder r = (ArgumentListBuilder)super.clone();
      r.args = new ArrayList(this.args);
      r.mask = (BitSet)this.mask.clone();
      return r;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    } 
  }
  
  public void clear() {
    this.args.clear();
    this.mask.clear();
  }
  
  public List<String> toList() { return this.args; }
  
  public String toStringWithQuote() {
    StringBuilder buf = new StringBuilder();
    for (String arg : this.args) {
      if (buf.length() > 0)
        buf.append(' '); 
      if (arg.indexOf(' ') >= 0 || arg.isEmpty()) {
        buf.append('"').append(arg).append('"');
        continue;
      } 
      buf.append(arg);
    } 
    return buf.toString();
  }
  
  public ArgumentListBuilder toWindowsCommand(boolean escapeVars) {
    ArgumentListBuilder windowsCommand = (new ArgumentListBuilder()).add(new String[] { "cmd.exe", "/C" });
    for (int i = 0; i < this.args.size(); i++) {
      StringBuilder quotedArgs = new StringBuilder();
      String arg = (String)this.args.get(i);
      boolean percent = false, quoted = percent;
      for (int j = 0; j < arg.length(); j++) {
        char c = arg.charAt(j);
        if (!quoted && (c == ' ' || c == '*' || c == '?' || c == ',' || c == ';')) {
          quoted = startQuoting(quotedArgs, arg, j);
        } else if (c == '^' || c == '&' || c == '<' || c == '>' || c == '|') {
          if (!quoted)
            quoted = startQuoting(quotedArgs, arg, j); 
        } else if (c == '"') {
          if (!quoted)
            quoted = startQuoting(quotedArgs, arg, j); 
          quotedArgs.append('"');
        } else if (percent && escapeVars && ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
          if (!quoted)
            quoted = startQuoting(quotedArgs, arg, j); 
          quotedArgs.append('"').append(c);
          c = '"';
        } 
        percent = (c == '%');
        if (quoted)
          quotedArgs.append(c); 
      } 
      if (i == 0)
        if (quoted) {
          quotedArgs.insert(0, '"');
        } else {
          quotedArgs.append('"');
        }  
      if (quoted) {
        quotedArgs.append('"');
      } else {
        quotedArgs.append(arg);
      } 
      windowsCommand.add(quotedArgs, this.mask.get(i));
    } 
    windowsCommand.add("&&").add("exit").add("%%ERRORLEVEL%%\"");
    return windowsCommand;
  }
  
  public ArgumentListBuilder toWindowsCommand() { return toWindowsCommand(false); }
  
  private static boolean startQuoting(StringBuilder buf, String arg, int atIndex) {
    buf.append('"').append(arg, 0, atIndex);
    return true;
  }
  
  public boolean hasMaskedArguments() { return (this.mask.length() > 0); }
  
  public boolean[] toMaskArray() {
    boolean[] mask = new boolean[this.args.size()];
    for (int i = 0; i < mask.length; i++)
      mask[i] = this.mask.get(i); 
    return mask;
  }
  
  public void addMasked(String string) { add(string, true); }
  
  public ArgumentListBuilder addMasked(Secret s) { return add(Secret.toString(s), true); }
  
  public String toString() {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < this.args.size(); i++) {
      String arg = (String)this.args.get(i);
      if (this.mask.get(i))
        arg = "******"; 
      if (buf.length() > 0)
        buf.append(' '); 
      if (arg.indexOf(' ') >= 0 || arg.isEmpty()) {
        buf.append('"').append(arg).append('"');
      } else {
        buf.append(arg);
      } 
    } 
    return buf.toString();
  }
  
  public ArgumentListBuilder() {}
}
