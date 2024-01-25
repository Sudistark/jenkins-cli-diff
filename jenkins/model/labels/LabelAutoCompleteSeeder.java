package jenkins.model.labels;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class LabelAutoCompleteSeeder {
  private final String source;
  
  public LabelAutoCompleteSeeder(@NonNull String source) { this.source = source; }
  
  @NonNull
  public List<String> getSeeds() {
    ArrayList<String> terms = new ArrayList<String>();
    boolean trailingQuote = this.source.endsWith("\"");
    boolean leadingQuote = this.source.startsWith("\"");
    boolean trailingSpace = this.source.endsWith(" ");
    if (trailingQuote || (trailingSpace && !leadingQuote)) {
      terms.add("");
    } else if (leadingQuote) {
      int quote = this.source.lastIndexOf('"');
      if (quote == 0) {
        terms.add(this.source.substring(1));
      } else {
        terms.add("");
      } 
    } else {
      int space = this.source.lastIndexOf(' ');
      if (space > -1) {
        terms.add(this.source.substring(space + 1));
      } else {
        terms.add(this.source);
      } 
    } 
    return terms;
  }
}
