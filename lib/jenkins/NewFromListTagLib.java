package lib.jenkins;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/lib/hudson/newFromList")
public interface NewFromListTagLib extends TypedTagLibrary {
  void form(Map paramMap, Closure paramClosure);
  
  void form(Closure paramClosure);
  
  void form(Map paramMap);
  
  void form();
}
