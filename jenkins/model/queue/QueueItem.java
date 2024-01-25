package jenkins.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.ModelObject;
import hudson.model.Queue;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.Beta.class})
public interface QueueItem extends ModelObject {
  boolean isStuck();
  
  @NonNull
  Queue.Task getTask();
  
  default boolean hasCancelPermission() { return getTask().hasAbortPermission(); }
  
  long getId();
  
  @NonNull
  String getCausesDescription();
  
  @CheckForNull
  String getWhy();
  
  @NonNull
  String getParams();
  
  @NonNull
  String getInQueueForString();
  
  @CheckForNull
  default String getDisplayName() { return getTask().getFullDisplayName(); }
}
