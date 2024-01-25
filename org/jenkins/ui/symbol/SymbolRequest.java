package org.jenkins.ui.symbol;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.logging.Logger;

public final class SymbolRequest {
  private static final Logger LOGGER = Logger.getLogger(SymbolRequest.class.getName());
  
  @NonNull
  private final String name;
  
  @CheckForNull
  private final String title;
  
  @CheckForNull
  private final String tooltip;
  
  @CheckForNull
  private final String htmlTooltip;
  
  @CheckForNull
  private final String classes;
  
  @CheckForNull
  private final String pluginName;
  
  @CheckForNull
  private final String id;
  
  @NonNull
  public String getName() { return this.name; }
  
  @CheckForNull
  public String getTitle() { return this.title; }
  
  @CheckForNull
  public String getTooltip() { return this.tooltip; }
  
  @CheckForNull
  public String getHtmlTooltip() { return this.htmlTooltip; }
  
  @CheckForNull
  public String getClasses() { return this.classes; }
  
  @CheckForNull
  public String getPluginName() { return this.pluginName; }
  
  @CheckForNull
  public String getId() { return this.id; }
  
  private SymbolRequest(@NonNull String name, @CheckForNull String title, @CheckForNull String tooltip, @CheckForNull String htmlTooltip, @CheckForNull String classes, @CheckForNull String pluginName, @CheckForNull String id) {
    this.name = name;
    this.title = title;
    this.tooltip = tooltip;
    this.htmlTooltip = htmlTooltip;
    this.classes = classes;
    this.pluginName = pluginName;
    this.id = id;
  }
}
