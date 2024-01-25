package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.ResourceList;
import java.io.IOException;
import java.util.Collection;

public abstract class QueueTaskFilter implements Queue.Task {
  private final Queue.Task base;
  
  protected QueueTaskFilter(Queue.Task base) { this.base = base; }
  
  public Label getAssignedLabel() { return this.base.getAssignedLabel(); }
  
  public Node getLastBuiltOn() { return this.base.getLastBuiltOn(); }
  
  @Deprecated
  public boolean isBuildBlocked() { return this.base.isBuildBlocked(); }
  
  @Deprecated
  public String getWhyBlocked() { return this.base.getWhyBlocked(); }
  
  public CauseOfBlockage getCauseOfBlockage() { return this.base.getCauseOfBlockage(); }
  
  public String getName() { return this.base.getName(); }
  
  public String getFullDisplayName() { return this.base.getFullDisplayName(); }
  
  public long getEstimatedDuration() { return this.base.getEstimatedDuration(); }
  
  @CheckForNull
  public Queue.Executable createExecutable() throws IOException { return this.base.createExecutable(); }
  
  public void checkAbortPermission() { this.base.checkAbortPermission(); }
  
  public boolean hasAbortPermission() { return this.base.hasAbortPermission(); }
  
  public String getUrl() { return this.base.getUrl(); }
  
  public boolean isConcurrentBuild() { return this.base.isConcurrentBuild(); }
  
  public String getDisplayName() { return this.base.getDisplayName(); }
  
  public ResourceList getResourceList() { return this.base.getResourceList(); }
  
  public Collection<? extends SubTask> getSubTasks() { return this.base.getSubTasks(); }
  
  public Object getSameNodeConstraint() { return this.base.getSameNodeConstraint(); }
}
