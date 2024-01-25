package jenkins.security.stapler;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
final class WebMethodConstants {
  private static final List<Class<?>> WEB_METHOD_PARAMETERS = List.of(org.kohsuke.stapler.StaplerRequest.class, javax.servlet.http.HttpServletRequest.class, org.kohsuke.stapler.StaplerResponse.class, javax.servlet.http.HttpServletResponse.class);
  
  static final Set<String> WEB_METHOD_PARAMETERS_NAMES = Collections.unmodifiableSet((Set)WEB_METHOD_PARAMETERS
      .stream()
      .map(Class::getName)
      .collect(Collectors.toSet()));
  
  static final List<Class<? extends Annotation>> WEB_METHOD_ANNOTATIONS = List.of(org.kohsuke.stapler.WebMethod.class);
  
  static final Set<String> WEB_METHOD_ANNOTATION_NAMES;
  
  private static final List<Class<? extends Annotation>> WEB_METHOD_PARAMETER_ANNOTATIONS;
  
  static final Set<String> WEB_METHOD_PARAMETER_ANNOTATION_NAMES;
  
  static  {
    webMethodAnnotationNames = (Set)WEB_METHOD_ANNOTATIONS.stream().map(Class::getName).collect(Collectors.toSet());
    webMethodAnnotationNames.add(org.kohsuke.stapler.bind.JavaScriptMethod.class.getName());
    WEB_METHOD_ANNOTATION_NAMES = Collections.unmodifiableSet(webMethodAnnotationNames);
    WEB_METHOD_PARAMETER_ANNOTATIONS = List.of(org.kohsuke.stapler.QueryParameter.class, org.kohsuke.stapler.AncestorInPath.class, org.kohsuke.stapler.Header.class, org.kohsuke.stapler.json.JsonBody.class, org.kohsuke.stapler.json.SubmittedForm.class);
    WEB_METHOD_PARAMETER_ANNOTATION_NAMES = Collections.unmodifiableSet((Set)WEB_METHOD_PARAMETER_ANNOTATIONS
        .stream()
        .map(Class::getName)
        .collect(Collectors.toSet()));
  }
}
