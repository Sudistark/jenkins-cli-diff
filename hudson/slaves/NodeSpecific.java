package hudson.slaves;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.IOException;

public interface NodeSpecific<T extends NodeSpecific<T>> {
  T forNode(@NonNull Node paramNode, TaskListener paramTaskListener) throws IOException, InterruptedException;
}
