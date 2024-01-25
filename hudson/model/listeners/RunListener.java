package hudson.model.listeners;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionListView;
import hudson.ExtensionPoint;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Environment;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.CopyOnWriteList;
import java.io.IOException;
import java.lang.reflect.Type;
import jenkins.util.Listeners;
import org.jvnet.tiger_types.Types;

public abstract class RunListener<R extends Run> extends Object implements ExtensionPoint {
  public final Class<R> targetType;
  
  protected RunListener(Class<R> targetType) { this.targetType = targetType; }
  
  protected RunListener() {
    Type type = Types.getBaseClass(getClass(), RunListener.class);
    if (type instanceof java.lang.reflect.ParameterizedType) {
      this.targetType = Types.erasure(Types.getTypeArgument(type, 0));
    } else {
      throw new IllegalStateException("" + getClass() + " uses the raw type for extending RunListener");
    } 
  }
  
  public void onCompleted(R r, @NonNull TaskListener listener) {}
  
  public void onFinalized(R r) {}
  
  public void onInitialize(R r) {}
  
  public void onStarted(R r, TaskListener listener) {}
  
  public Environment setUpEnvironment(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException, Run.RunnerAbortedException { return new Object(this); }
  
  public void onDeleted(R r) {}
  
  @Deprecated
  public void register() { all().add(this); }
  
  public void unregister() { all().remove(this); }
  
  @Deprecated
  public static final CopyOnWriteList<RunListener> LISTENERS = ExtensionListView.createCopyOnWriteList(RunListener.class);
  
  public static void fireCompleted(Run r, @NonNull TaskListener listener) {
    Listeners.notify(RunListener.class, true, l -> {
          if (l.targetType.isInstance(r))
            l.onCompleted(r, listener); 
        });
  }
  
  public static void fireInitialize(Run r) {
    Listeners.notify(RunListener.class, true, l -> {
          if (l.targetType.isInstance(r))
            l.onInitialize(r); 
        });
  }
  
  public static void fireStarted(Run r, TaskListener listener) {
    Listeners.notify(RunListener.class, true, l -> {
          if (l.targetType.isInstance(r))
            l.onStarted(r, listener); 
        });
  }
  
  public static void fireFinalized(Run r) {
    if (!Functions.isExtensionsAvailable())
      return; 
    Listeners.notify(RunListener.class, true, l -> {
          if (l.targetType.isInstance(r))
            l.onFinalized(r); 
        });
  }
  
  public static void fireDeleted(Run r) {
    Listeners.notify(RunListener.class, true, l -> {
          if (l.targetType.isInstance(r))
            l.onDeleted(r); 
        });
  }
  
  public static ExtensionList<RunListener> all() { return ExtensionList.lookup(RunListener.class); }
}
