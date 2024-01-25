package jenkins.widgets;

import hudson.Functions;
import hudson.Util;
import hudson.model.BallColor;
import hudson.model.Run;
import java.util.Date;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
public class BuildListTable extends RunListProgressiveRendering {
  protected void calculate(Run<?, ?> build, JSONObject element) {
    BallColor iconColor = build.getIconColor();
    element.put("iconColorOrdinal", Integer.valueOf(iconColor.ordinal()));
    element.put("iconColorDescription", iconColor.getDescription());
    element.put("url", build.getUrl());
    element.put("iconName", build.getIconColor().getIconName());
    element.put("parentUrl", build.getParent().getUrl());
    element.put("parentFullDisplayName", Functions.breakableString(Functions.escape(build.getParent().getFullDisplayName())));
    element.put("displayName", build.getDisplayName());
    element.put("timestampString", build.getTimestampString());
    element.put("timestampString2", build.getTimestampString2());
    element.put("timestampString3", Util.XS_DATETIME_FORMATTER.format(new Date(build.getStartTimeInMillis())));
    Run.Summary buildStatusSummary = build.getBuildStatusSummary();
    element.put("buildStatusSummaryWorse", Boolean.valueOf(buildStatusSummary.isWorse));
    element.put("buildStatusSummaryMessage", buildStatusSummary.message);
  }
}
