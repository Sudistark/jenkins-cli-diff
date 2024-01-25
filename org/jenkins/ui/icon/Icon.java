package org.jenkins.ui.icon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;

public class Icon {
  public static final String ICON_SMALL_STYLE = "width: 16px; height: 16px;";
  
  public static final String ICON_MEDIUM_STYLE = "width: 24px; height: 24px;";
  
  public static final String ICON_LARGE_STYLE = "width: 32px; height: 32px;";
  
  public static final String ICON_XLARGE_STYLE = "width: 48px; height: 48px;";
  
  private static final String[] SUPPORTED_FORMATS = { ".svg", ".png", ".gif" };
  
  private static final Map<String, String> iconDims = new HashMap();
  
  private final String classSpec;
  
  private final String normalizedSelector;
  
  private final String url;
  
  private final String style;
  
  private IconType iconType;
  
  private IconFormat iconFormat;
  
  static  {
    iconDims.put("16x16", "icon-sm");
    iconDims.put("24x24", "icon-md");
    iconDims.put("32x32", "icon-lg");
    iconDims.put("48x48", "icon-xlg");
  }
  
  public Icon(String classSpec, String style) { this(classSpec, null, style, IconType.CORE); }
  
  public Icon(String classSpec, String url, String style) {
    this(classSpec, url, style, IconType.CORE);
    if (url != null)
      if (url.startsWith("images/")) {
        this.iconType = IconType.CORE;
      } else if (url.startsWith("plugin/")) {
        this.iconType = IconType.PLUGIN;
      }  
  }
  
  public Icon(String classSpec, String url, String style, IconType iconType) { this(classSpec, url, style, iconType, IconFormat.IMG); }
  
  public Icon(String classSpec, String url, String style, IconFormat iconFormat) {
    this(classSpec, url, style, IconType.CORE, iconFormat);
    if (url != null)
      if (url.startsWith("images/")) {
        this.iconType = IconType.CORE;
      } else if (url.startsWith("plugin/")) {
        this.iconType = IconType.PLUGIN;
      }  
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Icon(String classSpec, String url, String style, IconType iconType, IconFormat iconFormat) {
    this.classSpec = classSpec;
    this.normalizedSelector = toNormalizedCSSSelector(classSpec);
    this.url = toNormalizedIconUrl(url);
    this.style = style;
    this.iconType = iconType;
    this.iconFormat = iconFormat;
  }
  
  public String getClassSpec() { return this.classSpec; }
  
  public boolean isSvgSprite() { return (this.iconFormat == IconFormat.EXTERNAL_SVG_SPRITE); }
  
  public String getNormalizedSelector() { return this.normalizedSelector; }
  
  public String getUrl() { return this.url; }
  
  public String getQualifiedUrl(JellyContext context) {
    if (this.url != null)
      return this.iconType.toQualifiedUrl(this.url, context.getVariable("resURL").toString()); 
    return "";
  }
  
  public String getQualifiedUrl(String resUrl) {
    if (this.url != null)
      return this.iconType.toQualifiedUrl(this.url, resUrl); 
    return "";
  }
  
  public String getStyle() { return this.style; }
  
  public static String toNormalizedIconNameClass(String string) {
    if (string == null)
      return null; 
    String iconName = toNormalizedIconName(string);
    if (iconName.startsWith("icon-"))
      return iconName; 
    return "icon-" + iconName;
  }
  
  public static String toNormalizedIconName(String string) {
    if (string == null)
      return null; 
    if (StringUtils.endsWithAny(string, SUPPORTED_FORMATS))
      string = string.substring(0, string.length() - 4); 
    return string.replace('_', '-');
  }
  
  public static String toNormalizedIconSizeClass(String string) {
    if (string == null)
      return null; 
    String normalizedSizeClass = (String)iconDims.get(string.trim());
    return (normalizedSizeClass != null) ? normalizedSizeClass : string;
  }
  
  public static String toNormalizedCSSSelector(String classNames) {
    if (classNames == null)
      return null; 
    String[] classNameTokA = classNames.split(" ");
    List<String> classNameTokL = new ArrayList<String>();
    for (String classNameTok : classNameTokA) {
      String trimmedToken = classNameTok.trim();
      if (trimmedToken.length() > 0)
        classNameTokL.add(trimmedToken); 
    } 
    classNameTokA = new String[classNameTokL.size()];
    classNameTokL.toArray(classNameTokA);
    Arrays.sort(classNameTokA, Comparator.comparing(String::toString));
    StringBuilder stringBuilder = new StringBuilder();
    for (String classNameTok : classNameTokA)
      stringBuilder.append(".").append(classNameTok); 
    return stringBuilder.toString();
  }
  
  public static String toNormalizedIconUrl(String url) {
    if (url == null)
      return null; 
    String originalUrl = url;
    if (url.startsWith("/"))
      url = url.substring(1); 
    if (url.startsWith("images/"))
      return url.substring("images/".length()); 
    if (url.startsWith("plugin/"))
      return url.substring("plugin/".length()); 
    return originalUrl;
  }
}
