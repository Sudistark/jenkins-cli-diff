package hudson.model;

import jenkins.model.Jenkins;
import org.jvnet.localizer.LocaleProvider;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.Stapler;

public final class StockStatusIcon extends AbstractStatusIcon {
  private final Localizable description;
  
  private final String image;
  
  public StockStatusIcon(String image, Localizable description) {
    this.image = image;
    this.description = description;
  }
  
  public String getImageOf(String size) {
    if (this.image.endsWith(".svg"))
      return Stapler.getCurrentRequest().getContextPath() + Stapler.getCurrentRequest().getContextPath() + "/images/svgs/" + Jenkins.RESOURCE_PATH; 
    return Stapler.getCurrentRequest().getContextPath() + Stapler.getCurrentRequest().getContextPath() + "/images/" + Jenkins.RESOURCE_PATH + "/" + size;
  }
  
  public String getDescription() { return this.description.toString(LocaleProvider.getLocale()); }
}
