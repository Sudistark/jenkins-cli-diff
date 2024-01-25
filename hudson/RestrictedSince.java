package hudson;

import java.lang.annotation.Documented;

@Documented
public @interface RestrictedSince {
  String value();
}
