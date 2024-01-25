package jenkins.widgets;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.widgets.Widget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.kohsuke.accmod.Restricted;

public abstract class WidgetFactory<T extends HasWidgets, W extends Widget> extends Object implements ExtensionPoint {
  public abstract Class<T> type();
  
  public abstract Class<W> widgetType();
  
  @NonNull
  public abstract Collection<W> createFor(@NonNull T paramT);
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static <T extends HasWidgets, W extends Widget> Iterable<WidgetFactory<T, W>> factoriesFor(Class<T> type, Class<W> widgetType) {
    List<WidgetFactory<T, W>> result = new ArrayList<WidgetFactory<T, W>>();
    for (WidgetFactory wf : ExtensionList.lookup(WidgetFactory.class)) {
      if (wf.type().isAssignableFrom(type) && widgetType.isAssignableFrom(wf.widgetType()))
        result.add(wf); 
    } 
    return result;
  }
}
