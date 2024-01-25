package hudson.model.queue;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractProject;
import java.util.Collection;
import java.util.Collections;

public abstract class SubTaskContributor implements ExtensionPoint {
  public Collection<? extends SubTask> forProject(AbstractProject<?, ?> p) { return Collections.emptyList(); }
  
  public static ExtensionList<SubTaskContributor> all() { return ExtensionList.lookup(SubTaskContributor.class); }
}
