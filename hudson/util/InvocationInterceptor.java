package hudson.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public interface InvocationInterceptor {
  Object invoke(Object paramObject, Method paramMethod, Object[] paramArrayOfObject, InvocationHandler paramInvocationHandler) throws Throwable;
}
