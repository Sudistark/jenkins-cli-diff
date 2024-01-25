package lib;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/lib/test")
public interface TestTagLib extends TypedTagLibrary {
  void bar(Map paramMap, Closure paramClosure);
  
  void bar(Closure paramClosure);
  
  void bar(Map paramMap);
  
  void bar();
}
