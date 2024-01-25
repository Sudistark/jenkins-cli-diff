package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.model.labels.LabelAtom;
import hudson.model.labels.LabelExpression;
import hudson.model.labels.LabelExpressionLexer;
import hudson.model.labels.LabelExpressionParser;
import hudson.model.labels.LabelOperatorPrecedence;
import hudson.model.labels.LabelVisitor;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import hudson.util.QuotedStringTokenizer;
import hudson.util.VariableResolver;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithChildren;
import jenkins.model.ModelObjectWithContextMenu;
import jenkins.util.antlr.JenkinsANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class Label extends Actionable implements Comparable<Label>, ModelObjectWithChildren {
  @NonNull
  protected final String name;
  
  @Exported
  @NonNull
  public final LoadStatistics loadStatistics;
  
  @NonNull
  public final NodeProvisioner nodeProvisioner;
  
  protected Label(@NonNull String name) {
    this.name = name;
    this.loadStatistics = new Object(this, 0, 0);
    this.nodeProvisioner = new NodeProvisioner(this, this.loadStatistics);
  }
  
  @Exported(visibility = 2)
  @NonNull
  public final String getName() { return getDisplayName(); }
  
  @NonNull
  public String getDisplayName() { return this.name; }
  
  public String getUrl() { return "label/" + Util.rawEncode(this.name) + "/"; }
  
  public String getSearchUrl() { return getUrl(); }
  
  public boolean isAtom() { return false; }
  
  public final boolean matches(Collection<LabelAtom> labels) { return matches(new Object(this, labels)); }
  
  public final boolean matches(Node n) { return matches(n.getAssignedLabels()); }
  
  public boolean isSelfLabel() {
    Set<Node> nodes = getNodes();
    return (nodes.size() == 1 && ((Node)nodes.iterator().next()).getSelfLabel().equals(this));
  }
  
  @Exported
  public Set<Node> getNodes() {
    Set<Node> nodes = this.nodes;
    if (nodes != null)
      return nodes; 
    Set<Node> r = new HashSet<Node>();
    Jenkins h = Jenkins.get();
    if (matches(h))
      r.add(h); 
    for (Node n : h.getNodes()) {
      if (matches(n))
        r.add(n); 
    } 
    return this.nodes = Collections.unmodifiableSet(r);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public Set<Node> getSortedNodes() {
    Set<Node> r = new TreeSet<Node>(new NodeSorter());
    r.addAll(getNodes());
    return r;
  }
  
  @Exported
  public Set<Cloud> getClouds() {
    if (this.clouds == null) {
      Set<Cloud> r = new HashSet<Cloud>();
      Jenkins h = Jenkins.get();
      for (Cloud c : h.clouds) {
        if (c.canProvision(this))
          r.add(c); 
      } 
      this.clouds = Collections.unmodifiableSet(r);
    } 
    return this.clouds;
  }
  
  public boolean isAssignable() {
    for (Node n : getNodes()) {
      if (n.getNumExecutors() > 0)
        return true; 
    } 
    return !getClouds().isEmpty();
  }
  
  public int getTotalConfiguredExecutors() {
    int r = 0;
    for (Node n : getNodes())
      r += n.getNumExecutors(); 
    return r;
  }
  
  @Exported
  public int getTotalExecutors() {
    int r = 0;
    for (Node n : getNodes()) {
      Computer c = n.toComputer();
      if (c != null && c.isOnline())
        r += c.countExecutors(); 
    } 
    return r;
  }
  
  @Exported
  public int getBusyExecutors() {
    int r = 0;
    for (Node n : getNodes()) {
      Computer c = n.toComputer();
      if (c != null && c.isOnline())
        r += c.countBusy(); 
    } 
    return r;
  }
  
  @Exported
  public int getIdleExecutors() {
    int r = 0;
    for (Node n : getNodes()) {
      Computer c = n.toComputer();
      if (c != null && (c.isOnline() || c.isConnecting()) && c.isAcceptingTasks())
        r += c.countIdle(); 
    } 
    return r;
  }
  
  @Exported
  public boolean isOffline() {
    for (Node n : getNodes()) {
      Computer c = n.toComputer();
      if (c != null && !c.isOffline())
        return false; 
    } 
    return true;
  }
  
  @Exported
  public String getDescription() {
    Set<Node> nodes = getNodes();
    if (nodes.isEmpty()) {
      Set<Cloud> clouds = getClouds();
      if (clouds.isEmpty())
        return Messages.Label_InvalidLabel(); 
      return Messages.Label_ProvisionedFrom(toString(clouds));
    } 
    if (nodes.size() == 1)
      return ((Node)nodes.iterator().next()).getNodeDescription(); 
    return Messages.Label_GroupOf(toString(nodes));
  }
  
  private String toString(Collection<? extends ModelObject> model) {
    boolean first = true;
    StringBuilder buf = new StringBuilder();
    for (ModelObject c : model) {
      if (buf.length() > 80) {
        buf.append(",...");
        break;
      } 
      if (!first) {
        buf.append(',');
      } else {
        first = false;
      } 
      buf.append(c.getDisplayName());
    } 
    return buf.toString();
  }
  
  @Exported
  public List<AbstractProject> getTiedJobs() {
    return (List)StreamSupport.stream(Jenkins.get().allItems(AbstractProject.class, i -> 
          (i instanceof TopLevelItem && equals(i.getAssignedLabel()))).spliterator(), true)
      
      .sorted(Items.BY_FULL_NAME).collect(Collectors.toList());
  }
  
  public int getTiedJobCount() {
    if (this.tiedJobsCount != -1)
      return this.tiedJobsCount; 
    ACLContext ctx = ACL.as2(ACL.SYSTEM2);
    try {
      int result = 0;
      for (AbstractProject ignored : Jenkins.get().allItems(AbstractProject.class, p -> matches(p.getAssignedLabelString())))
        result++; 
      int i = this.tiedJobsCount = result;
      if (ctx != null)
        ctx.close(); 
      return i;
    } catch (Throwable throwable) {
      if (ctx != null)
        try {
          ctx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  public boolean contains(Node node) { return getNodes().contains(node); }
  
  public boolean isEmpty() { return (getNodes().isEmpty() && getClouds().isEmpty()); }
  
  void reset() {
    this.nodes = null;
    this.clouds = null;
    this.tiedJobsCount = -1;
  }
  
  public Api getApi() { return new Api(this); }
  
  public Set<LabelAtom> listAtoms() {
    Set<LabelAtom> r = new HashSet<LabelAtom>();
    accept(ATOM_COLLECTOR, r);
    return r;
  }
  
  public Label and(Label rhs) { return new LabelExpression.And(this, rhs); }
  
  public Label or(Label rhs) { return new LabelExpression.Or(this, rhs); }
  
  public Label iff(Label rhs) { return new LabelExpression.Iff(this, rhs); }
  
  public Label implies(Label rhs) { return new LabelExpression.Implies(this, rhs); }
  
  public Label not() { return new LabelExpression.Not(this); }
  
  public Label paren() { return new LabelExpression.Paren(this); }
  
  public final boolean equals(Object that) {
    if (this == that)
      return true; 
    if (that == null || getClass() != that.getClass())
      return false; 
    return matches(((Label)that).name);
  }
  
  public final int hashCode() { return this.name.hashCode(); }
  
  public final int compareTo(Label that) { return this.name.compareTo(that.name); }
  
  private boolean matches(String name) { return this.name.equals(name); }
  
  public String toString() { return this.name; }
  
  public ModelObjectWithContextMenu.ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
    ModelObjectWithContextMenu.ContextMenu menu = new ModelObjectWithContextMenu.ContextMenu();
    for (Node node : getNodes())
      menu.add(node); 
    return menu;
  }
  
  @NonNull
  public static Set<LabelAtom> parse(@CheckForNull String labels) {
    Set<LabelAtom> r = new TreeSet<LabelAtom>();
    labels = Util.fixNull(labels);
    if (labels.length() > 0) {
      Jenkins j = Jenkins.get();
      LabelAtom labelAtom = j.tryGetLabelAtom(labels);
      if (labelAtom == null) {
        QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(labels);
        while (tokenizer.hasMoreTokens())
          r.add(j.getLabelAtom(tokenizer.nextToken())); 
      } else {
        r.add(labelAtom);
      } 
    } 
    return r;
  }
  
  @CheckForNull
  public static Label get(String l) { return Jenkins.get().getLabel(l); }
  
  public static Label parseExpression(@NonNull String labelExpression) {
    LabelExpressionLexer lexer = new LabelExpressionLexer(CharStreams.fromString(labelExpression));
    lexer.removeErrorListeners();
    lexer.addErrorListener(new JenkinsANTLRErrorListener());
    LabelExpressionParser parser = new LabelExpressionParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    parser.addErrorListener(new JenkinsANTLRErrorListener());
    return (parser.expr()).l;
  }
  
  private static final LabelVisitor<Void, Set<LabelAtom>> ATOM_COLLECTOR = new Object();
  
  public abstract String getExpression();
  
  public abstract boolean matches(VariableResolver<Boolean> paramVariableResolver);
  
  public abstract <V, P> V accept(LabelVisitor<V, P> paramLabelVisitor, P paramP);
  
  public abstract LabelOperatorPrecedence precedence();
}
