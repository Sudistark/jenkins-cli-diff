package hudson.cli;

import hudson.Extension;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.model.UpdateSite;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;

@Extension
public class ListPluginsCommand extends CLICommand {
  @Argument(metaVar = "NAME", usage = "Name of a specific plugin", required = false)
  public String name;
  
  public String getShortDescription() { return Messages.ListPluginsCommand_ShortDescription(); }
  
  protected int run() {
    Jenkins h = Jenkins.get();
    h.checkPermission(Jenkins.ADMINISTER);
    PluginManager pluginManager = h.getPluginManager();
    if (this.name != null) {
      PluginWrapper plugin = pluginManager.getPlugin(this.name);
      if (plugin != null) {
        printPlugin(plugin, plugin.getShortName().length(), plugin.getDisplayName().length());
      } else {
        throw new IllegalArgumentException("No plugin with the name '" + this.name + "' found");
      } 
    } else {
      int colWidthShortName = 1;
      int colWidthDisplayName = 1;
      List<PluginWrapper> plugins = pluginManager.getPlugins();
      if (plugins != null) {
        for (PluginWrapper plugin : plugins) {
          colWidthShortName = Math.max(colWidthShortName, plugin.getShortName().length());
          colWidthDisplayName = Math.max(colWidthDisplayName, plugin.getDisplayName().length());
        } 
        for (PluginWrapper plugin : plugins)
          printPlugin(plugin, colWidthShortName, colWidthDisplayName); 
      } 
    } 
    return 0;
  }
  
  private void printPlugin(PluginWrapper plugin, int colWidthShortName, int colWidthDisplayName) {
    String version;
    if (plugin.hasUpdate()) {
      UpdateSite.Plugin updateInfo = plugin.getUpdateInfo();
      version = String.format("%s (%s)", new Object[] { plugin.getVersion(), updateInfo.version });
    } else {
      version = plugin.getVersion();
    } 
    String formatString = String.format("%%-%ds %%-%ds %%s", new Object[] { Integer.valueOf(colWidthShortName), Integer.valueOf(colWidthDisplayName) });
    this.stdout.printf(formatString + "%n", new Object[] { plugin.getShortName(), plugin.getDisplayName(), version });
  }
}
