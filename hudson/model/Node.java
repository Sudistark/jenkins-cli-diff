package hudson.model;

import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.FileSystemProvisioner;
import hudson.Launcher;
import hudson.Util;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.CauseOfBlockage;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.slaves.NodeDescriptor;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.OfflineCause;
import hudson.util.ClockDifference;
import hudson.util.DescribableList;
import hudson.util.TagCloud;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import jenkins.util.io.OnMaster;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.BindInterceptor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.springframework.security.core.Authentication;

@ExportedBean
@BridgeMethodsAdded
public abstract class Node extends AbstractModelObject implements ReconfigurableDescribable<Node>, ExtensionPoint, AccessControlled, OnMaster, Saveable {
  private static final Logger LOGGER = Logger.getLogger(Node.class.getName());
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean SKIP_BUILD_CHECK_ON_FLYWEIGHTS = SystemProperties.getBoolean(Node.class.getName() + ".SKIP_BUILD_CHECK_ON_FLYWEIGHTS", true);
  
  private OfflineCause temporaryOfflineCause;
  
  public String getDisplayName() { return getNodeName(); }
  
  public String getSearchUrl() {
    Computer c = toComputer();
    if (c != null)
      return c.getUrl(); 
    return "computer/" + Util.rawEncode(getNodeName());
  }
  
  public boolean isHoldOffLaunchUntilSave() { return this.holdOffLaunchUntilSave; }
  
  public void save() {
    Jenkins jenkins = Jenkins.getInstanceOrNull();
    if (jenkins != null)
      jenkins.updateNode(this); 
  }
  
  @CheckForNull
  public final Computer toComputer() {
    Jenkins jenkins = Jenkins.get();
    return jenkins.getComputer(this);
  }
  
  @CheckForNull
  public final VirtualChannel getChannel() {
    Computer c = toComputer();
    return (c == null) ? null : c.getChannel();
  }
  
  public boolean isAcceptingTasks() { return true; }
  
  void setTemporaryOfflineCause(OfflineCause cause) {
    try {
      if (this.temporaryOfflineCause != cause) {
        this.temporaryOfflineCause = cause;
        save();
      } 
    } catch (IOException e) {
      LOGGER.warning("Unable to complete save, temporary offline status will not be persisted: " + e.getMessage());
    } 
  }
  
  public OfflineCause getTemporaryOfflineCause() { return this.temporaryOfflineCause; }
  
  public TagCloud<LabelAtom> getLabelCloud() { return new TagCloud(getAssignedLabels(), Label::getTiedJobCount); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  protected Set<LabelAtom> getLabelAtomSet() { return Collections.unmodifiableSet(Label.parse(getLabelString())); }
  
  @Exported
  public Set<LabelAtom> getAssignedLabels() {
    Set<LabelAtom> r = new HashSet<LabelAtom>(getLabelAtomSet());
    r.add(getSelfLabel());
    r.addAll(getDynamicLabels());
    return Collections.unmodifiableSet(r);
  }
  
  private HashSet<LabelAtom> getDynamicLabels() {
    HashSet<LabelAtom> result = new HashSet<LabelAtom>();
    for (LabelFinder labeler : LabelFinder.all()) {
      for (Label label : labeler.findLabels(this)) {
        if (label instanceof LabelAtom)
          result.add((LabelAtom)label); 
      } 
    } 
    return result;
  }
  
  public void setLabelString(String labelString) { throw new UnsupportedOperationException(); }
  
  @NonNull
  @WithBridgeMethods({Label.class})
  public LabelAtom getSelfLabel() { return LabelAtom.get(getNodeName()); }
  
  @Deprecated
  public CauseOfBlockage canTake(Queue.Task task) { return null; }
  
  public CauseOfBlockage canTake(Queue.BuildableItem item) {
    Label l = item.getAssignedLabel();
    if (l != null && !l.contains(this))
      return CauseOfBlockage.fromMessage(Messages._Node_LabelMissing(getDisplayName(), l)); 
    if (l == null && getMode() == Mode.EXCLUSIVE)
      if (item.task instanceof Queue.FlyweightTask) {
        if (!(this instanceof Jenkins))
          if (Jenkins.get().getNumExecutors() >= 1 && 
            Jenkins.get().getMode() != Mode.EXCLUSIVE)
            return CauseOfBlockage.fromMessage(Messages._Node_BecauseNodeIsReserved(getDisplayName()));  
      } else {
        return CauseOfBlockage.fromMessage(Messages._Node_BecauseNodeIsReserved(getDisplayName()));
      }  
    Authentication identity = item.authenticate2();
    if ((!SKIP_BUILD_CHECK_ON_FLYWEIGHTS || !(item.task instanceof Queue.FlyweightTask)) && !hasPermission2(identity, Computer.BUILD))
      return CauseOfBlockage.fromMessage(Messages._Node_LackingBuildPermission(identity.getName(), getDisplayName())); 
    for (NodeProperty prop : getNodeProperties()) {
      CauseOfBlockage c;
      try {
        c = prop.canTake(item);
      } catch (Throwable t) {
        LOGGER.log(Level.WARNING, t, () -> String.format("Exception evaluating if the node '%s' can take the task '%s'", new Object[] { getNodeName(), item.task.getName() }));
        c = CauseOfBlockage.fromMessage(Messages._Queue_ExceptionCanTake());
      } 
      if (c != null)
        return c; 
    } 
    if (!isAcceptingTasks())
      return new CauseOfBlockage.BecauseNodeIsNotAcceptingTasks(this); 
    return null;
  }
  
  @CheckForNull
  public FilePath createPath(String absolutePath) {
    VirtualChannel ch = getChannel();
    if (ch == null)
      return null; 
    return new FilePath(ch, absolutePath);
  }
  
  @Deprecated
  public FileSystemProvisioner getFileSystemProvisioner() { return FileSystemProvisioner.DEFAULT; }
  
  @CheckForNull
  public <T extends NodeProperty> T getNodeProperty(Class<T> clazz) {
    for (NodeProperty p : getNodeProperties()) {
      if (clazz.isInstance(p))
        return (T)(NodeProperty)clazz.cast(p); 
    } 
    return null;
  }
  
  @CheckForNull
  public NodeProperty getNodeProperty(String className) {
    for (NodeProperty p : getNodeProperties()) {
      if (p.getClass().getName().equals(className))
        return p; 
    } 
    return null;
  }
  
  public List<NodePropertyDescriptor> getNodePropertyDescriptors() { return NodeProperty.for_(this); }
  
  @NonNull
  public ACL getACL() { return Jenkins.get().getAuthorizationStrategy().getACL(this); }
  
  public Node reconfigure(@NonNull StaplerRequest req, JSONObject form) throws Descriptor.FormException {
    if (form == null)
      return null; 
    JSONObject jsonForProperties = form.optJSONObject("nodeProperties");
    old = new AtomicReference<BindInterceptor>();
    old.set(req.setBindInterceptor(new Object(this, jsonForProperties, old, req)));
    try {
      return (Node)getDescriptor().newInstance(req, form);
    } finally {
      req.setBindListener((BindInterceptor)old.get());
    } 
  }
  
  public ClockDifference getClockDifference() throws IOException, InterruptedException {
    VirtualChannel channel = getChannel();
    if (channel == null)
      throw new IOException(getNodeName() + " is offline"); 
    return (ClockDifference)channel.call(getClockDifferenceCallable());
  }
  
  @Exported(visibility = 999)
  @NonNull
  public abstract String getNodeName();
  
  @Deprecated
  public abstract void setNodeName(String paramString);
  
  @Exported
  public abstract String getNodeDescription();
  
  public abstract Launcher createLauncher(TaskListener paramTaskListener);
  
  @Exported
  public abstract int getNumExecutors();
  
  @Exported
  public abstract Mode getMode();
  
  @Restricted({org.kohsuke.accmod.restrictions.ProtectedExternally.class})
  @CheckForNull
  protected abstract Computer createComputer();
  
  public abstract String getLabelString();
  
  @CheckForNull
  public abstract FilePath getWorkspaceFor(TopLevelItem paramTopLevelItem);
  
  @CheckForNull
  public abstract FilePath getRootPath();
  
  @NonNull
  public abstract DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties();
  
  public abstract NodeDescriptor getDescriptor();
  
  public abstract Callable<ClockDifference, IOException> getClockDifferenceCallable();
}
