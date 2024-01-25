package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Action;
import java.util.Collection;
import org.kohsuke.accmod.Restricted;

public abstract class TransientActionFactory<T> extends Object implements ExtensionPoint {
  public abstract Class<T> type();
  
  public Class<? extends Action> actionType() { return Action.class; }
  
  @NonNull
  public abstract Collection<? extends Action> createFor(@NonNull T paramT);
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Iterable<? extends TransientActionFactory<?>> factoriesFor(Class<?> type, Class<? extends Action> actionType) { return (Iterable)((ClassValue)((Cache)ExtensionList.lookupSingleton(Cache.class)).cache().get(type)).get(actionType); }
}
