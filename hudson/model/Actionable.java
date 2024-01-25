package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.ModelObjectWithContextMenu;
import jenkins.model.TransientActionFactory;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class Actionable extends AbstractModelObject implements ModelObjectWithContextMenu {
  @Deprecated
  @NonNull
  public List<Action> getActions() {
    if (this.actions == null)
      synchronized (this) {
        if (this.actions == null)
          this.actions = new CopyOnWriteArrayList(); 
      }  
    return this.actions;
  }
  
  @Exported(name = "actions")
  @NonNull
  public final List<? extends Action> getAllActions() {
    List<Action> _actions = getActions();
    boolean adding = false;
    for (TransientActionFactory<?> taf : TransientActionFactory.factoriesFor(getClass(), Action.class)) {
      Collection<? extends Action> additions = createFor(taf);
      if (!additions.isEmpty()) {
        if (!adding) {
          adding = true;
          _actions = new ArrayList<Action>(_actions);
        } 
        _actions.addAll(additions);
      } 
    } 
    return Collections.unmodifiableList(_actions);
  }
  
  private <T> Collection<? extends Action> createFor(TransientActionFactory<T> taf) {
    try {
      Collection<? extends Action> result = taf.createFor(taf.type().cast(this));
      for (Action a : result) {
        if (!taf.actionType().isInstance(a)) {
          LOGGER.log(Level.WARNING, "Actions from {0} for {1} included {2} not assignable to {3}", new Object[] { taf, this, a, taf.actionType() });
          return Collections.emptySet();
        } 
      } 
      return result;
    } catch (RuntimeException e) {
      LOGGER.log(Level.WARNING, "Could not load actions from " + taf + " for " + this, e);
      return Collections.emptySet();
    } 
  }
  
  @NonNull
  public <T extends Action> List<T> getActions(Class<T> type) {
    List<T> _actions = Util.filter(getActions(), type);
    for (TransientActionFactory<?> taf : TransientActionFactory.factoriesFor(getClass(), type))
      _actions.addAll(Util.filter(createFor(taf), type)); 
    return Collections.unmodifiableList(_actions);
  }
  
  public void addAction(@NonNull Action a) {
    if (a == null)
      throw new IllegalArgumentException("Action must be non-null"); 
    getActions().add(a);
  }
  
  public void replaceAction(@NonNull Action a) { addOrReplaceAction(a); }
  
  public boolean addOrReplaceAction(@NonNull Action a) {
    if (a == null)
      throw new IllegalArgumentException("Action must be non-null"); 
    List<Action> old = new ArrayList<Action>(1);
    List<Action> current = getActions();
    boolean found = false;
    for (Action a2 : current) {
      if (!found && a.equals(a2)) {
        found = true;
        continue;
      } 
      if (a2.getClass() == a.getClass())
        old.add(a2); 
    } 
    current.removeAll(old);
    if (!found)
      addAction(a); 
    return (!found || !old.isEmpty());
  }
  
  public boolean removeAction(@Nullable Action a) {
    if (a == null)
      return false; 
    return getActions().removeAll(Set.of(a));
  }
  
  public boolean removeActions(@NonNull Class<? extends Action> clazz) {
    if (clazz == null)
      throw new IllegalArgumentException("Action type must be non-null"); 
    List<Action> old = new ArrayList<Action>();
    List<Action> current = getActions();
    for (Action a : current) {
      if (clazz.isInstance(a))
        old.add(a); 
    } 
    return current.removeAll(old);
  }
  
  public boolean replaceActions(@NonNull Class<? extends Action> clazz, @NonNull Action a) {
    if (clazz == null)
      throw new IllegalArgumentException("Action type must be non-null"); 
    if (a == null)
      throw new IllegalArgumentException("Action must be non-null"); 
    List<Action> old = new ArrayList<Action>();
    List<Action> current = getActions();
    boolean found = false;
    for (Action a1 : current) {
      if (!found) {
        if (a.equals(a1)) {
          found = true;
          continue;
        } 
        if (clazz.isInstance(a1))
          old.add(a1); 
        continue;
      } 
      if (clazz.isInstance(a1) && !a.equals(a1))
        old.add(a1); 
    } 
    current.removeAll(old);
    if (!found)
      addAction(a); 
    return (!old.isEmpty() || !found);
  }
  
  @Deprecated
  public Action getAction(int index) {
    if (this.actions == null)
      return null; 
    return (Action)this.actions.get(index);
  }
  
  public <T extends Action> T getAction(Class<T> type) {
    for (Action a : getActions()) {
      if (type.isInstance(a))
        return (T)(Action)type.cast(a); 
    } 
    for (TransientActionFactory<?> taf : TransientActionFactory.factoriesFor(getClass(), type)) {
      for (Action a : createFor(taf)) {
        if (type.isInstance(a))
          return (T)(Action)type.cast(a); 
      } 
    } 
    return null;
  }
  
  public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
    for (Action a : getAllActions()) {
      if (a == null)
        continue; 
      String urlName = a.getUrlName();
      if (urlName == null)
        continue; 
      if (urlName.equals(token))
        return a; 
    } 
    return null;
  }
  
  public ModelObjectWithContextMenu.ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws Exception { return (new ModelObjectWithContextMenu.ContextMenu()).from(this, request, response); }
  
  private static final Logger LOGGER = Logger.getLogger(Actionable.class.getName());
}
