package jenkins.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Executor;
import net.jcip.annotations.GuardedBy;
import org.kohsuke.accmod.Restricted;

public abstract class AsynchronousExecution extends RuntimeException {
  @GuardedBy("this")
  private Executor executor;
  
  @GuardedBy("this")
  @CheckForNull
  private Throwable result;
  
  public abstract void interrupt(boolean paramBoolean);
  
  public abstract boolean blocksRestart();
  
  public abstract boolean displayCell();
  
  @CheckForNull
  public final Executor getExecutor() { return this.executor; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public final void setExecutorWithoutCompleting(@NonNull Executor executor) {
    assert this.executor == null;
    this.executor = executor;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public final void maybeComplete() {
    assert this.executor != null;
    if (this.result != null) {
      this.executor.completedAsynchronous((this.result != NULL) ? this.result : null);
      this.result = null;
    } 
  }
  
  public final void completed(@CheckForNull Throwable error) {
    if (this.executor != null) {
      this.executor.completedAsynchronous(error);
    } else {
      this.result = (error == null) ? NULL : error;
    } 
  }
  
  private static final Throwable NULL = new Throwable("NULL");
}
