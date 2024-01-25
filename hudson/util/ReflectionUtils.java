package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.util.ReflectionUtils;

public class ReflectionUtils extends ReflectionUtils {
  public static Method getPublicMethodNamed(Class c, String methodName) {
    for (Method m : c.getMethods()) {
      if (m.getName().equals(methodName))
        return m; 
    } 
    return null;
  }
  
  public static List<Parameter> getParameters(Method m) { return new MethodInfo(m); }
  
  public static Object getPublicProperty(Object o, String p) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
    PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(o, p);
    if (pd == null)
      try {
        Field f = o.getClass().getField(p);
        return f.get(o);
      } catch (NoSuchFieldException e) {
        throw new IllegalArgumentException("No such property " + p + " on " + o.getClass(), e);
      }  
    return PropertyUtils.getProperty(o, p);
  }
  
  @CheckForNull
  public static Object getVmDefaultValueForPrimitiveType(Class<?> type) { return defaultPrimitiveValue.get(type); }
  
  private static final Map<Class<?>, Object> defaultPrimitiveValue = new HashMap();
  
  static  {
    defaultPrimitiveValue.put(boolean.class, Boolean.valueOf(false));
    defaultPrimitiveValue.put(char.class, Character.valueOf(false));
    defaultPrimitiveValue.put(byte.class, Byte.valueOf((byte)0));
    defaultPrimitiveValue.put(short.class, Short.valueOf((short)0));
    defaultPrimitiveValue.put(int.class, Integer.valueOf(0));
    defaultPrimitiveValue.put(long.class, Long.valueOf(0L));
    defaultPrimitiveValue.put(float.class, Float.valueOf(0.0F));
    defaultPrimitiveValue.put(double.class, Double.valueOf(0.0D));
    defaultPrimitiveValue.put(void.class, null);
  }
}
