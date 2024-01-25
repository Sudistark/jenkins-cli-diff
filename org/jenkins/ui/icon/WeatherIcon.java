package org.jenkins.ui.icon;

import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class WeatherIcon extends Icon {
  public WeatherIcon(String classSpec, String style, Status status) { super(classSpec, status.url, style, IconFormat.EXTERNAL_SVG_SPRITE); }
}
