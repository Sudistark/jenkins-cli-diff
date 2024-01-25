package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.labels.LabelAtom;
import java.util.Collection;

public abstract class LabelFinder implements ExtensionPoint {
  public static ExtensionList<LabelFinder> all() { return ExtensionList.lookup(LabelFinder.class); }
  
  @NonNull
  public abstract Collection<LabelAtom> findLabels(@NonNull Node paramNode);
}
