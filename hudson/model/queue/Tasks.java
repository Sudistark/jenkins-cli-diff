package hudson.model.queue;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Item;
import hudson.model.Queue;
import java.util.Collection;
import jenkins.security.QueueItemAuthenticator;
import jenkins.security.QueueItemAuthenticatorProvider;
import org.acegisecurity.Authentication;
import org.springframework.security.core.Authentication;

public class Tasks {
  @Deprecated
  public static Collection<? extends SubTask> getSubTasksOf(Queue.Task task) { return task.getSubTasks(); }
  
  @Deprecated
  public static Object getSameNodeConstraintOf(SubTask t) { return t.getSameNodeConstraint(); }
  
  @Deprecated
  @NonNull
  public static Queue.Task getOwnerTaskOf(@NonNull SubTask t) { return t.getOwnerTask(); }
  
  @CheckForNull
  public static Item getItemOf(@NonNull SubTask t) {
    Queue.Task p = t.getOwnerTask();
    while (!(p instanceof Item)) {
      Queue.Task o = p.getOwnerTask();
      if (o == p)
        break; 
      p = o;
    } 
    return (p instanceof Item) ? (Item)p : null;
  }
  
  @Deprecated
  @NonNull
  public static Authentication getDefaultAuthenticationOf(Queue.Task t) { return t.getDefaultAuthentication(); }
  
  @Deprecated
  @NonNull
  public static Authentication getDefaultAuthenticationOf(Queue.Task t, Queue.Item item) { return t.getDefaultAuthentication(item); }
  
  @NonNull
  public static Authentication getAuthenticationOf2(@NonNull Queue.Task t) {
    for (QueueItemAuthenticator qia : QueueItemAuthenticatorProvider.authenticators()) {
      Authentication a = qia.authenticate2(t);
      if (a != null)
        return a; 
    } 
    return t.getDefaultAuthentication2();
  }
  
  @Deprecated
  @NonNull
  public static Authentication getAuthenticationOf(@NonNull Queue.Task t) { return Authentication.fromSpring(getAuthenticationOf2(t)); }
}
