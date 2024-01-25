package hudson.util;

import hudson.RestrictedSince;
import hudson.model.TaskListener;
import org.kohsuke.accmod.Restricted;

@Deprecated
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@RestrictedSince("2.91")
public abstract class AbstractTaskListener implements TaskListener {
  private static final long serialVersionUID = 7217626701881006422L;
}
