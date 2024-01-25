package hudson.model;

import hudson.EnvVars;

public interface EnvironmentSpecific<T extends EnvironmentSpecific<T>> {
  T forEnvironment(EnvVars paramEnvVars);
}
