package hudson.model;

import hudson.Util;
import hudson.scm.SCM;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrappers;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import hudson.tasks.Publisher;
import hudson.triggers.SCMTrigger;
import hudson.triggers.Trigger;
import hudson.util.DescribableList;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.triggers.SCMTriggerItem;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class Project<P extends Project<P, B>, B extends Build<P, B>> extends AbstractProject<P, B> implements SCMTriggerItem, Saveable, Maven.ProjectWithMaven, BuildableItemWithBuildWrappers {
  private static final AtomicReferenceFieldUpdater<Project, DescribableList> buildersSetter = AtomicReferenceFieldUpdater.newUpdater(Project.class, DescribableList.class, "builders");
  
  private static final AtomicReferenceFieldUpdater<Project, DescribableList> publishersSetter = AtomicReferenceFieldUpdater.newUpdater(Project.class, DescribableList.class, "publishers");
  
  private static final AtomicReferenceFieldUpdater<Project, DescribableList> buildWrappersSetter = AtomicReferenceFieldUpdater.newUpdater(Project.class, DescribableList.class, "buildWrappers");
  
  protected Project(ItemGroup parent, String name) { super(parent, name); }
  
  public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
    super.onLoad(parent, name);
    getBuildersList().setOwner(this);
    getPublishersList().setOwner(this);
    getBuildWrappersList().setOwner(this);
  }
  
  public AbstractProject<?, ?> asProject() { return this; }
  
  public Item asItem() { return this; }
  
  public SCMTrigger getSCMTrigger() { return (SCMTrigger)getTrigger(SCMTrigger.class); }
  
  public Collection<? extends SCM> getSCMs() { return SCMTriggerItem.SCMTriggerItems.resolveMultiScmIfConfigured(getScm()); }
  
  public List<Builder> getBuilders() { return getBuildersList().toList(); }
  
  @Deprecated
  public Map<Descriptor<Publisher>, Publisher> getPublishers() { return getPublishersList().toMap(); }
  
  public DescribableList<Builder, Descriptor<Builder>> getBuildersList() {
    if (this.builders == null)
      buildersSetter.compareAndSet(this, null, new DescribableList(this)); 
    return this.builders;
  }
  
  public DescribableList<Publisher, Descriptor<Publisher>> getPublishersList() {
    if (this.publishers == null)
      publishersSetter.compareAndSet(this, null, new DescribableList(this)); 
    return this.publishers;
  }
  
  public Map<Descriptor<BuildWrapper>, BuildWrapper> getBuildWrappers() { return getBuildWrappersList().toMap(); }
  
  public DescribableList<BuildWrapper, Descriptor<BuildWrapper>> getBuildWrappersList() {
    if (this.buildWrappers == null)
      buildWrappersSetter.compareAndSet(this, null, new DescribableList(this)); 
    return this.buildWrappers;
  }
  
  protected Set<ResourceActivity> getResourceActivities() {
    Set<ResourceActivity> activities = new HashSet<ResourceActivity>();
    activities.addAll(super.getResourceActivities());
    activities.addAll(Util.filter(getBuildersList(), ResourceActivity.class));
    activities.addAll(Util.filter(getPublishersList(), ResourceActivity.class));
    activities.addAll(Util.filter(getBuildWrappersList(), ResourceActivity.class));
    return activities;
  }
  
  @Deprecated
  public void addPublisher(Publisher buildStep) throws IOException { getPublishersList().add(buildStep); }
  
  @Deprecated
  public void removePublisher(Descriptor<Publisher> descriptor) throws IOException { getPublishersList().remove(descriptor); }
  
  public Publisher getPublisher(Descriptor<Publisher> descriptor) {
    for (Publisher p : getPublishersList()) {
      if (p.getDescriptor() == descriptor)
        return p; 
    } 
    return null;
  }
  
  protected void buildDependencyGraph(DependencyGraph graph) {
    super.buildDependencyGraph(graph);
    getPublishersList().buildDependencyGraph(this, graph);
    getBuildersList().buildDependencyGraph(this, graph);
    getBuildWrappersList().buildDependencyGraph(this, graph);
  }
  
  public boolean isFingerprintConfigured() { return (getPublishersList().get(hudson.tasks.Fingerprinter.class) != null); }
  
  public Maven.MavenInstallation inferMavenInstallation() {
    Maven m = (Maven)getBuildersList().get(Maven.class);
    if (m != null)
      return m.getMaven(); 
    return null;
  }
  
  protected void submit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    super.submit(req, rsp);
    JSONObject json = req.getSubmittedForm();
    getBuildWrappersList().rebuild(req, json, BuildWrappers.getFor(this));
    getBuildersList().rebuildHetero(req, json, Builder.all(), "builder");
    getPublishersList().rebuildHetero(req, json, Publisher.all(), "publisher");
  }
  
  protected List<Action> createTransientActions() {
    List<Action> r = super.createTransientActions();
    for (BuildStep step : getBuildersList()) {
      try {
        r.addAll(step.getProjectActions(this));
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Error loading build step.", e);
      } 
    } 
    for (BuildStep step : getPublishersList()) {
      try {
        r.addAll(step.getProjectActions(this));
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Error loading publisher.", e);
      } 
    } 
    for (BuildWrapper step : getBuildWrappers().values()) {
      try {
        r.addAll(step.getProjectActions(this));
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Error loading build wrapper.", e);
      } 
    } 
    for (Trigger trigger : triggers()) {
      try {
        r.addAll(trigger.getProjectActions());
      } catch (RuntimeException e) {
        LOGGER.log(Level.SEVERE, "Error loading trigger.", e);
      } 
    } 
    return r;
  }
  
  private static final Logger LOGGER = Logger.getLogger(Project.class.getName());
}
