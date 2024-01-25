package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue;
import hudson.model.ResourceActivity;
import java.io.IOException;

public interface SubTask extends ResourceActivity {
  default Label getAssignedLabel() { return null; }
  
  default Node getLastBuiltOn() { return null; }
  
  default long getEstimatedDuration() { return -1L; }
  
  @CheckForNull
  Queue.Executable createExecutable() throws IOException;
  
  @NonNull
  default Queue.Task getOwnerTask() { return (Queue.Task)this; }
  
  @CheckForNull
  default Queue.Executable getOwnerExecutable() throws IOException { return null; }
  
  default Object getSameNodeConstraint() { return null; }
}
