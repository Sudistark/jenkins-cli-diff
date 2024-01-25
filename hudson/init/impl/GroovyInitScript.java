package hudson.init.impl;

import hudson.init.InitMilestone;
import hudson.init.Initializer;
import jenkins.model.Jenkins;
import jenkins.util.groovy.GroovyHookScript;

public class GroovyInitScript {
  @Initializer(after = InitMilestone.JOB_CONFIG_ADAPTED)
  public static void init(Jenkins j) { (new GroovyHookScript("init", j.servletContext, j.getRootDir(), (j.getPluginManager()).uberClassLoader)).run(); }
}
