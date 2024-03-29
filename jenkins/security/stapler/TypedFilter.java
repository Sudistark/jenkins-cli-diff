package jenkins.security.stapler;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.FunctionList;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.lang.FieldRef;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class TypedFilter implements FieldRef.Filter, FunctionList.Filter {
  private static final Logger LOGGER = Logger.getLogger(TypedFilter.class.getName());
  
  private static final Map<Class<?>, Boolean> staplerCache = new HashMap();
  
  private boolean isClassAcceptable(Class<?> clazz) {
    if (clazz.isArray()) {
      Class<?> elementClazz = clazz.getComponentType();
      if (isClassAcceptable(elementClazz)) {
        LOGGER.log(Level.FINE, "Class {0} is acceptable because it is an Array of acceptable elements {1}", new Object[] { clazz
              
              .getName(), elementClazz.getName() });
        return true;
      } 
      LOGGER.log(Level.FINE, "Class {0} is not acceptable because it is an Array of non-acceptable elements {1}", new Object[] { clazz
            
            .getName(), elementClazz.getName() });
      return false;
    } 
    return (SKIP_TYPE_CHECK || isStaplerRelevantCached(clazz));
  }
  
  private static boolean isStaplerRelevantCached(@NonNull Class<?> clazz) {
    if (staplerCache.containsKey(clazz))
      return ((Boolean)staplerCache.get(clazz)).booleanValue(); 
    boolean ret = isStaplerRelevant(clazz);
    staplerCache.put(clazz, Boolean.valueOf(ret));
    return ret;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static boolean isStaplerRelevant(@NonNull Class<?> clazz) { return (isSpecificClassStaplerRelevant(clazz) || isSuperTypesStaplerRelevant(clazz)); }
  
  private static boolean isSuperTypesStaplerRelevant(@NonNull Class<?> clazz) {
    Class<?> superclass = clazz.getSuperclass();
    if (superclass != null && isStaplerRelevantCached(superclass))
      return true; 
    for (Class<?> interfaceClass : clazz.getInterfaces()) {
      if (isStaplerRelevantCached(interfaceClass))
        return true; 
    } 
    return false;
  }
  
  private static boolean isSpecificClassStaplerRelevant(@NonNull Class<?> clazz) {
    if (clazz.isAnnotationPresent(StaplerAccessibleType.class))
      return true; 
    if (org.kohsuke.stapler.StaplerProxy.class.isAssignableFrom(clazz))
      return true; 
    if (org.kohsuke.stapler.StaplerFallback.class.isAssignableFrom(clazz))
      return true; 
    if (org.kohsuke.stapler.StaplerOverridable.class.isAssignableFrom(clazz))
      return true; 
    for (Method m : clazz.getMethods()) {
      if (isRoutableMethod(m))
        return true; 
    } 
    return false;
  }
  
  private static boolean isRoutableMethod(@NonNull Method m) {
    for (Annotation a : m.getDeclaredAnnotations()) {
      if (WebMethodConstants.WEB_METHOD_ANNOTATION_NAMES.contains(a.annotationType().getName()))
        return true; 
      if (a.annotationType().isAnnotationPresent(org.kohsuke.stapler.interceptor.InterceptorAnnotation.class))
        return true; 
    } 
    for (Annotation[] set : m.getParameterAnnotations()) {
      for (Annotation a : set) {
        if (WebMethodConstants.WEB_METHOD_PARAMETER_ANNOTATION_NAMES.contains(a.annotationType().getName()))
          return true; 
      } 
    } 
    for (Class<?> parameterType : m.getParameterTypes()) {
      if (WebMethodConstants.WEB_METHOD_PARAMETERS_NAMES.contains(parameterType.getName()))
        return true; 
    } 
    return WebApp.getCurrent().getFilterForDoActions().keep(new Function.InstanceFunction(m));
  }
  
  public boolean keep(@NonNull FieldRef fieldRef) {
    if (fieldRef.getAnnotation(StaplerNotDispatchable.class) != null)
      return false; 
    if (fieldRef.getAnnotation(StaplerDispatchable.class) != null)
      return true; 
    String signature = fieldRef.getSignature();
    ExtensionList<RoutingDecisionProvider> decisionProviders = ExtensionList.lookup(RoutingDecisionProvider.class);
    if (decisionProviders.size() > 0)
      for (RoutingDecisionProvider provider : decisionProviders) {
        RoutingDecisionProvider.Decision fieldDecision = provider.decide(signature);
        if (fieldDecision == RoutingDecisionProvider.Decision.ACCEPTED) {
          LOGGER.log(Level.CONFIG, "Field {0} is acceptable because it is whitelisted by {1}", new Object[] { signature, provider });
          return true;
        } 
        if (fieldDecision == RoutingDecisionProvider.Decision.REJECTED) {
          LOGGER.log(Level.CONFIG, "Field {0} is not acceptable because it is blacklisted by {1}", new Object[] { signature, provider });
          return false;
        } 
        Class<?> type = fieldRef.getReturnType();
        if (type != null) {
          String typeSignature = "class " + type.getCanonicalName();
          RoutingDecisionProvider.Decision fieldTypeDecision = provider.decide(typeSignature);
          if (fieldTypeDecision == RoutingDecisionProvider.Decision.ACCEPTED) {
            LOGGER.log(Level.CONFIG, "Field {0} is acceptable because its type is whitelisted by {1}", new Object[] { signature, provider });
            return true;
          } 
          if (fieldTypeDecision == RoutingDecisionProvider.Decision.REJECTED) {
            LOGGER.log(Level.CONFIG, "Field {0} is not acceptable because its type is blacklisted by {1}", new Object[] { signature, provider });
            return false;
          } 
        } 
      }  
    if (PROHIBIT_STATIC_ACCESS && fieldRef.isStatic())
      return false; 
    Class<?> returnType = fieldRef.getReturnType();
    boolean isOk = isClassAcceptable(returnType);
    LOGGER.log(Level.FINE, "Field analyzed: {0} => {1}", new Object[] { fieldRef.getName(), Boolean.valueOf(isOk) });
    return isOk;
  }
  
  public boolean keep(@NonNull Function function) {
    if (function.getAnnotation(StaplerNotDispatchable.class) != null)
      return false; 
    if (function.getAnnotation(StaplerDispatchable.class) != null)
      return true; 
    String signature = function.getSignature();
    ExtensionList<RoutingDecisionProvider> decision = ExtensionList.lookup(RoutingDecisionProvider.class);
    if (decision.size() > 0)
      for (RoutingDecisionProvider provider : decision) {
        RoutingDecisionProvider.Decision methodDecision = provider.decide(signature);
        if (methodDecision == RoutingDecisionProvider.Decision.ACCEPTED) {
          LOGGER.log(Level.CONFIG, "Function {0} is acceptable because it is whitelisted by {1}", new Object[] { signature, provider });
          return true;
        } 
        if (methodDecision == RoutingDecisionProvider.Decision.REJECTED) {
          LOGGER.log(Level.CONFIG, "Function {0} is not acceptable because it is blacklisted by {1}", new Object[] { signature, provider });
          return false;
        } 
        Class<?> type = function.getReturnType();
        if (type != null) {
          String typeSignature = "class " + type.getCanonicalName();
          RoutingDecisionProvider.Decision returnTypeDecision = provider.decide(typeSignature);
          if (returnTypeDecision == RoutingDecisionProvider.Decision.ACCEPTED) {
            LOGGER.log(Level.CONFIG, "Function {0} is acceptable because its type is whitelisted by {1}", new Object[] { signature, provider });
            return true;
          } 
          if (returnTypeDecision == RoutingDecisionProvider.Decision.REJECTED) {
            LOGGER.log(Level.CONFIG, "Function {0} is not acceptable because its type is blacklisted by {1}", new Object[] { signature, provider });
            return false;
          } 
        } 
      }  
    if (PROHIBIT_STATIC_ACCESS && function.isStatic())
      return false; 
    if (function.getName().equals("getDynamic")) {
      Class[] parameterTypes = function.getParameterTypes();
      if (parameterTypes.length > 0 && parameterTypes[false] == String.class)
        return false; 
    } 
    if (function.getName().equals("getStaplerFallback") && function.getParameterTypes().length == 0)
      return false; 
    if (function.getName().equals("getTarget") && function.getParameterTypes().length == 0)
      return false; 
    Class<?> returnType = function.getReturnType();
    boolean isOk = isClassAcceptable(returnType);
    LOGGER.log(Level.FINE, "Function analyzed: {0} => {1}", new Object[] { signature, Boolean.valueOf(isOk) });
    return isOk;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_TYPE_CHECK = SystemProperties.getBoolean(TypedFilter.class.getName() + ".skipTypeCheck");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean PROHIBIT_STATIC_ACCESS = SystemProperties.getBoolean(TypedFilter.class.getName() + ".prohibitStaticAccess", true);
}
