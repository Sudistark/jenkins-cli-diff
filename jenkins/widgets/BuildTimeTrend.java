package jenkins.widgets;

import hudson.model.AbstractBuild;
import hudson.model.BallColor;
import hudson.model.Messages;
import hudson.model.Node;
import hudson.model.Run;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
public class BuildTimeTrend extends RunListProgressiveRendering {
  protected void calculate(Run<?, ?> build, JSONObject element) {
    BallColor iconColor = build.getIconColor();
    element.put("iconName", iconColor.getIconName());
    element.put("iconColorOrdinal", Integer.valueOf(iconColor.ordinal()));
    element.put("iconColorDescription", iconColor.getDescription());
    element.put("number", Integer.valueOf(build.getNumber()));
    element.put("displayName", build.getDisplayName());
    element.put("duration", Long.valueOf(build.getDuration()));
    element.put("durationString", build.getDurationString());
    if (build instanceof AbstractBuild) {
      AbstractBuild<?, ?> b = (AbstractBuild)build;
      Node n = b.getBuiltOn();
      if (n == null) {
        String ns = b.getBuiltOnStr();
        if (ns != null && !ns.isEmpty())
          element.put("builtOnStr", ns); 
      } else if (n != Jenkins.get()) {
        element.put("builtOn", n.getNodeName());
        element.put("builtOnStr", n.getDisplayName());
      } else {
        element.put("builtOnStr", Messages.Hudson_Computer_DisplayName());
      } 
    } 
  }
}
