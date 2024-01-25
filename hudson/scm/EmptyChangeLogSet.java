package hudson.scm;

import hudson.model.Run;
import java.util.Collections;
import java.util.Iterator;

final class EmptyChangeLogSet extends ChangeLogSet<ChangeLogSet.Entry> {
  EmptyChangeLogSet(Run<?, ?> build) { super(build, new Object()); }
  
  public boolean isEmptySet() { return true; }
  
  public Iterator<ChangeLogSet.Entry> iterator() { return Collections.emptyIterator(); }
}
