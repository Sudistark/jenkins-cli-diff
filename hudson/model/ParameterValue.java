package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.EnvVars;
import hudson.Util;
import hudson.model.queue.SubTask;
import hudson.tasks.BuildWrapper;
import hudson.util.VariableResolver;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.security.stapler.StaplerAccessibleType;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 3)
@StaplerAccessibleType
public abstract class ParameterValue implements Serializable {
  private static final Logger LOGGER = Logger.getLogger(ParameterValue.class.getName());
  
  protected final String name;
  
  private String description;
  
  protected ParameterValue(String name, String description) {
    this.name = name;
    this.description = description;
  }
  
  protected ParameterValue(String name) { this(name, null); }
  
  public String getDescription() { return this.description; }
  
  public void setDescription(String description) { this.description = description; }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public String getFormattedDescription() {
    try {
      return Jenkins.get().getMarkupFormatter().translate(this.description);
    } catch (IOException e) {
      LOGGER.warning("failed to translate description using configured markup formatter");
      return "";
    } 
  }
  
  @Exported
  public final String getName() { return this.name; }
  
  @Deprecated
  public void buildEnvVars(AbstractBuild<?, ?> build, Map<String, String> env) {
    if (env instanceof EnvVars)
      if (Util.isOverridden(ParameterValue.class, getClass(), "buildEnvironment", new Class[] { Run.class, EnvVars.class })) {
        buildEnvironment(build, (EnvVars)env);
      } else if (Util.isOverridden(ParameterValue.class, getClass(), "buildEnvVars", new Class[] { AbstractBuild.class, EnvVars.class })) {
        buildEnvVars(build, (EnvVars)env);
      }  
  }
  
  @Deprecated
  public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
    if (Util.isOverridden(ParameterValue.class, getClass(), "buildEnvironment", new Class[] { Run.class, EnvVars.class })) {
      buildEnvironment(build, env);
    } else {
      buildEnvVars(build, env);
    } 
  }
  
  public void buildEnvironment(Run<?, ?> build, EnvVars env) {
    if (build instanceof AbstractBuild)
      buildEnvVars((AbstractBuild)build, env); 
  }
  
  public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) { return null; }
  
  public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) { return VariableResolver.NONE; }
  
  @Deprecated
  public ParameterDefinition getDefinition() { throw new UnsupportedOperationException(); }
  
  public int hashCode() { return Objects.hash(new Object[] { this.name }); }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    ParameterValue other = (ParameterValue)obj;
    if (!Objects.equals(this.name, other.name))
      return false; 
    return true;
  }
  
  public String getShortDescription() { return toString(); }
  
  public boolean isSensitive() { return false; }
  
  @CheckForNull
  public Object getValue() { return null; }
  
  public Label getAssignedLabel(SubTask task) { return null; }
}
