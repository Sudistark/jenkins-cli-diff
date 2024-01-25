package jenkins.widgets;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.widgets.Widget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.security.stapler.StaplerAccessibleType;
import jenkins.security.stapler.StaplerDispatchable;

@StaplerAccessibleType
public interface HasWidgets {
  public static final Logger LOGGER = Logger.getLogger(HasWidgets.class.getName());
  
  default List<Widget> getWidgets() {
    List<Widget> result = new ArrayList<Widget>();
    WidgetFactory.factoriesFor(getClass(), Widget.class)
      .forEach(wf -> {
          try {
            Collection<Widget> wfResult = wf.createFor((HasWidgets)wf.type().cast(this));
            for (Widget w : wfResult) {
              if (wf.widgetType().isInstance(w)) {
                result.add(w);
                continue;
              } 
              LOGGER.log(Level.WARNING, "Widget from {0} for {1} included {2} not assignable to {3}", new Object[] { wf, this, w, wf.widgetType() });
            } 
          } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Could not load all widgets from " + wf + " for " + this, e);
          } 
        });
    return Collections.unmodifiableList(result);
  }
  
  @StaplerDispatchable
  @CheckForNull
  default Widget getWidget(String name) {
    if (name == null)
      return null; 
    return (Widget)getWidgets().stream().filter(w -> name.equals(w.getUrlName())).findFirst().orElse(null);
  }
}
