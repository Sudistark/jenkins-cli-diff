package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public interface ReconfigurableDescribable<T extends ReconfigurableDescribable<T>> extends Describable<T> {
  @CheckForNull
  T reconfigure(@NonNull StaplerRequest paramStaplerRequest, @CheckForNull JSONObject paramJSONObject) throws Descriptor.FormException;
}
