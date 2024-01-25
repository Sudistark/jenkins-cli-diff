package jenkins;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/hudson/tools")
public interface ToolsTagLib extends TypedTagLibrary {
  void label(Map paramMap, Closure paramClosure);
  
  void label(Closure paramClosure);
  
  void label(Map paramMap);
  
  void label();
}
