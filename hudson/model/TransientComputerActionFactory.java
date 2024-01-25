package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class TransientComputerActionFactory implements ExtensionPoint {
  public abstract Collection<? extends Action> createFor(Computer paramComputer);
  
  public static ExtensionList<TransientComputerActionFactory> all() { return ExtensionList.lookup(TransientComputerActionFactory.class); }
  
  public static List<Action> createAllFor(Computer target) {
    List<Action> result = new ArrayList<Action>();
    for (TransientComputerActionFactory f : all())
      result.addAll(f.createFor(target)); 
    return result;
  }
}
