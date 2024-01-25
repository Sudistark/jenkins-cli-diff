package hudson.model;

import java.io.PrintStream;
import java.util.List;

public interface BuildListener extends TaskListener {
  default void started(List<Cause> causes) {
    PrintStream l = getLogger();
    if (causes == null || causes.isEmpty()) {
      l.println("Started");
    } else {
      for (Cause cause : causes)
        cause.print(this); 
    } 
  }
  
  default void finished(Result result) { getLogger().println("Finished: " + result); }
}
