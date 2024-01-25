package hudson.util;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.collections.map.LRUMap;

public class LRUStringConverter extends AbstractSingleValueConverter {
  private final Map<String, String> cache;
  
  public LRUStringConverter() { this(1000); }
  
  public LRUStringConverter(int size) { this.cache = Collections.synchronizedMap(new LRUMap(size)); }
  
  public boolean canConvert(Class type) { return type.equals(String.class); }
  
  public Object fromString(String str) {
    String s = (String)this.cache.get(str);
    if (s == null) {
      this.cache.put(str, str);
      s = str;
    } 
    return s;
  }
}
