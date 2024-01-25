package hudson;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jenkins.YesNoMaybe;
import net.java.sezpoz.Indexable;

@Indexable
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface Extension {
  double ordinal() default 0.0D;
  
  boolean optional() default false;
  
  YesNoMaybe dynamicLoadable() default YesNoMaybe.MAYBE;
}
