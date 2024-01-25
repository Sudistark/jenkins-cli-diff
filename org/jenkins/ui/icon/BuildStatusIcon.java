package org.jenkins.ui.icon;

import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class BuildStatusIcon extends Icon {
  private boolean inProgress;
  
  public BuildStatusIcon(String classSpec, String url, String style, boolean inProgress) {
    super(classSpec, url, style, IconFormat.EXTERNAL_SVG_SPRITE);
    this.inProgress = inProgress;
  }
  
  public BuildStatusIcon(String classSpec, String url, String style) { this(classSpec, url, style, false); }
  
  public boolean isSvgSprite() { return super.isSvgSprite(); }
  
  public boolean isBuildStatus() { return true; }
  
  public boolean isInProgress() { return this.inProgress; }
}
