package org.jenkins.ui.icon;

public static enum IconType {
  CORE, PLUGIN;
  
  public String toQualifiedUrl(String url, String resURL) {
    switch (null.$SwitchMap$org$jenkins$ui$icon$IconType[ordinal()]) {
      case 1:
        return resURL + "/images/" + resURL;
      case 2:
        return resURL + "/plugin/" + resURL;
    } 
    throw new AssertionError("Unknown icon type: " + this);
  }
}
