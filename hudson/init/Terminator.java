package hudson.init;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jvnet.hudson.annotation_indexer.Indexed;

@Indexed
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Terminator {
  TermMilestone after() default TermMilestone.STARTED;
  
  TermMilestone before() default TermMilestone.COMPLETED;
  
  String[] requires() default {};
  
  String[] attains() default {};
  
  String displayName() default "";
}
