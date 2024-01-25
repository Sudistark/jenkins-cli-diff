package org.acegisecurity.util;

import java.lang.reflect.Field;
import org.apache.commons.lang.reflect.FieldUtils;

@Deprecated
public final class FieldUtils {
  public static Object getProtectedFieldValue(String protectedField, Object object) {
    try {
      return FieldUtils.readField(object, protectedField, true);
    } catch (IllegalAccessException x) {
      throw new RuntimeException(x);
    } 
  }
  
  public static void setProtectedFieldValue(String protectedField, Object object, Object newValue) {
    try {
      Field field = FieldUtils.getField(object.getClass(), protectedField, true);
      field.setAccessible(true);
      field.set(object, newValue);
    } catch (Exception x) {
      throw new RuntimeException(x);
    } 
  }
}
