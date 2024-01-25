package jenkins.model;

import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;

public interface DependencyDeclarer {
  void buildDependencyGraph(AbstractProject paramAbstractProject, DependencyGraph paramDependencyGraph);
}
