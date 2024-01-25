package jenkins.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildAuthorizationToken;
import hudson.model.Cause;
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
import hudson.search.SearchIndexBuilder;
import hudson.triggers.Trigger;
import hudson.util.AlternativeUiTextProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import jenkins.util.TimeDuration;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public abstract class ParameterizedJobMixIn<JobT extends Job<JobT, RunT> & ParameterizedJobMixIn.ParameterizedJob<JobT, RunT> & Queue.Task, RunT extends Run<JobT, RunT> & Queue.Executable> extends Object {
  protected abstract JobT asJob();
  
  public final boolean scheduleBuild() { return scheduleBuild(((ParameterizedJob)asJob()).getQuietPeriod(), new Cause.LegacyCodeCause()); }
  
  public final boolean scheduleBuild(Cause c) { return scheduleBuild(((ParameterizedJob)asJob()).getQuietPeriod(), c); }
  
  public final boolean scheduleBuild(int quietPeriod) { return scheduleBuild(quietPeriod, new Cause.LegacyCodeCause()); }
  
  public final boolean scheduleBuild(int quietPeriod, Cause c) { return (scheduleBuild2(quietPeriod, (c != null) ? List.of(new CauseAction(c)) : Collections.emptyList()) != null); }
  
  @CheckForNull
  public final QueueTaskFuture<RunT> scheduleBuild2(int quietPeriod, Action... actions) {
    Queue.Item i = scheduleBuild2(quietPeriod, Arrays.asList(actions));
    return (i != null) ? i.getFuture() : null;
  }
  
  @CheckForNull
  public static Queue.Item scheduleBuild2(Job<?, ?> job, int quietPeriod, Action... actions) {
    if (!(job instanceof ParameterizedJob))
      return null; 
    return (new Object(job))


      
      .scheduleBuild2((quietPeriod == -1) ? ((ParameterizedJob)job).getQuietPeriod() : quietPeriod, Arrays.asList(actions));
  }
  
  @CheckForNull
  Queue.Item scheduleBuild2(int quietPeriod, List<Action> actions) {
    if (!((ParameterizedJob)asJob()).isBuildable())
      return null; 
    List<Action> queueActions = new ArrayList<Action>(actions);
    if (isParameterized() && Util.filter(queueActions, ParametersAction.class).isEmpty())
      queueActions.add(new ParametersAction(getDefaultParametersValues())); 
    return Jenkins.get().getQueue().schedule2((Queue.Task)asJob(), quietPeriod, queueActions).getItem();
  }
  
  private List<ParameterValue> getDefaultParametersValues() {
    ParametersDefinitionProperty paramDefProp = (ParametersDefinitionProperty)asJob().getProperty(ParametersDefinitionProperty.class);
    ArrayList<ParameterValue> defValues = new ArrayList<ParameterValue>();
    if (paramDefProp == null)
      return defValues; 
    for (ParameterDefinition paramDefinition : paramDefProp.getParameterDefinitions()) {
      ParameterValue defaultValue = paramDefinition.getDefaultParameterValue();
      if (defaultValue != null)
        defValues.add(defaultValue); 
    } 
    return defValues;
  }
  
  public final boolean isParameterized() { return (asJob().getProperty(ParametersDefinitionProperty.class) != null); }
  
  public final void doBuild(StaplerRequest req, StaplerResponse rsp, @QueryParameter TimeDuration delay) throws IOException, ServletException {
    if (delay == null)
      delay = new TimeDuration(TimeUnit.MILLISECONDS.convert(((ParameterizedJob)asJob()).getQuietPeriod(), TimeUnit.SECONDS)); 
    if (!((ParameterizedJob)asJob()).isBuildable())
      throw HttpResponses.error(409, new IOException(asJob().getFullName() + " is not buildable")); 
    ParametersDefinitionProperty pp = (ParametersDefinitionProperty)asJob().getProperty(ParametersDefinitionProperty.class);
    if (pp != null && !req.getMethod().equals("POST")) {
      req.getView(pp, "index.jelly").forward(req, rsp);
      return;
    } 
    BuildAuthorizationToken.checkPermission(asJob(), ((ParameterizedJob)asJob()).getAuthToken(), req, rsp);
    if (pp != null) {
      pp._doBuild(req, rsp, delay);
      return;
    } 
    Queue.Item item = Jenkins.get().getQueue().schedule2((Queue.Task)asJob(), delay.getTimeInSeconds(), new Action[] { getBuildCause((ParameterizedJob)asJob(), req) }).getItem();
    if (item != null) {
      rsp.sendRedirect(201, req.getContextPath() + "/" + req.getContextPath());
    } else {
      rsp.sendRedirect(".");
    } 
  }
  
  public final void doBuildWithParameters(StaplerRequest req, StaplerResponse rsp, @QueryParameter TimeDuration delay) throws IOException, ServletException {
    BuildAuthorizationToken.checkPermission(asJob(), ((ParameterizedJob)asJob()).getAuthToken(), req, rsp);
    ParametersDefinitionProperty pp = (ParametersDefinitionProperty)asJob().getProperty(ParametersDefinitionProperty.class);
    if (!((ParameterizedJob)asJob()).isBuildable())
      throw HttpResponses.error(409, new IOException(asJob().getFullName() + " is not buildable!")); 
    if (pp != null) {
      pp.buildWithParameters(req, rsp, delay);
    } else {
      throw new IllegalStateException("This build is not parameterized!");
    } 
  }
  
  @RequirePOST
  public final void doCancelQueue(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    asJob().checkPermission(Item.CANCEL);
    Jenkins.get().getQueue().cancel((Queue.Task)asJob());
    rsp.forwardToPreviousPage(req);
  }
  
  public final SearchIndexBuilder extendSearchIndex(SearchIndexBuilder sib) {
    if (((ParameterizedJob)asJob()).isBuildable() && asJob().hasPermission(Item.BUILD))
      sib.add("build", "build"); 
    return sib;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static CauseAction getBuildCause(ParameterizedJob job, StaplerRequest req) {
    Cause.UserIdCause userIdCause;
    BuildAuthorizationToken authToken = job.getAuthToken();
    if (authToken != null && authToken.getToken() != null && req.getParameter("token") != null) {
      String causeText = req.getParameter("cause");
      userIdCause = new Cause.RemoteCause(req.getRemoteAddr(), causeText);
    } else {
      userIdCause = new Cause.UserIdCause();
    } 
    return new CauseAction(userIdCause);
  }
  
  public static final AlternativeUiTextProvider.Message<ParameterizedJob> BUILD_NOW_TEXT = new AlternativeUiTextProvider.Message();
  
  public static final AlternativeUiTextProvider.Message<ParameterizedJob> BUILD_WITH_PARAMETERS_TEXT = new AlternativeUiTextProvider.Message();
  
  public final String getBuildNowText() {
    return isParameterized() ? AlternativeUiTextProvider.get(BUILD_WITH_PARAMETERS_TEXT, (ParameterizedJob)asJob(), 
        AlternativeUiTextProvider.get(BUILD_NOW_TEXT, (ParameterizedJob)asJob(), Messages.ParameterizedJobMixIn_build_with_parameters())) : 
      AlternativeUiTextProvider.get(BUILD_NOW_TEXT, (ParameterizedJob)asJob(), Messages.ParameterizedJobMixIn_build_now());
  }
  
  @CheckForNull
  public static <T extends Trigger<?>> T getTrigger(Job<?, ?> job, Class<T> clazz) {
    if (!(job instanceof ParameterizedJob))
      return null; 
    for (Trigger<?> t : ((ParameterizedJob)job).getTriggers().values()) {
      if (clazz.isInstance(t))
        return (T)(Trigger)clazz.cast(t); 
    } 
    return null;
  }
}
