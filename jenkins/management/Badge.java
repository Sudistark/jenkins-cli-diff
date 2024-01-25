package jenkins.management;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Locale;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class Badge {
  private final String text;
  
  private final String tooltip;
  
  private final Severity severity;
  
  public Badge(@NonNull String text, @NonNull String tooltip, @NonNull Severity severity) {
    this.text = text;
    this.tooltip = tooltip;
    this.severity = severity;
  }
  
  @Exported(visibility = 999)
  public String getText() { return this.text; }
  
  @Exported(visibility = 999)
  public String getTooltip() { return this.tooltip; }
  
  public String getSeverity() { return this.severity.toString().toLowerCase(Locale.US); }
}
