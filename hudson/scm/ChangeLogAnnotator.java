package hudson.scm;

import hudson.ExtensionList;
import hudson.ExtensionListView;
import hudson.ExtensionPoint;
import hudson.MarkupText;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.util.CopyOnWriteList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ChangeLogAnnotator implements ExtensionPoint {
  public void annotate(Run<?, ?> build, ChangeLogSet.Entry change, MarkupText text) {
    if (build instanceof AbstractBuild && Util.isOverridden(ChangeLogAnnotator.class, getClass(), "annotate", new Class[] { AbstractBuild.class, ChangeLogSet.Entry.class, MarkupText.class })) {
      annotate((AbstractBuild)build, change, text);
    } else {
      Logger.getLogger(ChangeLogAnnotator.class.getName()).log(Level.WARNING, "You must override the newer overload of annotate from {0}", getClass().getName());
    } 
  }
  
  @Deprecated
  public void annotate(AbstractBuild<?, ?> build, ChangeLogSet.Entry change, MarkupText text) { annotate(build, change, text); }
  
  @Deprecated
  public final void register() { all().add(this); }
  
  public final boolean unregister() { return all().remove(this); }
  
  @Deprecated
  public static final CopyOnWriteList<ChangeLogAnnotator> annotators = ExtensionListView.createCopyOnWriteList(ChangeLogAnnotator.class);
  
  public static ExtensionList<ChangeLogAnnotator> all() { return ExtensionList.lookup(ChangeLogAnnotator.class); }
}
