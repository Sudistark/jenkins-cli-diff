package lib.jenkins;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagFile;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/lib/hudson/project")
public interface ProjectTagLib extends TypedTagLibrary {
  @TagFile("config-builders")
  void config_builders(Map paramMap, Closure paramClosure);
  
  @TagFile("config-builders")
  void config_builders(Closure paramClosure);
  
  @TagFile("config-builders")
  void config_builders(Map paramMap);
  
  @TagFile("config-builders")
  void config_builders();
  
  @TagFile("config-disableBuild")
  void config_disableBuild(Map paramMap, Closure paramClosure);
  
  @TagFile("config-disableBuild")
  void config_disableBuild(Closure paramClosure);
  
  @TagFile("config-disableBuild")
  void config_disableBuild(Map paramMap);
  
  @TagFile("config-disableBuild")
  void config_disableBuild();
  
  void configurable(Map paramMap, Closure paramClosure);
  
  void configurable(Closure paramClosure);
  
  void configurable(Map paramMap);
  
  void configurable();
  
  @TagFile("upstream-downstream")
  void upstream_downstream(Map paramMap, Closure paramClosure);
  
  @TagFile("upstream-downstream")
  void upstream_downstream(Closure paramClosure);
  
  @TagFile("upstream-downstream")
  void upstream_downstream(Map paramMap);
  
  @TagFile("upstream-downstream")
  void upstream_downstream();
  
  @TagFile("config-trigger")
  void config_trigger(Map paramMap, Closure paramClosure);
  
  @TagFile("config-trigger")
  void config_trigger(Closure paramClosure);
  
  @TagFile("config-trigger")
  void config_trigger(Map paramMap);
  
  @TagFile("config-trigger")
  void config_trigger();
  
  @TagFile("config-blockWhenDownstreamBuilding")
  void config_blockWhenDownstreamBuilding(Map paramMap, Closure paramClosure);
  
  @TagFile("config-blockWhenDownstreamBuilding")
  void config_blockWhenDownstreamBuilding(Closure paramClosure);
  
  @TagFile("config-blockWhenDownstreamBuilding")
  void config_blockWhenDownstreamBuilding(Map paramMap);
  
  @TagFile("config-blockWhenDownstreamBuilding")
  void config_blockWhenDownstreamBuilding();
  
  void projectActionFloatingBox(Map paramMap, Closure paramClosure);
  
  void projectActionFloatingBox(Closure paramClosure);
  
  void projectActionFloatingBox(Map paramMap);
  
  void projectActionFloatingBox();
  
  @TagFile("console-link")
  void console_link(Map paramMap, Closure paramClosure);
  
  @TagFile("console-link")
  void console_link(Closure paramClosure);
  
  @TagFile("console-link")
  void console_link(Map paramMap);
  
  @TagFile("console-link")
  void console_link();
  
  void makeDisabled(Map paramMap, Closure paramClosure);
  
  void makeDisabled(Closure paramClosure);
  
  void makeDisabled(Map paramMap);
  
  void makeDisabled();
  
  @TagFile("config-blockWhenUpstreamBuilding")
  void config_blockWhenUpstreamBuilding(Map paramMap, Closure paramClosure);
  
  @TagFile("config-blockWhenUpstreamBuilding")
  void config_blockWhenUpstreamBuilding(Closure paramClosure);
  
  @TagFile("config-blockWhenUpstreamBuilding")
  void config_blockWhenUpstreamBuilding(Map paramMap);
  
  @TagFile("config-blockWhenUpstreamBuilding")
  void config_blockWhenUpstreamBuilding();
  
  @TagFile("config-upstream-pseudo-trigger")
  void config_upstream_pseudo_trigger(Map paramMap, Closure paramClosure);
  
  @TagFile("config-upstream-pseudo-trigger")
  void config_upstream_pseudo_trigger(Closure paramClosure);
  
  @TagFile("config-upstream-pseudo-trigger")
  void config_upstream_pseudo_trigger(Map paramMap);
  
  @TagFile("config-upstream-pseudo-trigger")
  void config_upstream_pseudo_trigger();
  
  @TagFile("config-publishers")
  void config_publishers(Map paramMap, Closure paramClosure);
  
  @TagFile("config-publishers")
  void config_publishers(Closure paramClosure);
  
  @TagFile("config-publishers")
  void config_publishers(Map paramMap);
  
  @TagFile("config-publishers")
  void config_publishers();
  
  @TagFile("config-retryCount")
  void config_retryCount(Map paramMap, Closure paramClosure);
  
  @TagFile("config-retryCount")
  void config_retryCount(Closure paramClosure);
  
  @TagFile("config-retryCount")
  void config_retryCount(Map paramMap);
  
  @TagFile("config-retryCount")
  void config_retryCount();
  
  @TagFile("config-scm")
  void config_scm(Map paramMap, Closure paramClosure);
  
  @TagFile("config-scm")
  void config_scm(Closure paramClosure);
  
  @TagFile("config-scm")
  void config_scm(Map paramMap);
  
  @TagFile("config-scm")
  void config_scm();
  
  @TagFile("config-buildWrappers")
  void config_buildWrappers(Map paramMap, Closure paramClosure);
  
  @TagFile("config-buildWrappers")
  void config_buildWrappers(Closure paramClosure);
  
  @TagFile("config-buildWrappers")
  void config_buildWrappers(Map paramMap);
  
  @TagFile("config-buildWrappers")
  void config_buildWrappers();
  
  @TagFile("config-quietPeriod")
  void config_quietPeriod(Map paramMap, Closure paramClosure);
  
  @TagFile("config-quietPeriod")
  void config_quietPeriod(Closure paramClosure);
  
  @TagFile("config-quietPeriod")
  void config_quietPeriod(Map paramMap);
  
  @TagFile("config-quietPeriod")
  void config_quietPeriod();
  
  @TagFile("config-concurrentBuild")
  void config_concurrentBuild(Map paramMap, Closure paramClosure);
  
  @TagFile("config-concurrentBuild")
  void config_concurrentBuild(Closure paramClosure);
  
  @TagFile("config-concurrentBuild")
  void config_concurrentBuild(Map paramMap);
  
  @TagFile("config-concurrentBuild")
  void config_concurrentBuild();
  
  @TagFile("build-permalink")
  void build_permalink(Map paramMap, Closure paramClosure);
  
  @TagFile("build-permalink")
  void build_permalink(Closure paramClosure);
  
  @TagFile("build-permalink")
  void build_permalink(Map paramMap);
  
  @TagFile("build-permalink")
  void build_permalink();
  
  @TagFile("config-customWorkspace")
  void config_customWorkspace(Map paramMap, Closure paramClosure);
  
  @TagFile("config-customWorkspace")
  void config_customWorkspace(Closure paramClosure);
  
  @TagFile("config-customWorkspace")
  void config_customWorkspace(Map paramMap);
  
  @TagFile("config-customWorkspace")
  void config_customWorkspace();
  
  @TagFile("config-assignedLabel")
  void config_assignedLabel(Map paramMap, Closure paramClosure);
  
  @TagFile("config-assignedLabel")
  void config_assignedLabel(Closure paramClosure);
  
  @TagFile("config-assignedLabel")
  void config_assignedLabel(Map paramMap);
  
  @TagFile("config-assignedLabel")
  void config_assignedLabel();
  
  @TagFile("config-publishers2")
  void config_publishers2(Map paramMap, Closure paramClosure);
  
  @TagFile("config-publishers2")
  void config_publishers2(Closure paramClosure);
  
  @TagFile("config-publishers2")
  void config_publishers2(Map paramMap);
  
  @TagFile("config-publishers2")
  void config_publishers2();
}
