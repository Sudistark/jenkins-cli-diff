package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.Collection;
import java.util.Collections;

@Deprecated
public abstract class TransientBuildActionFactory implements ExtensionPoint {
  public Collection<? extends Action> createFor(Run target) {
    if (target instanceof AbstractBuild)
      return createFor((AbstractBuild)target); 
    return Collections.emptyList();
  }
  
  @Deprecated
  public Collection<? extends Action> createFor(AbstractBuild target) { return Collections.emptyList(); }
  
  public static ExtensionList<TransientBuildActionFactory> all() { return ExtensionList.lookup(TransientBuildActionFactory.class); }
}
