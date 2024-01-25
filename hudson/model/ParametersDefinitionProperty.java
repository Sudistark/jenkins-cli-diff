package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.queue.ScheduleResult;
import hudson.util.AlternativeUiTextProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.model.OptionalJobProperty;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.util.TimeDuration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 2)
public class ParametersDefinitionProperty extends OptionalJobProperty<Job<?, ?>> implements Action {
  public static final AlternativeUiTextProvider.Message<Job> BUILD_BUTTON_TEXT = new AlternativeUiTextProvider.Message();
  
  private final List<ParameterDefinition> parameterDefinitions;
  
  @DataBoundConstructor
  public ParametersDefinitionProperty(@NonNull List<ParameterDefinition> parameterDefinitions) { this.parameterDefinitions = (parameterDefinitions != null) ? parameterDefinitions : new ArrayList(); }
  
  public ParametersDefinitionProperty(@NonNull ParameterDefinition... parameterDefinitions) { this.parameterDefinitions = (parameterDefinitions != null) ? Arrays.asList(parameterDefinitions) : new ArrayList(); }
  
  private Object readResolve() { return (this.parameterDefinitions == null) ? new ParametersDefinitionProperty(new ParameterDefinition[0]) : this; }
  
  public final String getBuildButtonText() { return AlternativeUiTextProvider.get(BUILD_BUTTON_TEXT, this.owner, Messages.ParametersDefinitionProperty_BuildButtonText()); }
  
  @Deprecated
  public AbstractProject<?, ?> getOwner() { return (AbstractProject)this.owner; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public ParameterizedJobMixIn.ParameterizedJob getJob() { return (ParameterizedJobMixIn.ParameterizedJob)this.owner; }
  
  @Exported
  public List<ParameterDefinition> getParameterDefinitions() { return this.parameterDefinitions; }
  
  public List<String> getParameterDefinitionNames() { return new DefinitionsAbstractList(this.parameterDefinitions); }
  
  @NonNull
  public Collection<Action> getJobActions(Job<?, ?> job) { return Set.of(this); }
  
  @Deprecated
  public Collection<Action> getJobActions(AbstractProject<?, ?> job) { return getJobActions(job); }
  
  @Deprecated
  public AbstractProject<?, ?> getProject() { return (AbstractProject)this.owner; }
  
  @Deprecated
  public void _doBuild(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { _doBuild(req, rsp, TimeDuration.fromString(req.getParameter("delay"))); }
  
  public void _doBuild(StaplerRequest req, StaplerResponse rsp, @QueryParameter TimeDuration delay) throws IOException, ServletException {
    if (delay == null)
      delay = new TimeDuration(TimeUnit.MILLISECONDS.convert(getJob().getQuietPeriod(), TimeUnit.SECONDS)); 
    List<ParameterValue> values = new ArrayList<ParameterValue>();
    JSONObject formData = req.getSubmittedForm();
    Object parameter = formData.get("parameter");
    if (parameter != null) {
      JSONArray a = JSONArray.fromObject(parameter);
      for (Object o : a) {
        JSONObject jo = (JSONObject)o;
        String name = jo.getString("name");
        ParameterDefinition d = getParameterDefinition(name);
        if (d == null)
          throw new IllegalArgumentException("No such parameter definition: " + name); 
        ParameterValue parameterValue = d.createValue(req, jo);
        if (parameterValue != null) {
          values.add(parameterValue);
          continue;
        } 
        throw new IllegalArgumentException("Cannot retrieve the parameter value: " + name);
      } 
    } 
    Queue.WaitingItem item = Jenkins.get().getQueue().schedule(
        getJob(), delay.getTimeInSeconds(), new Action[] { new ParametersAction(values), new CauseAction(new Cause.UserIdCause()) });
    if (item != null) {
      String url = formData.optString("redirectTo");
      if (url == null || !Util.isSafeToRedirectTo(url))
        url = req.getContextPath() + "/" + req.getContextPath(); 
      rsp.sendRedirect(formData.optInt("statusCode", 201), url);
    } else {
      rsp.sendRedirect(".");
    } 
  }
  
  @Deprecated
  public void buildWithParameters(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { buildWithParameters(req, rsp, TimeDuration.fromString(req.getParameter("delay"))); }
  
  public void buildWithParameters(StaplerRequest req, StaplerResponse rsp, @CheckForNull TimeDuration delay) throws IOException, ServletException {
    List<ParameterValue> values = new ArrayList<ParameterValue>();
    for (ParameterDefinition d : this.parameterDefinitions) {
      ParameterValue value = d.createValue(req);
      if (value != null)
        values.add(value); 
    } 
    if (delay == null)
      delay = new TimeDuration(TimeUnit.MILLISECONDS.convert(getJob().getQuietPeriod(), TimeUnit.SECONDS)); 
    ScheduleResult scheduleResult = Jenkins.get().getQueue().schedule2(
        getJob(), delay.getTimeInSeconds(), new Action[] { new ParametersAction(values), ParameterizedJobMixIn.getBuildCause(getJob(), req) });
    Queue.Item item = scheduleResult.getItem();
    if (item != null && !scheduleResult.isCreated()) {
      rsp.sendRedirect(303, req.getContextPath() + "/" + req.getContextPath());
      return;
    } 
    if (item != null) {
      rsp.sendRedirect(201, req.getContextPath() + "/" + req.getContextPath());
      return;
    } 
    rsp.sendRedirect(".");
  }
  
  @CheckForNull
  public ParameterDefinition getParameterDefinition(String name) {
    for (ParameterDefinition pd : this.parameterDefinitions) {
      if (pd.getName().equals(name))
        return pd; 
    } 
    return null;
  }
  
  public String getDisplayName() { return null; }
  
  public String getIconFileName() { return null; }
  
  public String getUrlName() { return null; }
}
