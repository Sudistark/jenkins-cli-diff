package jenkins.model.labels;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionPoint;
import hudson.model.Item;
import hudson.model.Label;
import hudson.util.FormValidation;

public interface LabelValidator extends ExtensionPoint {
  @NonNull
  FormValidation check(@NonNull Item paramItem, @NonNull Label paramLabel);
}
