package jenkins.widgets;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Computer;
import hudson.widgets.Widget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExecutorsWidget extends Widget {
  private final String ownerUrl;
  
  private final List<Computer> computers;
  
  public ExecutorsWidget(@NonNull String ownerUrl, @NonNull List<Computer> computers) {
    this.ownerUrl = ownerUrl;
    this.computers = new ArrayList(computers);
  }
  
  protected String getOwnerUrl() { return this.ownerUrl; }
  
  public List<Computer> getComputers() { return Collections.unmodifiableList(this.computers); }
}
