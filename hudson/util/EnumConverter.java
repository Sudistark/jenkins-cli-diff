package hudson.util;

import org.apache.commons.beanutils.Converter;

public class EnumConverter implements Converter {
  public Object convert(Class aClass, Object object) { return Enum.valueOf(aClass, object.toString()); }
}
