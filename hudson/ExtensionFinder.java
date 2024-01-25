package hudson;

import hudson.model.Hudson;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import jenkins.ExtensionComponentSet;
import jenkins.ExtensionRefreshException;
import net.java.sezpoz.IndexItem;
import org.kohsuke.accmod.Restricted;

public abstract class ExtensionFinder implements ExtensionPoint {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public <T> Collection<T> findExtensions(Class<T> type, Hudson hudson) { return Collections.emptyList(); }
  
  public boolean isRefreshable() {
    try {
      return (getClass().getMethod("refresh", new Class[false]).getDeclaringClass() != ExtensionFinder.class);
    } catch (NoSuchMethodException e) {
      return false;
    } 
  }
  
  @Deprecated
  public <T> Collection<ExtensionComponent<T>> _find(Class<T> type, Hudson hudson) { return find(type, hudson); }
  
  public void scout(Class extensionType, Hudson hudson) {}
  
  Collection<Method> getMethodAndInterfaceDeclarations(Method method, Collection<Class<?>> interfaces) {
    List<Method> methods = new ArrayList<Method>();
    methods.add(method);
    Objects.requireNonNull(methods);
    interfaces.stream().map(Class::getMethods).flatMap(Arrays::stream).filter(m -> (m.getName().equals(method.getName()) && Arrays.equals(m.getParameterTypes(), method.getParameterTypes()))).findFirst().ifPresent(methods::add);
    return methods;
  }
  
  private static Class<?> getClassFromIndex(IndexItem<Extension, Object> item) throws InstantiationException {
    Class<?> extType;
    AnnotatedElement e = item.element();
    if (e instanceof Class) {
      extType = (Class)e;
    } else if (e instanceof Field) {
      extType = ((Field)e).getType();
    } else if (e instanceof Method) {
      extType = ((Method)e).getReturnType();
    } else {
      throw new AssertionError();
    } 
    return extType;
  }
  
  private static final Logger LOGGER = Logger.getLogger(ExtensionFinder.class.getName());
  
  public abstract ExtensionComponentSet refresh() throws ExtensionRefreshException;
  
  public abstract <T> Collection<ExtensionComponent<T>> find(Class<T> paramClass, Hudson paramHudson);
}
