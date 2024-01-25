package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Queue;
import java.util.Calendar;
import java.util.Collections;
import org.acegisecurity.Authentication;
import org.springframework.security.core.Authentication;

public abstract class QueueItemAuthenticator extends AbstractDescribableImpl<QueueItemAuthenticator> implements ExtensionPoint {
  @CheckForNull
  public Authentication authenticate2(Queue.Item item) {
    if (Util.isOverridden(QueueItemAuthenticator.class, getClass(), "authenticate2", new Class[] { Queue.Task.class }))
      return authenticate2(item.task); 
    if (Util.isOverridden(QueueItemAuthenticator.class, getClass(), "authenticate", new Class[] { Queue.Task.class })) {
      Authentication a = authenticate(item.task);
      return (a != null) ? a.toSpring() : null;
    } 
    if (Util.isOverridden(QueueItemAuthenticator.class, getClass(), "authenticate", new Class[] { Queue.Item.class })) {
      Authentication a = authenticate(item);
      return (a != null) ? a.toSpring() : null;
    } 
    throw new AbstractMethodError("you must override at least one of the QueueItemAuthenticator.authenticate2 methods");
  }
  
  @CheckForNull
  public Authentication authenticate2(Queue.Task task) {
    if (Util.isOverridden(QueueItemAuthenticator.class, getClass(), "authenticate2", new Class[] { Queue.Item.class }))
      return authenticate2(new Queue.WaitingItem(Calendar.getInstance(), task, Collections.emptyList())); 
    if (Util.isOverridden(QueueItemAuthenticator.class, getClass(), "authenticate", new Class[] { Queue.Item.class })) {
      Authentication a = authenticate(new Queue.WaitingItem(Calendar.getInstance(), task, Collections.emptyList()));
      return (a != null) ? a.toSpring() : null;
    } 
    if (Util.isOverridden(QueueItemAuthenticator.class, getClass(), "authenticate", new Class[] { Queue.Task.class })) {
      Authentication a = authenticate(task);
      return (a != null) ? a.toSpring() : null;
    } 
    throw new AbstractMethodError("you must override at least one of the QueueItemAuthenticator.authenticate2 methods");
  }
  
  @Deprecated
  @CheckForNull
  public Authentication authenticate(Queue.Item item) {
    Authentication a = authenticate2(item);
    return (a != null) ? Authentication.fromSpring(a) : null;
  }
  
  @Deprecated
  @CheckForNull
  public Authentication authenticate(Queue.Task task) {
    Authentication a = authenticate2(task);
    return (a != null) ? Authentication.fromSpring(a) : null;
  }
  
  public QueueItemAuthenticatorDescriptor getDescriptor() { return (QueueItemAuthenticatorDescriptor)super.getDescriptor(); }
}
