package hudson.cli;

import hudson.Extension;
import hudson.PluginWrapper;
import hudson.lifecycle.RestartNotSupportedException;
import java.io.PrintStream;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

@Extension
public class DisablePluginCommand extends CLICommand {
  @Argument(metaVar = "plugin1 plugin2 plugin3", required = true, usage = "Plugins to be disabled.")
  private List<String> pluginNames;
  
  @Option(name = "-restart", aliases = {"-r"}, usage = "Restart Jenkins after disabling plugins.")
  private boolean restart;
  
  @Option(name = "-strategy", aliases = {"-s"}, metaVar = "strategy", usage = "How to process the dependent plugins. \n- none: if a mandatory dependent plugin exists and it is enabled, the plugin cannot be disabled (default value).\n- mandatory: all mandatory dependent plugins are also disabled, optional dependent plugins remain enabled.\n- all: all dependent plugins are also disabled, no matter if its dependency is optional or mandatory.")
  private String strategy = PluginWrapper.PluginDisableStrategy.NONE


    
    .toString();
  
  @Option(name = "-quiet", aliases = {"-q"}, usage = "Be quiet, print only the error messages")
  private boolean quiet;
  
  private static final int INDENT_SPACE = 3;
  
  static final int RETURN_CODE_NOT_DISABLED_DEPENDANTS = 16;
  
  static final int RETURN_CODE_NO_SUCH_PLUGIN = 17;
  
  public String getShortDescription() { return Messages.DisablePluginCommand_ShortDescription(); }
  
  protected void printUsageSummary(PrintStream stderr) {
    super.printUsageSummary(stderr);
    stderr.println(Messages.DisablePluginCommand_PrintUsageSummary());
  }
  
  protected int run() throws Exception {
    PluginWrapper.PluginDisableStrategy strategyToUse;
    Jenkins jenkins = Jenkins.get();
    jenkins.checkPermission(Jenkins.ADMINISTER);
    try {
      strategyToUse = PluginWrapper.PluginDisableStrategy.valueOf(this.strategy.toUpperCase());
    } catch (IllegalArgumentException iae) {
      throw new IllegalArgumentException(
          Messages.DisablePluginCommand_NoSuchStrategy(this.strategy, 
            
            String.format("%s, %s, %s", new Object[] { PluginWrapper.PluginDisableStrategy.NONE, PluginWrapper.PluginDisableStrategy.MANDATORY, PluginWrapper.PluginDisableStrategy.ALL })), iae);
    } 
    List<PluginWrapper.PluginDisableResult> results = jenkins.pluginManager.disablePlugins(strategyToUse, this.pluginNames);
    printResults(results);
    restartIfNecessary(results);
    return getResultCode(results);
  }
  
  private void printResults(List<PluginWrapper.PluginDisableResult> results) {
    for (PluginWrapper.PluginDisableResult oneResult : results)
      printResult(oneResult, 0); 
  }
  
  private void printIndented(int indent, String format, String... arguments) {
    if (indent == 0) {
      this.stdout.format(format + "%n", (Object[])arguments);
    } else {
      String[] newArgs = new String[arguments.length + 1];
      newArgs[0] = " ";
      System.arraycopy(arguments, 0, newArgs, 1, arguments.length);
      String f = "%" + indent + "s" + format + "%n";
      this.stdout.format(f, (Object[])newArgs);
    } 
  }
  
  private void printResult(PluginWrapper.PluginDisableResult oneResult, int indent) {
    PluginWrapper.PluginDisableStatus status = oneResult.getStatus();
    if (this.quiet && (PluginWrapper.PluginDisableStatus.DISABLED.equals(status) || PluginWrapper.PluginDisableStatus.ALREADY_DISABLED.equals(status)))
      return; 
    printIndented(indent, Messages.DisablePluginCommand_StatusMessage(oneResult.getPlugin(), oneResult.getStatus(), oneResult.getMessage()), new String[0]);
    if (oneResult.getDependentsDisableStatus().size() > 0) {
      indent += 3;
      for (PluginWrapper.PluginDisableResult oneDependentResult : oneResult.getDependentsDisableStatus())
        printResult(oneDependentResult, indent); 
    } 
  }
  
  private void restartIfNecessary(List<PluginWrapper.PluginDisableResult> results) {
    if (this.restart)
      for (PluginWrapper.PluginDisableResult oneResult : results) {
        if (restartIfNecessary(oneResult))
          break; 
      }  
  }
  
  private boolean restartIfNecessary(PluginWrapper.PluginDisableResult oneResult) throws RestartNotSupportedException {
    PluginWrapper.PluginDisableStatus status = oneResult.getStatus();
    if (PluginWrapper.PluginDisableStatus.DISABLED.equals(status)) {
      Jenkins.get().safeRestart();
      return true;
    } 
    if (oneResult.getDependentsDisableStatus().size() > 0)
      for (PluginWrapper.PluginDisableResult oneDependentResult : oneResult.getDependentsDisableStatus()) {
        if (restartIfNecessary(oneDependentResult))
          return true; 
      }  
    return false;
  }
  
  private int getResultCode(List<PluginWrapper.PluginDisableResult> results) {
    int result = 0;
    for (PluginWrapper.PluginDisableResult oneResult : results) {
      result = getResultCode(oneResult);
      if (result != 0)
        break; 
    } 
    return result;
  }
  
  private int getResultCode(PluginWrapper.PluginDisableResult result) {
    returnCode = 0;
    switch (null.$SwitchMap$hudson$PluginWrapper$PluginDisableStatus[result.getStatus().ordinal()]) {
      case 1:
        return 16;
      case 2:
        return 17;
    } 
    for (PluginWrapper.PluginDisableResult oneDependentResult : result.getDependentsDisableStatus()) {
      returnCode = getResultCode(oneDependentResult);
      if (returnCode != 0)
        break; 
    } 
    return returnCode;
  }
}
