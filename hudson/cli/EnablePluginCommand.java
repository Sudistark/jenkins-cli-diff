package hudson.cli;

import hudson.Extension;
import hudson.PluginManager;
import hudson.PluginWrapper;
import java.io.IOException;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Extension
public class EnablePluginCommand extends CLICommand {
  @Argument(required = true, usage = "Enables the plugins with the given short names and their dependencies.")
  private List<String> pluginNames;
  
  @Option(name = "-restart", usage = "Restart Jenkins after enabling plugins.")
  private boolean restart;
  
  public String getShortDescription() { return Messages.EnablePluginCommand_ShortDescription(); }
  
  protected int run() throws Exception {
    Jenkins jenkins = Jenkins.get();
    jenkins.checkPermission(Jenkins.ADMINISTER);
    PluginManager manager = jenkins.getPluginManager();
    boolean enabledAnyPlugins = false;
    for (String pluginName : this.pluginNames)
      enabledAnyPlugins |= enablePlugin(manager, pluginName); 
    if (this.restart && enabledAnyPlugins)
      jenkins.safeRestart(); 
    return 0;
  }
  
  private boolean enablePlugin(PluginManager manager, String shortName) throws IOException {
    PluginWrapper plugin = manager.getPlugin(shortName);
    if (plugin == null)
      throw new IllegalArgumentException(Messages.EnablePluginCommand_NoSuchPlugin(shortName)); 
    if (plugin.isEnabled())
      return false; 
    this.stdout.printf("Enabling plugin `%s' (%s)%n", new Object[] { plugin.getShortName(), plugin.getVersion() });
    enableDependencies(manager, plugin);
    plugin.enable();
    this.stdout.printf("Plugin `%s' was enabled.%n", new Object[] { plugin.getShortName() });
    return true;
  }
  
  private void enableDependencies(PluginManager manager, PluginWrapper plugin) throws IOException {
    for (PluginWrapper.Dependency dep : plugin.getDependencies()) {
      PluginWrapper dependency = manager.getPlugin(dep.shortName);
      if (dependency == null)
        throw new IllegalArgumentException(Messages.EnablePluginCommand_MissingDependencies(plugin.getShortName(), dep)); 
      if (!dependency.isEnabled()) {
        enableDependencies(manager, dependency);
        this.stdout.printf("Enabling plugin dependency `%s' (%s) for `%s'%n", new Object[] { dependency.getShortName(), dependency.getVersion(), plugin.getShortName() });
        dependency.enable();
      } 
    } 
  }
}
