package hudson.util;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

public class HeapSpaceStringConverter extends AbstractSingleValueConverter {
  public boolean canConvert(Class type) { return type.equals(String.class); }
  
  public Object fromString(String str) { return str; }
}
