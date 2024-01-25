package hudson.search;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ParsedQuickSilver {
  private static final Map<Class, ParsedQuickSilver> TABLE = new HashMap();
  
  static ParsedQuickSilver get(Class<? extends SearchableModelObject> clazz) {
    ParsedQuickSilver pqs = (ParsedQuickSilver)TABLE.get(clazz);
    if (pqs == null)
      TABLE.put(clazz, pqs = new ParsedQuickSilver(clazz)); 
    return pqs;
  }
  
  private final List<Getter> getters = new ArrayList();
  
  private ParsedQuickSilver(Class<? extends SearchableModelObject> clazz) {
    for (Method m : clazz.getMethods()) {
      QuickSilver qs = (QuickSilver)m.getAnnotation(QuickSilver.class);
      if (qs != null) {
        String url = stripGetPrefix(m);
        if (qs.value().length == 0) {
          this.getters.add(new MethodGetter(url, splitName(url), m));
        } else {
          for (String name : qs.value())
            this.getters.add(new MethodGetter(url, name, m)); 
        } 
      } 
    } 
    for (Field f : clazz.getFields()) {
      QuickSilver qs = (QuickSilver)f.getAnnotation(QuickSilver.class);
      if (qs != null)
        if (qs.value().length == 0) {
          this.getters.add(new FieldGetter(f.getName(), splitName(f.getName()), f));
        } else {
          for (String name : qs.value())
            this.getters.add(new FieldGetter(f.getName(), name, f)); 
        }  
    } 
  }
  
  private String splitName(String url) {
    StringBuilder buf = new StringBuilder(url.length() + 5);
    for (String token : url.split("(?<=[a-z])(?=[A-Z])")) {
      if (buf.length() > 0)
        buf.append(' '); 
      buf.append(Introspector.decapitalize(token));
    } 
    return buf.toString();
  }
  
  private String stripGetPrefix(Method m) {
    String n = m.getName();
    if (n.startsWith("get"))
      n = Introspector.decapitalize(n.substring(3)); 
    return n;
  }
  
  private static IllegalAccessError toError(IllegalAccessException e) {
    IllegalAccessError iae = new IllegalAccessError();
    iae.initCause(e);
    return iae;
  }
  
  public void addTo(SearchIndexBuilder builder, Object instance) {
    for (Getter getter : this.getters)
      builder.add(new Object(this, getter, instance)); 
  }
}
