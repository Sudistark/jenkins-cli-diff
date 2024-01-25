package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.Collection;
import java.util.Collections;

public abstract class TransientUserActionFactory implements ExtensionPoint {
  public Collection<? extends Action> createFor(User target) { return Collections.emptyList(); }
  
  public static ExtensionList<TransientUserActionFactory> all() { return ExtensionList.lookup(TransientUserActionFactory.class); }
}
