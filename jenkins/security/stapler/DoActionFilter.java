package jenkins.security.stapler;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import java.lang.annotation.Annotation;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.FunctionList;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class DoActionFilter implements FunctionList.Filter {
  private static final Logger LOGGER = Logger.getLogger(DoActionFilter.class.getName());
  
  private static final Pattern DO_METHOD_REGEX = Pattern.compile("^do[^a-z].*");
  
  public boolean keep(@NonNull Function m) {
    if (m.getAnnotation(StaplerNotDispatchable.class) != null)
      return false; 
    if (m.getAnnotation(StaplerDispatchable.class) != null)
      return true; 
    String methodName = m.getName();
    String signature = m.getSignature();
    ExtensionList<RoutingDecisionProvider> whitelistProviders = ExtensionList.lookup(RoutingDecisionProvider.class);
    if (whitelistProviders.size() > 0)
      for (RoutingDecisionProvider provider : whitelistProviders) {
        RoutingDecisionProvider.Decision methodDecision = provider.decide(signature);
        if (methodDecision == RoutingDecisionProvider.Decision.ACCEPTED) {
          LOGGER.log(Level.CONFIG, "Action " + signature + " is acceptable because it is whitelisted by " + provider);
          return true;
        } 
        if (methodDecision == RoutingDecisionProvider.Decision.REJECTED) {
          LOGGER.log(Level.CONFIG, "Action " + signature + " is not acceptable because it is blacklisted by " + provider);
          return false;
        } 
      }  
    if (methodName.equals("doDynamic"))
      return false; 
    for (Annotation a : m.getAnnotations()) {
      if (WebMethodConstants.WEB_METHOD_ANNOTATION_NAMES.contains(a.annotationType().getName()))
        return true; 
      if (a.annotationType().getAnnotation(org.kohsuke.stapler.interceptor.InterceptorAnnotation.class) != null)
        return true; 
    } 
    for (Annotation[] perParameterAnnotation : m.getParameterAnnotations()) {
      for (Annotation annotation : perParameterAnnotation) {
        if (WebMethodConstants.WEB_METHOD_PARAMETER_ANNOTATION_NAMES.contains(annotation.annotationType().getName()))
          return true; 
      } 
    } 
    if (!DO_METHOD_REGEX.matcher(methodName).matches())
      return false; 
    for (Class<?> parameterType : m.getParameterTypes()) {
      if (WebMethodConstants.WEB_METHOD_PARAMETERS_NAMES.contains(parameterType.getName()))
        return true; 
    } 
    Class<?> returnType = m.getReturnType();
    if (org.kohsuke.stapler.HttpResponse.class.isAssignableFrom(returnType))
      return true; 
    Class[] checkedExceptionTypes = m.getCheckedExceptionTypes();
    for (Class<?> checkedExceptionType : checkedExceptionTypes) {
      if (org.kohsuke.stapler.HttpResponse.class.isAssignableFrom(checkedExceptionType))
        return true; 
    } 
    return false;
  }
}
