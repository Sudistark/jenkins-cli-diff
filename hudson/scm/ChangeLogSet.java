package hudson.scm;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 999)
public abstract class ChangeLogSet<T extends ChangeLogSet.Entry> extends Object implements Iterable<T> {
  private final Run<?, ?> run;
  
  @Deprecated
  public final AbstractBuild<?, ?> build;
  
  private final RepositoryBrowser<?> browser;
  
  protected ChangeLogSet(Run<?, ?> run, RepositoryBrowser<?> browser) {
    this.run = run;
    this.build = (run instanceof AbstractBuild) ? (AbstractBuild)run : null;
    this.browser = browser;
  }
  
  @Deprecated
  protected ChangeLogSet(AbstractBuild<?, ?> build) { this(build, browserFromBuild(build)); }
  
  private static RepositoryBrowser<?> browserFromBuild(AbstractBuild<?, ?> build) {
    if (build == null)
      return null; 
    return ((AbstractProject)build.getParent()).getScm().getEffectiveBrowser();
  }
  
  public Run<?, ?> getRun() { return this.run; }
  
  public RepositoryBrowser<?> getBrowser() { return this.browser; }
  
  public abstract boolean isEmptySet();
  
  @Exported
  public final Object[] getItems() {
    List<T> r = new ArrayList<T>();
    for (Iterator iterator = iterator(); iterator.hasNext(); ) {
      T t = (T)(Entry)iterator.next();
      r.add(t);
    } 
    return r.toArray();
  }
  
  @Exported
  public String getKind() { return null; }
  
  public static ChangeLogSet<? extends Entry> createEmpty(Run build) { return new EmptyChangeLogSet(build); }
  
  @Deprecated
  public static ChangeLogSet<? extends Entry> createEmpty(AbstractBuild build) { return createEmpty(build); }
}
