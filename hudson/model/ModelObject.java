package hudson.model;

import jenkins.security.stapler.StaplerAccessibleType;

@StaplerAccessibleType
public interface ModelObject {
  String getDisplayName();
}
