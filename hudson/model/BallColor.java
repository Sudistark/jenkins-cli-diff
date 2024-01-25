package hudson.model;

import hudson.util.ColorPalette;
import java.awt.Color;
import java.util.Locale;
import jenkins.model.Jenkins;
import org.jenkins.ui.icon.Icon;
import org.jvnet.localizer.LocaleProvider;
import org.jvnet.localizer.Localizable;
import org.kohsuke.stapler.Stapler;

public static enum BallColor implements StatusIcon {
  RED("red", Messages._BallColor_Failed(), ColorPalette.RED),
  RED_ANIME("red_anime", Messages._BallColor_InProgress(), ColorPalette.RED),
  YELLOW("yellow", Messages._BallColor_Unstable(), ColorPalette.YELLOW),
  YELLOW_ANIME("yellow_anime", Messages._BallColor_InProgress(), ColorPalette.YELLOW),
  BLUE("blue", Messages._BallColor_Success(), ColorPalette.BLUE),
  BLUE_ANIME("blue_anime", Messages._BallColor_InProgress(), ColorPalette.BLUE),
  GREY("grey", Messages._BallColor_Disabled(), ColorPalette.GREY),
  GREY_ANIME("grey_anime", Messages._BallColor_InProgress(), ColorPalette.GREY),
  DISABLED("disabled", Messages._BallColor_Disabled(), ColorPalette.GREY),
  DISABLED_ANIME("disabled_anime", Messages._BallColor_InProgress(), ColorPalette.GREY),
  ABORTED("aborted", Messages._BallColor_Aborted(), ColorPalette.DARK_GREY),
  ABORTED_ANIME("aborted_anime", Messages._BallColor_InProgress(), ColorPalette.DARK_GREY),
  NOTBUILT("nobuilt", Messages._BallColor_NotBuilt(), ColorPalette.LIGHT_GREY),
  NOTBUILT_ANIME("nobuilt_anime", Messages._BallColor_InProgress(), ColorPalette.LIGHT_GREY);
  
  private final Localizable description;
  
  private final String iconName;
  
  private final String iconClassName;
  
  private final String image;
  
  private final Color baseColor;
  
  BallColor(String image, Localizable description, Color baseColor) {
    this.iconName = Icon.toNormalizedIconName(image);
    this.iconClassName = Icon.toNormalizedIconNameClass(image);
    this.baseColor = baseColor;
    this.image = image + image;
    this.description = description;
  }
  
  public String getIconName() { return this.iconName; }
  
  public String getIconClassName() { return this.iconClassName; }
  
  public String getImage() { return this.image; }
  
  public String getImageOf(String size) { return Stapler.getCurrentRequest().getContextPath() + Stapler.getCurrentRequest().getContextPath() + "/images/" + Jenkins.RESOURCE_PATH + "/" + size; }
  
  public String getDescription() { return this.description.toString(LocaleProvider.getLocale()); }
  
  public Color getBaseColor() { return this.baseColor; }
  
  public String getHtmlBaseColor() { return String.format("#%06X", new Object[] { Integer.valueOf(this.baseColor.getRGB() & 0xFFFFFF) }); }
  
  public String toString() { return name().toLowerCase(Locale.ENGLISH); }
  
  public BallColor anime() {
    if (isAnimated())
      return this; 
    return valueOf(name() + "_ANIME");
  }
  
  public BallColor noAnime() {
    if (isAnimated())
      return valueOf(name().substring(0, name().length() - "_ANIME".length())); 
    return this;
  }
  
  public boolean isAnimated() { return name().endsWith("_ANIME"); }
}
