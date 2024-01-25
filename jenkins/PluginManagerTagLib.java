package jenkins;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/hudson/PluginManager")
public interface PluginManagerTagLib extends TypedTagLibrary {
  void updates(Map paramMap, Closure paramClosure);
  
  void updates(Closure paramClosure);
  
  void updates(Map paramMap);
  
  void updates();
  
  void available(Map paramMap, Closure paramClosure);
  
  void available(Closure paramClosure);
  
  void available(Map paramMap);
  
  void available();
  
  void _api(Map paramMap, Closure paramClosure);
  
  void _api(Closure paramClosure);
  
  void _api(Map paramMap);
  
  void _api();
  
  void installed(Map paramMap, Closure paramClosure);
  
  void installed(Closure paramClosure);
  
  void installed(Map paramMap);
  
  void installed();
  
  void advanced(Map paramMap, Closure paramClosure);
  
  void advanced(Closure paramClosure);
  
  void advanced(Map paramMap);
  
  void advanced();
  
  void check(Map paramMap, Closure paramClosure);
  
  void check(Closure paramClosure);
  
  void check(Map paramMap);
  
  void check();
  
  void index(Map paramMap, Closure paramClosure);
  
  void index(Closure paramClosure);
  
  void index(Map paramMap);
  
  void index();
  
  void sidepanel(Map paramMap, Closure paramClosure);
  
  void sidepanel(Closure paramClosure);
  
  void sidepanel(Map paramMap);
  
  void sidepanel();
}
