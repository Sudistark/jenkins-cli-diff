package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.Collection;

public abstract class TransientProjectActionFactory implements ExtensionPoint {
  public abstract Collection<? extends Action> createFor(AbstractProject paramAbstractProject);
  
  public static ExtensionList<TransientProjectActionFactory> all() { return ExtensionList.lookup(TransientProjectActionFactory.class); }
}
