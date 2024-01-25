package hudson.model.labels;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import hudson.model.Label;
import hudson.model.queue.SubTask;

public interface LabelAssignmentAction extends Action {
  Label getAssignedLabel(@NonNull SubTask paramSubTask);
}
