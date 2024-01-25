package hudson.util;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class InterceptingProxy {
  protected abstract Object call(Object paramObject, Method paramMethod, Object[] paramArrayOfObject) throws Throwable;
  
  public final <T> T wrap(Class<T> type, T object) { return (T)type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, new Object(this, object))); }
}
