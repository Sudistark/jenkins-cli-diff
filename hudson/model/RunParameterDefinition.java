package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.RunList;
import java.util.Objects;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

public class RunParameterDefinition extends SimpleParameterDefinition {
  private final String projectName;
  
  private final String runId;
  
  private final RunParameterFilter filter;
  
  @DataBoundConstructor
  public RunParameterDefinition(@NonNull String name, String projectName, @CheckForNull String description, @CheckForNull RunParameterFilter filter) {
    super(name, description);
    this.projectName = projectName;
    this.runId = null;
    this.filter = filter;
  }
  
  @Deprecated
  public RunParameterDefinition(@NonNull String name, String projectName, @CheckForNull String description) { this(name, projectName, description, RunParameterFilter.ALL); }
  
  private RunParameterDefinition(@NonNull String name, String projectName, String runId, @CheckForNull String description, @CheckForNull RunParameterFilter filter) {
    super(name, description);
    this.projectName = projectName;
    this.runId = runId;
    this.filter = filter;
  }
  
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
    if (defaultValue instanceof RunParameterValue) {
      RunParameterValue value = (RunParameterValue)defaultValue;
      return new RunParameterDefinition(getName(), getProjectName(), value.getRunId(), getDescription(), getFilter());
    } 
    return this;
  }
  
  @Exported
  public String getProjectName() { return this.projectName; }
  
  public Job getProject() { return (Job)Jenkins.get().getItemByFullName(this.projectName, Job.class); }
  
  @Exported
  public RunParameterFilter getFilter() { return (null == this.filter) ? RunParameterFilter.ALL : this.filter; }
  
  public RunList getBuilds() {
    switch (null.$SwitchMap$hudson$model$RunParameterDefinition$RunParameterFilter[getFilter().ordinal()]) {
      case 1:
        return getProject().getBuilds().overThresholdOnly(Result.ABORTED).completedOnly();
      case 2:
        return getProject().getBuilds().overThresholdOnly(Result.UNSTABLE).completedOnly();
      case 3:
        return getProject().getBuilds().overThresholdOnly(Result.SUCCESS).completedOnly();
    } 
    return getProject().getBuilds();
  }
  
  public ParameterValue getDefaultParameterValue() {
    Run<?, ?> lastBuild, lastBuild, lastBuild, lastBuild;
    if (this.runId != null)
      return createValue(this.runId); 
    Job project = getProject();
    if (project == null)
      return null; 
    switch (null.$SwitchMap$hudson$model$RunParameterDefinition$RunParameterFilter[getFilter().ordinal()]) {
      case 1:
        lastBuild = project.getLastCompletedBuild();
        break;
      case 2:
        lastBuild = project.getLastSuccessfulBuild();
        break;
      case 3:
        lastBuild = project.getLastStableBuild();
        break;
      default:
        lastBuild = project.getLastBuild();
        break;
    } 
    if (lastBuild != null)
      return createValue(lastBuild.getExternalizableId()); 
    return null;
  }
  
  public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
    RunParameterValue value = (RunParameterValue)req.bindJSON(RunParameterValue.class, jo);
    value.setDescription(getDescription());
    return value;
  }
  
  public RunParameterValue createValue(String value) { return new RunParameterValue(getName(), value, getDescription()); }
  
  public int hashCode() {
    if (RunParameterDefinition.class != getClass())
      return super.hashCode(); 
    return Objects.hash(new Object[] { getName(), getDescription(), this.projectName, this.runId, this.filter });
  }
  
  @SuppressFBWarnings(value = {"EQ_GETCLASS_AND_CLASS_CONSTANT"}, justification = "ParameterDefinitionTest tests that subclasses are not equal to their parent classes, so the behavior appears to be intentional")
  public boolean equals(Object obj) {
    if (RunParameterDefinition.class != getClass())
      return super.equals(obj); 
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    RunParameterDefinition other = (RunParameterDefinition)obj;
    if (!Objects.equals(getName(), other.getName()))
      return false; 
    if (!Objects.equals(getDescription(), other.getDescription()))
      return false; 
    if (!Objects.equals(this.projectName, other.projectName))
      return false; 
    if (!Objects.equals(this.runId, other.runId))
      return false; 
    return Objects.equals(this.filter, other.filter);
  }
  
  private static final Logger LOGGER = Logger.getLogger(RunParameterDefinition.class.getName());
}
