package jenkins.util;

import java.util.Locale;
import org.jvnet.localizer.Localizable;

public class NonLocalizable extends Localizable {
  private final String nonLocalizable;
  
  public NonLocalizable(String nonLocalizable) {
    super(null, null, new Object[0]);
    this.nonLocalizable = nonLocalizable;
  }
  
  public String toString(Locale locale) { return this.nonLocalizable; }
  
  public String toString() { return this.nonLocalizable; }
}
