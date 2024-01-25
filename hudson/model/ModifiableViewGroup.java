package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;

public interface ModifiableViewGroup extends ViewGroup {
  void addView(@NonNull View paramView) throws IOException;
}
