package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Util;
import hudson.diagnosis.OldDataMonitor;
import hudson.model.labels.LabelAssignmentAction;
import hudson.model.queue.SubTask;
import hudson.tasks.BuildWrapper;
import hudson.util.VariableResolver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.RunAction2;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class ParametersAction extends Object implements RunAction2, Iterable<ParameterValue>, Queue.QueueAction, EnvironmentContributingAction, LabelAssignmentAction {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String KEEP_UNDEFINED_PARAMETERS_SYSTEM_PROPERTY_NAME = ParametersAction.class.getName() + ".keepUndefinedParameters";
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String SAFE_PARAMETERS_SYSTEM_PROPERTY_NAME = ParametersAction.class.getName() + ".safeParameters";
  
  private Set<String> safeParameters;
  
  @NonNull
  private List<ParameterValue> parameters;
  
  private List<String> parameterDefinitionNames;
  
  @Deprecated
  private AbstractBuild<?, ?> build;
  
  private Run<?, ?> run;
  
  public ParametersAction(@NonNull List<ParameterValue> parameters) {
    this.parameters = new ArrayList(parameters);
    String paramNames = SystemProperties.getString(SAFE_PARAMETERS_SYSTEM_PROPERTY_NAME);
    this.safeParameters = new TreeSet();
    if (paramNames != null)
      this.safeParameters.addAll(Arrays.asList(paramNames.split(","))); 
  }
  
  public ParametersAction(List<ParameterValue> parameters, Collection<String> additionalSafeParameters) {
    this(parameters);
    if (additionalSafeParameters != null)
      this.safeParameters.addAll(additionalSafeParameters); 
  }
  
  public ParametersAction(ParameterValue... parameters) { this(Arrays.asList(parameters)); }
  
  public void createBuildWrappers(AbstractBuild<?, ?> build, Collection<? super BuildWrapper> result) {
    for (ParameterValue p : getParameters()) {
      if (p == null)
        continue; 
      BuildWrapper w = p.createBuildWrapper(build);
      if (w != null)
        result.add(w); 
    } 
  }
  
  public void buildEnvironment(Run<?, ?> run, EnvVars env) {
    for (ParameterValue p : getParameters()) {
      if (p == null)
        continue; 
      p.buildEnvironment(run, env);
    } 
  }
  
  public String substitute(AbstractBuild<?, ?> build, String text) { return Util.replaceMacro(text, createVariableResolver(build)); }
  
  public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
    VariableResolver[] resolvers = new VariableResolver[getParameters().size() + 1];
    int i = 0;
    for (ParameterValue p : getParameters()) {
      if (p == null)
        continue; 
      resolvers[i++] = p.createVariableResolver(build);
    } 
    resolvers[i] = build.getBuildVariableResolver();
    return new VariableResolver.Union(resolvers);
  }
  
  public Iterator<ParameterValue> iterator() { return getParameters().iterator(); }
  
  @Exported(visibility = 2)
  public List<ParameterValue> getParameters() { return Collections.unmodifiableList(filter(this.parameters)); }
  
  public ParameterValue getParameter(String name) {
    for (ParameterValue p : this.parameters) {
      if (p != null && 
        p.getName().equals(name))
        return p; 
    } 
    return null;
  }
  
  public Label getAssignedLabel(SubTask task) {
    for (ParameterValue p : getParameters()) {
      if (p == null)
        continue; 
      Label l = p.getAssignedLabel(task);
      if (l != null)
        return l; 
    } 
    return null;
  }
  
  public String getDisplayName() { return Messages.ParameterAction_DisplayName(); }
  
  public String getIconFileName() { return "symbol-parameters"; }
  
  public String getUrlName() { return "parameters"; }
  
  public boolean shouldSchedule(List<Action> actions) {
    List<ParametersAction> others = Util.filter(actions, ParametersAction.class);
    if (others.isEmpty())
      return !this.parameters.isEmpty(); 
    Set<ParameterValue> params = new HashSet<ParameterValue>();
    for (ParametersAction other : others)
      params.addAll(other.parameters); 
    return !params.equals(new HashSet(this.parameters));
  }
  
  @NonNull
  public ParametersAction createUpdated(Collection<? extends ParameterValue> overrides) {
    if (overrides == null) {
      ParametersAction parametersAction = new ParametersAction(this.parameters);
      parametersAction.safeParameters = this.safeParameters;
      return parametersAction;
    } 
    List<ParameterValue> combinedParameters = new ArrayList<ParameterValue>(overrides);
    Set<String> names = new HashSet<String>();
    for (ParameterValue v : overrides) {
      if (v == null)
        continue; 
      names.add(v.getName());
    } 
    for (ParameterValue v : this.parameters) {
      if (v != null && 
        !names.contains(v.getName()))
        combinedParameters.add(v); 
    } 
    return new ParametersAction(combinedParameters, this.safeParameters);
  }
  
  @NonNull
  public ParametersAction merge(@CheckForNull ParametersAction overrides) {
    if (overrides == null)
      return new ParametersAction(this.parameters, this.safeParameters); 
    ParametersAction parametersAction = createUpdated(overrides.parameters);
    Set<String> safe = new TreeSet<String>();
    if (parametersAction.safeParameters != null && this.safeParameters != null)
      safe.addAll(this.safeParameters); 
    if (overrides.safeParameters != null)
      safe.addAll(overrides.safeParameters); 
    parametersAction.safeParameters = safe;
    return parametersAction;
  }
  
  @SuppressFBWarnings(value = {"RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE"}, justification = "parameters in readResolve is needed for data migration.")
  private Object readResolve() {
    if (this.parameters == null)
      this.parameters = Collections.emptyList(); 
    if (this.build != null)
      OldDataMonitor.report(this.build, "1.283"); 
    if (this.safeParameters == null)
      this.safeParameters = Collections.emptySet(); 
    return this;
  }
  
  public void onAttached(Run<?, ?> r) {
    ParametersDefinitionProperty p = (ParametersDefinitionProperty)r.getParent().getProperty(ParametersDefinitionProperty.class);
    if (p != null) {
      this.parameterDefinitionNames = new ArrayList(p.getParameterDefinitionNames());
    } else {
      this.parameterDefinitionNames = Collections.emptyList();
    } 
    this.run = r;
  }
  
  public void onLoad(Run<?, ?> r) { this.run = r; }
  
  private List<? extends ParameterValue> filter(List<ParameterValue> parameters) {
    if (this.run == null)
      return parameters; 
    if (this.parameterDefinitionNames == null)
      return parameters; 
    Boolean shouldKeepFlag = SystemProperties.optBoolean(KEEP_UNDEFINED_PARAMETERS_SYSTEM_PROPERTY_NAME);
    if (shouldKeepFlag != null && shouldKeepFlag.booleanValue())
      return parameters; 
    List<ParameterValue> filteredParameters = new ArrayList<ParameterValue>();
    for (ParameterValue v : this.parameters) {
      if (this.parameterDefinitionNames.contains(v.getName()) || isSafeParameter(v.getName())) {
        filteredParameters.add(v);
        continue;
      } 
      if (shouldKeepFlag == null)
        LOGGER.log(Level.WARNING, "Skipped parameter `{0}` as it is undefined on `{1}` (#{2}). Set `-D{3}=true` to allow undefined parameters to be injected as environment variables or `-D{4}=[comma-separated list]` to whitelist specific parameter names, even though it represents a security breach or `-D{3}=false` to no longer show this message.", new Object[] { v

              
              .getName(), this.run.getParent().getFullName(), Integer.valueOf(this.run.getNumber()), KEEP_UNDEFINED_PARAMETERS_SYSTEM_PROPERTY_NAME, SAFE_PARAMETERS_SYSTEM_PROPERTY_NAME }); 
    } 
    return filteredParameters;
  }
  
  public List<ParameterValue> getAllParameters() { return Collections.unmodifiableList(this.parameters); }
  
  private boolean isSafeParameter(String name) { return this.safeParameters.contains(name); }
  
  private static final Logger LOGGER = Logger.getLogger(ParametersAction.class.getName());
}
