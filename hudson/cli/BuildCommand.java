package hudson.cli;

import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.EditDistance;
import hudson.util.StreamTaskListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.scm.SCMDecisionHandler;
import jenkins.triggers.SCMTriggerItem;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

@Extension
public class BuildCommand extends CLICommand {
  @Argument(metaVar = "JOB", usage = "Name of the job to build", required = true)
  public Job<?, ?> job;
  
  public String getShortDescription() { return Messages.BuildCommand_ShortDescription(); }
  
  @Option(name = "-f", usage = "Follow the build progress. Like -s only interrupts are not passed through to the build.")
  public boolean follow = false;
  
  @Option(name = "-s", usage = "Wait until the completion/abortion of the command. Interrupts are passed through to the build.")
  public boolean sync = false;
  
  @Option(name = "-w", usage = "Wait until the start of the command")
  public boolean wait = false;
  
  @Option(name = "-c", usage = "Check for SCM changes before starting the build, and if there's no change, exit without doing a build")
  public boolean checkSCM = false;
  
  @Option(name = "-p", usage = "Specify the build parameters in the key=value format.")
  public Map<String, String> parameters = new HashMap();
  
  @Option(name = "-v", usage = "Prints out the console output of the build. Use with -s")
  public boolean consoleOutput = false;
  
  @Option(name = "-r")
  @Deprecated
  public int retryCnt = 10;
  
  protected static final String BUILD_SCHEDULING_REFUSED = "Build scheduling Refused by an extension, hence not in Queue.";
  
  protected int run() throws Exception {
    this.job.checkPermission(Item.BUILD);
    ParametersAction a = null;
    if (!this.parameters.isEmpty()) {
      ParametersDefinitionProperty pdp = (ParametersDefinitionProperty)this.job.getProperty(ParametersDefinitionProperty.class);
      if (pdp == null)
        throw new IllegalStateException(this.job.getFullDisplayName() + " is not parameterized but the -p option was specified."); 
      List<ParameterValue> values = new ArrayList<ParameterValue>();
      for (Map.Entry<String, String> e : this.parameters.entrySet()) {
        String name = (String)e.getKey();
        ParameterDefinition pd = pdp.getParameterDefinition(name);
        if (pd == null) {
          String nearest = EditDistance.findNearest(name, pdp.getParameterDefinitionNames());
          throw new CmdLineException(null, (nearest == null) ? 
              String.format("'%s' is not a valid parameter.", new Object[] { name }) : String.format("'%s' is not a valid parameter. Did you mean %s?", new Object[] { name, nearest }));
        } 
        ParameterValue val = pd.createValue(this, Util.fixNull((String)e.getValue()));
        if (val == null)
          throw new CmdLineException(null, String.format("Cannot resolve the value for the parameter '%s'.", new Object[] { name })); 
        values.add(val);
      } 
      for (ParameterDefinition pd : pdp.getParameterDefinitions()) {
        if (this.parameters.containsKey(pd.getName()))
          continue; 
        ParameterValue defaultValue = pd.getDefaultParameterValue();
        if (defaultValue == null)
          throw new CmdLineException(null, String.format("No default value for the parameter '%s'.", new Object[] { pd.getName() })); 
        values.add(defaultValue);
      } 
      a = new ParametersAction(values);
    } 
    if (this.checkSCM) {
      SCMTriggerItem item = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(this.job);
      if (item == null)
        throw new AbortException(this.job.getFullDisplayName() + " has no SCM trigger, but checkSCM was specified"); 
      if (SCMDecisionHandler.firstShouldPollVeto(this.job) != null)
        return 0; 
      if (!item.poll(new StreamTaskListener(this.stdout, getClientCharset())).hasChanges())
        return 0; 
    } 
    if (!this.job.isBuildable()) {
      String msg = Messages.BuildCommand_CLICause_CannotBuildUnknownReasons(this.job.getFullDisplayName());
      if (this.job instanceof ParameterizedJobMixIn.ParameterizedJob && ((ParameterizedJobMixIn.ParameterizedJob)this.job).isDisabled()) {
        msg = Messages.BuildCommand_CLICause_CannotBuildDisabled(this.job.getFullDisplayName());
      } else if (this.job.isHoldOffBuildUntilSave()) {
        msg = Messages.BuildCommand_CLICause_CannotBuildConfigNotSaved(this.job.getFullDisplayName());
      } 
      throw new IllegalStateException(msg);
    } 
    Queue.Item item = ParameterizedJobMixIn.scheduleBuild2(this.job, 0, new Action[] { new CauseAction(new CLICause(Jenkins.getAuthentication2().getName())), a });
    QueueTaskFuture<? extends Run<?, ?>> f = (item != null) ? item.getFuture() : null;
    if (this.wait || this.sync || this.follow) {
      if (f == null)
        throw new IllegalStateException("Build scheduling Refused by an extension, hence not in Queue."); 
      Run<?, ?> b = (Run)f.waitForStart();
      this.stdout.println("Started " + b.getFullDisplayName());
      this.stdout.flush();
      if (this.sync || this.follow)
        try {
          if (this.consoleOutput) {
            int retryInterval = 100;
            for (int i = 0; i <= this.retryCnt;) {
              try {
                b.writeWholeLogTo(this.stdout);
                break;
              } catch (FileNotFoundException|java.nio.file.NoSuchFileException e) {
                if (i == this.retryCnt) {
                  AbortException abortException = new AbortException();
                  abortException.initCause(e);
                  throw abortException;
                } 
                i++;
                Thread.sleep(retryInterval);
              } 
            } 
          } 
          f.get();
          this.stdout.println("Completed " + b.getFullDisplayName() + " : " + b.getResult());
          return (b.getResult()).ordinal;
        } catch (InterruptedException e) {
          if (this.follow)
            return 125; 
          f.cancel(true);
          AbortException abortException = new AbortException();
          abortException.initCause(e);
          throw abortException;
        }  
    } 
    return 0;
  }
  
  protected void printUsageSummary(PrintStream stderr) { stderr.println("Starts a build, and optionally waits for a completion.\nAside from general scripting use, this command can be\nused to invoke another job from within a build of one job.\nWith the -s option, this command changes the exit code based on\nthe outcome of the build (exit code 0 indicates a success)\nand interrupting the command will interrupt the job.\nWith the -f option, this command changes the exit code based on\nthe outcome of the build (exit code 0 indicates a success)\nhowever, unlike -s, interrupting the command will not interrupt\nthe job (exit code 125 indicates the command was interrupted).\nWith the -c option, a build will only run if there has been\nan SCM change."); }
}
