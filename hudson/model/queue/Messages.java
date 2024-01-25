package hudson.model.queue;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Messages {
  private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
  
  public static String QueueSorter_installDefaultQueueSorter() { return holder.format("QueueSorter.installDefaultQueueSorter", new Object[0]); }
  
  public static Localizable _QueueSorter_installDefaultQueueSorter() { return new Localizable(holder, "QueueSorter.installDefaultQueueSorter", new Object[0]); }
}
