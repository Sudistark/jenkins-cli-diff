package hudson.model;

import hudson.Util;
import hudson.util.RunList;
import java.io.IOException;
import java.util.Date;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.koshuke.stapler.simile.timeline.Event;
import org.koshuke.stapler.simile.timeline.TimelineEventList;

public class BuildTimelineWidget {
  protected final RunList<?> builds;
  
  public BuildTimelineWidget(RunList<?> builds) { this.builds = builds.limit(20); }
  
  @Deprecated
  public Run<?, ?> getFirstBuild() { return this.builds.getFirstBuild(); }
  
  @Deprecated
  public Run<?, ?> getLastBuild() { return this.builds.getLastBuild(); }
  
  public TimelineEventList doData(StaplerRequest req, @QueryParameter long min, @QueryParameter long max) throws IOException {
    TimelineEventList result = new TimelineEventList();
    for (Run<?, ?> r : this.builds.byTimestamp(min, max)) {
      Event e = new Event();
      e.start = new Date(r.getStartTimeInMillis());
      e.end = new Date(r.getStartTimeInMillis() + r.getDuration());
      e.title = Util.escape(r.getFullDisplayName()).replace("&lt;", "&#60;");
      e.link = req.getContextPath() + "/" + req.getContextPath();
      BallColor c = r.getIconColor();
      e.color = String.format("#%06X", new Object[] { Integer.valueOf(c.getBaseColor().darker().getRGB() & 0xFFFFFF) });
      e.classname = "event-" + c.noAnime().toString() + " " + (c.isAnimated() ? "animated" : "");
      result.add(e);
    } 
    return result;
  }
}
