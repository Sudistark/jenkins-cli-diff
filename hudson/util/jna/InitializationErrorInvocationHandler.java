package hudson.util.jna;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class InitializationErrorInvocationHandler implements InvocationHandler {
  private final Throwable cause;
  
  private InitializationErrorInvocationHandler(Throwable cause) { this.cause = cause; }
  
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class)
      return method.invoke(this, args); 
    throw new UnsupportedOperationException("Failed to link the library: " + method.getDeclaringClass(), this.cause);
  }
  
  public static <T> T create(Class<T> type, Throwable cause) { return (T)type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, new InitializationErrorInvocationHandler(cause))); }
}
