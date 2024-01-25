package jenkins.widgets;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.View;
import hudson.widgets.Widget;
import java.util.Collection;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;

@Extension
@Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
@Deprecated
@Symbol({"jenkins"})
public final class JenkinsWidgetFactory extends WidgetFactory<View, Widget> {
  public Class<View> type() { return View.class; }
  
  public Class<Widget> widgetType() { return Widget.class; }
  
  @NonNull
  public Collection<Widget> createFor(@NonNull View target) { return Jenkins.get().getWidgets(); }
}
