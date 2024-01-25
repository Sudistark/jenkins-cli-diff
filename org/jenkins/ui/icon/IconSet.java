package org.jenkins.ui.icon;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.apache.commons.jelly.JellyContext;
import org.jenkins.ui.symbol.Symbol;
import org.jenkins.ui.symbol.SymbolRequest;
import org.kohsuke.accmod.Restricted;

public class IconSet {
  public static final IconSet icons = new IconSet();
  
  private Map<String, Icon> iconsByCSSSelector = new ConcurrentHashMap();
  
  private Map<String, Icon> iconsByUrl = new ConcurrentHashMap();
  
  private Map<String, Icon> iconsByClassSpec = new ConcurrentHashMap();
  
  private Map<String, Icon> coreIcons = new ConcurrentHashMap();
  
  private static final Icon NO_ICON = new Icon("_", "_", "_");
  
  private static final Map<String, String> ICON_TO_SYMBOL_TRANSLATIONS;
  
  public Map<String, Icon> getCoreIcons() { return this.coreIcons; }
  
  public static void initPageVariables(JellyContext context) { context.setVariable("icons", icons); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String getSymbol(String name, String title, String tooltip, String htmlTooltip, String classes, String pluginName, String id) {
    return Symbol.get((new SymbolRequest.Builder())
        .withName(cleanName(name))
        .withTitle(title)
        .withTooltip(tooltip)
        .withHtmlTooltip(htmlTooltip)
        .withClasses(classes)
        .withPluginName(pluginName)
        .withId(id)
        .build());
  }
  
  public IconSet addIcon(Icon icon) {
    this.iconsByCSSSelector.put(icon.getNormalizedSelector(), icon);
    if (icon.getUrl() != null)
      this.iconsByUrl.put(icon.getUrl(), icon); 
    this.iconsByClassSpec.clear();
    return this;
  }
  
  public Icon getIconByNormalizedCSSSelector(Object cssSelector) {
    if (cssSelector == null)
      return null; 
    return getIconByNormalizedCSSSelector(cssSelector.toString());
  }
  
  private Icon getIconByNormalizedCSSSelector(String cssSelector) {
    if (cssSelector == null)
      return null; 
    return (Icon)this.iconsByCSSSelector.get(cssSelector);
  }
  
  public Icon getIconByClassSpec(Object iconClassSpec) {
    if (iconClassSpec == null)
      return null; 
    return getIconByClassSpec(iconClassSpec.toString());
  }
  
  private Icon getIconByClassSpec(String iconClassSpec) {
    if (iconClassSpec == null)
      return null; 
    Icon icon = (Icon)this.iconsByClassSpec.get(iconClassSpec);
    if (icon == NO_ICON)
      return null; 
    if (icon != null)
      return icon; 
    String normalizedCSSSelector = Icon.toNormalizedCSSSelector(iconClassSpec);
    icon = getIconByNormalizedCSSSelector(normalizedCSSSelector);
    if (icon != null) {
      this.iconsByClassSpec.put(iconClassSpec, icon);
      return icon;
    } 
    this.iconsByClassSpec.put(iconClassSpec, NO_ICON);
    return null;
  }
  
  public Icon getIconByUrl(Object url) {
    if (url == null)
      return null; 
    return getIconByUrl(url.toString());
  }
  
  private Icon getIconByUrl(String url) {
    if (url == null)
      return null; 
    url = Icon.toNormalizedIconUrl(url);
    return (Icon)this.iconsByUrl.get(url);
  }
  
  public static String toNormalizedIconNameClass(Object string) {
    if (string == null)
      return null; 
    return toNormalizedIconNameClass(string.toString());
  }
  
  private static String toNormalizedIconNameClass(String string) { return Icon.toNormalizedIconNameClass(string); }
  
  public static String toNormalizedIconSizeClass(Object string) {
    if (string == null)
      return null; 
    return toNormalizedIconSizeClass(string.toString());
  }
  
  private static String toNormalizedIconSizeClass(String string) { return Icon.toNormalizedIconSizeClass(string); }
  
  public static String toNormalizedIconUrl(Object url) {
    if (url == null)
      return null; 
    return toNormalizedIconUrl(url.toString());
  }
  
  private static String toNormalizedIconUrl(String url) { return Icon.toNormalizedIconUrl(url); }
  
  static  {
    icons.addIcon(new BuildStatusIcon("icon-aborted icon-sm", "build-status/build-status-sprite.svg#last-aborted", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-aborted-anime icon-sm", "build-status/build-status-sprite.svg#last-aborted", "width: 16px; height: 16px;", true));
    icons.addIcon(new BuildStatusIcon("icon-blue icon-sm", "build-status/build-status-sprite.svg#last-successful", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-blue-anime icon-sm", "build-status/build-status-sprite.svg#last-successful", "width: 16px; height: 16px;", true));
    icons.addIcon(new Icon("icon-clock-anime icon-sm", "16x16/clock_anime.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-disabled icon-sm", "build-status/build-status-sprite.svg#last-disabled", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-disabled-anime icon-sm", "build-status/build-status-sprite.svg#last-disabled", "width: 16px; height: 16px;", true));
    icons.addIcon(new Icon("icon-document-add icon-sm", "16x16/document_add.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-document-delete icon-sm", "16x16/document_delete.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-document-edit icon-sm", "16x16/document_edit.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-edit-delete icon-sm", "16x16/edit-delete.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-edit-select-all icon-sm", "16x16/edit-select-all.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-empty icon-sm", "16x16/empty.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-folder-open icon-sm", "16x16/folder-open.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-green icon-sm", "16x16/green.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-green-anime icon-sm", "16x16/green_anime.gif", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey icon-sm", "build-status/build-status-sprite.svg#never-built", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey-anime icon-sm", "build-status/build-status-sprite.svg#never-built", "width: 16px; height: 16px;", true));
    icons.addIcon(new WeatherIcon("icon-health-00to19 icon-sm", "width: 16px; height: 16px;", WeatherIcon.Status.POURING));
    icons.addIcon(new WeatherIcon("icon-health-20to39 icon-sm", "width: 16px; height: 16px;", WeatherIcon.Status.RAINY));
    icons.addIcon(new WeatherIcon("icon-health-40to59 icon-sm", "width: 16px; height: 16px;", WeatherIcon.Status.CLOUDY));
    icons.addIcon(new WeatherIcon("icon-health-60to79 icon-sm", "width: 16px; height: 16px;", WeatherIcon.Status.PARTLY_CLOUDY));
    icons.addIcon(new WeatherIcon("icon-health-80plus icon-sm", "width: 16px; height: 16px;", WeatherIcon.Status.SUNNY));
    icons.addIcon(new BuildStatusIcon("icon-nobuilt icon-sm", "build-status/build-status-sprite.svg#never-built", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-nobuilt-anime icon-sm", "build-status/build-status-sprite.svg#never-built", "width: 16px; height: 16px;", true));
    icons.addIcon(new BuildStatusIcon("icon-red icon-sm", "build-status/build-status-sprite.svg#last-failed", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-red-anime icon-sm", "build-status/build-status-sprite.svg#last-failed", "width: 16px; height: 16px;", true));
    icons.addIcon(new BuildStatusIcon("icon-yellow icon-sm", "build-status/build-status-sprite.svg#last-unstable", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-yellow-anime icon-sm", "build-status/build-status-sprite.svg#last-unstable", "width: 16px; height: 16px;", true));
    icons.addIcon(new Icon("icon-collapse icon-sm", "16x16/collapse.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-document-add icon-sm", "16x16/document_add.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-document-delete icon-sm", "16x16/document_delete.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-document-edit icon-sm", "16x16/document_edit.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-edit-select-all icon-sm", "16x16/edit-select-all.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-empty icon-sm", "16x16/empty.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-expand icon-sm", "16x16/expand.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-folder-open icon-sm", "16x16/folder-open.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-go-next icon-sm", "16x16/go-next.png", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey icon-sm", "build-status/build-status-sprite.svg#never-built", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-text-error icon-sm", "16x16/text-error.png", "width: 16px; height: 16px;"));
    icons.addIcon(new Icon("icon-text icon-sm", "16x16/text.png", "width: 16px; height: 16px;"));
    icons.addIcon(new BuildStatusIcon("icon-aborted icon-md", "build-status/build-status-sprite.svg#last-aborted", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-aborted-anime icon-md", "build-status/build-status-sprite.svg#last-aborted", "width: 24px; height: 24px;", true));
    icons.addIcon(new BuildStatusIcon("icon-blue icon-md", "build-status/build-status-sprite.svg#last-successful", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-blue-anime icon-md", "build-status/build-status-sprite.svg#last-successful", "width: 24px; height: 24px;", true));
    icons.addIcon(new Icon("icon-clock-anime icon-md", "24x24/clock_anime.gif", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-disabled icon-md", "build-status/build-status-sprite.svg#last-disabled", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-disabled-anime icon-md", "build-status/build-status-sprite.svg#last-disabled", "width: 24px; height: 24px;", true));
    icons.addIcon(new Icon("icon-empty icon-md", "24x24/empty.gif", "width: 24px; height: 24px;"));
    icons.addIcon(new Icon("icon-green icon-md", "24x24/green.gif", "width: 24px; height: 24px;"));
    icons.addIcon(new Icon("icon-green-anime icon-md", "24x24/green_anime.gif", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey icon-md", "build-status/build-status-sprite.svg#never-built", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey-anime icon-md", "build-status/build-status-sprite.svg#never-built", "width: 24px; height: 24px;", true));
    icons.addIcon(new WeatherIcon("icon-health-00to19 icon-md", "width: 24px; height: 24px;", WeatherIcon.Status.POURING));
    icons.addIcon(new WeatherIcon("icon-health-20to39 icon-md", "width: 24px; height: 24px;", WeatherIcon.Status.RAINY));
    icons.addIcon(new WeatherIcon("icon-health-40to59 icon-md", "width: 24px; height: 24px;", WeatherIcon.Status.CLOUDY));
    icons.addIcon(new WeatherIcon("icon-health-60to79 icon-md", "width: 24px; height: 24px;", WeatherIcon.Status.PARTLY_CLOUDY));
    icons.addIcon(new WeatherIcon("icon-health-80plus icon-md", "width: 24px; height: 24px;", WeatherIcon.Status.SUNNY));
    icons.addIcon(new BuildStatusIcon("icon-nobuilt icon-md", "build-status/build-status-sprite.svg#never-built", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-nobuilt-anime icon-md", "build-status/build-status-sprite.svg#never-built", "width: 24px; height: 24px;", true));
    icons.addIcon(new BuildStatusIcon("icon-red icon-md", "build-status/build-status-sprite.svg#last-failed", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-red-anime icon-md", "build-status/build-status-sprite.svg#last-failed", "width: 24px; height: 24px;", true));
    icons.addIcon(new BuildStatusIcon("icon-yellow icon-md", "build-status/build-status-sprite.svg#last-unstable", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-yellow-anime icon-md", "build-status/build-status-sprite.svg#last-unstable", "width: 24px; height: 24px;", true));
    icons.addIcon(new Icon("icon-empty icon-md", "24x24/empty.png", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey icon-md", "build-status/build-status-sprite.svg#never-built", "width: 24px; height: 24px;"));
    icons.addIcon(new BuildStatusIcon("icon-aborted icon-lg", "build-status/build-status-sprite.svg#last-aborted", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-aborted-anime icon-lg", "build-status/build-status-sprite.svg#last-aborted", "width: 32px; height: 32px;", true));
    icons.addIcon(new BuildStatusIcon("icon-blue icon-lg", "build-status/build-status-sprite.svg#last-successful", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-blue-anime icon-lg", "build-status/build-status-sprite.svg#last-successful", "width: 32px; height: 32px;", true));
    icons.addIcon(new Icon("icon-clock-anime icon-lg", "32x32/clock_anime.gif", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-disabled icon-lg", "build-status/build-status-sprite.svg#last-disabled", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-disabled-anime icon-lg", "build-status/build-status-sprite.svg#last-disabled", "width: 32px; height: 32px;", true));
    icons.addIcon(new Icon("icon-empty icon-lg", "32x32/empty.gif", "width: 32px; height: 32px;"));
    icons.addIcon(new Icon("icon-green icon-lg", "32x32/green.gif", "width: 32px; height: 32px;"));
    icons.addIcon(new Icon("icon-green-anime icon-lg", "32x32/green_anime.gif", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey icon-lg", "build-status/build-status-sprite.svg#never-built", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey-anime icon-lg", "build-status/build-status-sprite.svg#never-built", "width: 32px; height: 32px;", true));
    icons.addIcon(new Icon("icon-empty icon-lg", "32x32/empty.png", "width: 32px; height: 32px;"));
    icons.addIcon(new WeatherIcon("icon-health-00to19 icon-lg", "width: 32px; height: 32px;", WeatherIcon.Status.POURING));
    icons.addIcon(new WeatherIcon("icon-health-20to39 icon-lg", "width: 32px; height: 32px;", WeatherIcon.Status.RAINY));
    icons.addIcon(new WeatherIcon("icon-health-40to59 icon-lg", "width: 32px; height: 32px;", WeatherIcon.Status.CLOUDY));
    icons.addIcon(new WeatherIcon("icon-health-60to79 icon-lg", "width: 32px; height: 32px;", WeatherIcon.Status.PARTLY_CLOUDY));
    icons.addIcon(new WeatherIcon("icon-health-80plus icon-lg", "width: 32px; height: 32px;", WeatherIcon.Status.SUNNY));
    icons.addIcon(new BuildStatusIcon("icon-nobuilt icon-lg", "build-status/build-status-sprite.svg#never-built", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-nobuilt-anime icon-lg", "build-status/build-status-sprite.svg#never-built", "width: 32px; height: 32px;", true));
    icons.addIcon(new BuildStatusIcon("icon-red icon-lg", "build-status/build-status-sprite.svg#last-failed", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-red-anime icon-lg", "build-status/build-status-sprite.svg#last-failed", "width: 32px; height: 32px;", true));
    icons.addIcon(new BuildStatusIcon("icon-yellow icon-lg", "build-status/build-status-sprite.svg#last-unstable", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-yellow-anime icon-lg", "build-status/build-status-sprite.svg#last-unstable", "width: 32px; height: 32px;", true));
    icons.addIcon(new BuildStatusIcon("icon-grey icon-lg", "build-status/build-status-sprite.svg#never-built", "width: 32px; height: 32px;"));
    icons.addIcon(new BuildStatusIcon("icon-aborted icon-xlg", "build-status/build-status-sprite.svg#last-aborted", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-aborted-anime icon-xlg", "build-status/build-status-sprite.svg#last-aborted", "width: 48px; height: 48px;", true));
    icons.addIcon(new BuildStatusIcon("icon-blue icon-xlg", "build-status/build-status-sprite.svg#last-successful", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-blue-anime icon-xlg", "build-status/build-status-sprite.svg#last-successful", "width: 48px; height: 48px;", true));
    icons.addIcon(new BuildStatusIcon("icon-disabled icon-xlg", "build-status/build-status-sprite.svg#last-disabled", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-disabled-anime icon-xlg", "build-status/build-status-sprite.svg#last-disabled", "width: 48px; height: 48px;", true));
    icons.addIcon(new Icon("icon-green icon-xlg", "48x48/green.gif", "width: 48px; height: 48px;"));
    icons.addIcon(new Icon("icon-green-anime icon-xlg", "48x48/green_anime.gif", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey icon-xlg", "build-status/build-status-sprite.svg#never-built", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey-anime icon-xlg", "build-status/build-status-sprite.svg#never-built", "width: 48px; height: 48px;", true));
    icons.addIcon(new WeatherIcon("icon-health-00to19 icon-xlg", "width: 48px; height: 48px;", WeatherIcon.Status.POURING));
    icons.addIcon(new WeatherIcon("icon-health-20to39 icon-xlg", "width: 48px; height: 48px;", WeatherIcon.Status.RAINY));
    icons.addIcon(new WeatherIcon("icon-health-40to59 icon-xlg", "width: 48px; height: 48px;", WeatherIcon.Status.CLOUDY));
    icons.addIcon(new WeatherIcon("icon-health-60to79 icon-xlg", "width: 48px; height: 48px;", WeatherIcon.Status.PARTLY_CLOUDY));
    icons.addIcon(new WeatherIcon("icon-health-80plus icon-xlg", "width: 48px; height: 48px;", WeatherIcon.Status.SUNNY));
    icons.addIcon(new BuildStatusIcon("icon-nobuilt icon-xlg", "build-status/build-status-sprite.svg#never-built", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-nobuilt-anime icon-xlg", "build-status/build-status-sprite.svg#never-built", "width: 48px; height: 48px;", true));
    icons.addIcon(new BuildStatusIcon("icon-red icon-xlg", "build-status/build-status-sprite.svg#last-failed", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-red-anime icon-xlg", "build-status/build-status-sprite.svg#last-failed", "width: 48px; height: 48px;", true));
    icons.addIcon(new BuildStatusIcon("icon-yellow icon-xlg", "build-status/build-status-sprite.svg#last-unstable", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-yellow-anime icon-xlg", "build-status/build-status-sprite.svg#last-unstable", "width: 48px; height: 48px;", true));
    icons.addIcon(new Icon("icon-empty icon-xlg", "48x48/empty.png", "width: 48px; height: 48px;"));
    icons.addIcon(new BuildStatusIcon("icon-grey icon-xlg", "build-status/build-status-sprite.svg#never-built", "width: 48px; height: 48px;"));
    initializeSVGs();
    icons.coreIcons.putAll(icons.iconsByCSSSelector);
    translations = new HashMap();
    translations.put("icon-application-certificate", "symbol-ribbon");
    translations.put("icon-document", "symbol-document-text");
    translations.put("icon-clipboard", "symbol-logs");
    translations.put("icon-clock", "symbol-play");
    translations.put("icon-edit-delete", "symbol-trash");
    translations.put("icon-fingerprint", "symbol-fingerprint");
    translations.put("icon-folder", "symbol-folder");
    translations.put("icon-gear", "symbol-settings");
    translations.put("icon-gear2", "symbol-settings");
    translations.put("icon-health-00to19", "symbol-weather-icon-health-00to19");
    translations.put("icon-health-20to39", "symbol-weather-icon-health-20to39");
    translations.put("icon-health-40to59", "symbol-weather-icon-health-40to59");
    translations.put("icon-health-60to79", "symbol-weather-icon-health-60to79");
    translations.put("icon-health-80plus", "symbol-weather-icon-health-80plus");
    translations.put("icon-help", "symbol-help-circle");
    translations.put("icon-keys", "symbol-key");
    translations.put("icon-monitor", "symbol-terminal");
    translations.put("icon-new-package", "symbol-add");
    translations.put("icon-next", "symbol-arrow-right");
    translations.put("icon-plugin", "symbol-plugins");
    translations.put("icon-previous", "symbol-arrow-left");
    translations.put("icon-search", "symbol-search");
    translations.put("icon-setting", "symbol-build");
    translations.put("icon-terminal", "symbol-terminal");
    translations.put("icon-text", "symbol-details");
    translations.put("icon-up", "symbol-arrow-up");
    translations.put("icon-user", "symbol-people");
    translations.put("icon-undo", "symbol-undo");
    translations.put("icon-redo", "symbol-redo");
    translations.put("icon-hourglass", "symbol-hourglass");
    ICON_TO_SYMBOL_TRANSLATIONS = translations;
  }
  
  private static void initializeSVGs() {
    sizes = new HashMap();
    sizes.put("icon-sm", "width: 16px; height: 16px;");
    sizes.put("icon-md", "width: 24px; height: 24px;");
    sizes.put("icon-lg", "width: 32px; height: 32px;");
    sizes.put("icon-xlg", "width: 48px; height: 48px;");
    List<String> images = new ArrayList<String>();
    images.add("computer");
    images.add("delete-document");
    images.add("accept");
    images.add("application-certificate");
    images.add("attribute");
    images.add("bookmark-new");
    images.add("certificate");
    images.add("clipboard-list-solid");
    images.add("clipboard");
    images.add("clock");
    images.add("computer-user-offline");
    images.add("computer-x");
    images.add("document");
    images.add("edit-delete");
    images.add("emblem-urgent");
    images.add("error");
    images.add("fingerprint");
    images.add("folder-delete");
    images.add("folder");
    images.add("gear");
    images.add("gear2");
    images.add("go-down");
    images.add("go-up");
    images.add("graph");
    images.add("headless");
    images.add("headshot");
    images.add("hourglass");
    images.add("installer");
    images.add("keys");
    images.add("lock");
    images.add("logo");
    images.add("monitor");
    images.add("network");
    images.add("new-computer");
    images.add("new-document");
    images.add("new-package");
    images.add("new-user");
    images.add("next");
    images.add("notepad");
    images.add("orange-square");
    images.add("package");
    images.add("person");
    images.add("plugin");
    images.add("previous");
    images.add("redo");
    images.add("refresh");
    images.add("save-new");
    images.add("save");
    images.add("search");
    images.add("secure");
    images.add("setting");
    images.add("shield");
    images.add("star-gold");
    images.add("star-large-gold");
    images.add("star-large");
    images.add("star");
    images.add("stop");
    images.add("system-log-out");
    images.add("terminal");
    images.add("undo");
    images.add("up");
    images.add("user");
    images.add("video");
    images.add("warning");
    images.add("document-properties");
    images.add("help");
    for (Map.Entry<String, String> size : sizes.entrySet()) {
      for (String image : images)
        icons.addIcon(new Icon("icon-" + image + " " + (String)size.getKey(), "svgs/" + image + ".svg", (String)size.getValue())); 
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String tryTranslateTangoIconToSymbol(@CheckForNull String tangoIcon) { return tryTranslateTangoIconToSymbol(tangoIcon, () -> null); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String tryTranslateTangoIconToSymbol(@CheckForNull String tangoIcon, @NonNull Supplier<String> defaultValueSupplier) { return (tangoIcon == null) ? null : (String)ICON_TO_SYMBOL_TRANSLATIONS.getOrDefault(cleanName(tangoIcon), (String)defaultValueSupplier.get()); }
  
  private static String cleanName(String tangoIcon) {
    if (tangoIcon != null)
      tangoIcon = tangoIcon.split(" ")[0]; 
    return tangoIcon;
  }
}
