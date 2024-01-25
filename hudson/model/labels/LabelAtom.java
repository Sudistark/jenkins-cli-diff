package hudson.model.labels;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.BulkChange;
import hudson.XmlFile;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Failure;
import hudson.model.Label;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.util.DescribableList;
import hudson.util.EditDistance;
import hudson.util.FormApply;
import hudson.util.QuotedStringTokenizer;
import hudson.util.VariableResolver;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

public class LabelAtom extends Label implements Saveable {
  private static final Pattern PROHIBITED_DOUBLE_DOT = Pattern.compile(".*\\.\\.[\\\\/].*");
  
  private static boolean ALLOW_FOLDER_TRAVERSAL = SystemProperties.getBoolean(LabelAtom.class.getName() + ".allowFolderTraversal");
  
  private DescribableList<LabelAtomProperty, LabelAtomPropertyDescriptor> properties = new DescribableList(this);
  
  private String description;
  
  public LabelAtom(@NonNull String name) { super(name); }
  
  public String getExpression() { return escape(this.name); }
  
  public boolean isAtom() { return true; }
  
  @NonNull
  public List<Action> getActions() {
    List<Action> actions = new Vector<Action>(super.getActions());
    actions.addAll(this.transientActions);
    return Collections.unmodifiableList(actions);
  }
  
  protected void updateTransientActions() {
    Vector<Action> ta = new Vector<Action>();
    for (LabelAtomProperty p : this.properties)
      ta.addAll(p.getActions(this)); 
    this.transientActions = ta;
  }
  
  public String getDescription() { return this.description; }
  
  public void setDescription(String description) {
    this.description = description;
    save();
  }
  
  public DescribableList<LabelAtomProperty, LabelAtomPropertyDescriptor> getProperties() { return this.properties; }
  
  @Exported
  public List<LabelAtomProperty> getPropertiesList() { return this.properties.toList(); }
  
  public boolean matches(VariableResolver<Boolean> resolver) { return ((Boolean)resolver.resolve(this.name)).booleanValue(); }
  
  public <V, P> V accept(LabelVisitor<V, P> visitor, P param) { return (V)visitor.onAtom(this, param); }
  
  public Set<LabelAtom> listAtoms() { return Set.of(this); }
  
  public LabelOperatorPrecedence precedence() { return LabelOperatorPrecedence.ATOM; }
  
  XmlFile getConfigFile() {
    return new XmlFile(XSTREAM, new File((Jenkins.get()).root, "labels/" + this.name + ".xml"));
  }
  
  public void save() {
    if (isInvalidName())
      throw new IOException("Invalid label"); 
    if (BulkChange.contains(this))
      return; 
    try {
      getConfigFile().write(this);
      SaveableListener.fireOnChange(this, getConfigFile());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to save " + getConfigFile(), e);
    } 
  }
  
  public void load() {
    XmlFile file = getConfigFile();
    if (file.exists())
      try {
        file.unmarshal(this);
      } catch (IOException e) {
        LOGGER.log(Level.WARNING, "Failed to load " + file, e);
      }  
    this.properties.setOwner(this);
    updateTransientActions();
  }
  
  public List<LabelAtomPropertyDescriptor> getApplicablePropertyDescriptors() { return LabelAtomProperty.all(); }
  
  @POST
  public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    Jenkins app = Jenkins.get();
    app.checkPermission(Jenkins.ADMINISTER);
    if (isInvalidName())
      throw new Descriptor.FormException("Invalid label", null); 
    this.properties.rebuild(req, req.getSubmittedForm(), getApplicablePropertyDescriptors());
    this.description = req.getSubmittedForm().getString("description");
    updateTransientActions();
    save();
    FormApply.success(".").generateResponse(req, rsp, null);
  }
  
  private boolean isInvalidName() { return (!ALLOW_FOLDER_TRAVERSAL && PROHIBITED_DOUBLE_DOT.matcher(this.name).matches()); }
  
  @RequirePOST
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    setDescription(req.getParameter("description"));
    rsp.sendRedirect(".");
  }
  
  @Nullable
  public static LabelAtom get(@CheckForNull String l) { return Jenkins.get().getLabelAtom(l); }
  
  public static LabelAtom findNearest(String name) {
    List<String> candidates = new ArrayList<String>();
    for (LabelAtom a : Jenkins.get().getLabelAtoms())
      candidates.add(a.getName()); 
    return get(EditDistance.findNearest(name, candidates));
  }
  
  public static boolean needsEscape(String name) {
    try {
      Jenkins.checkGoodName(name);
      for (int i = 0; i < name.length(); i++) {
        char ch = name.charAt(i);
        if (" ()\t\n".indexOf(ch) != -1)
          return true; 
      } 
      return false;
    } catch (Failure failure) {
      return true;
    } 
  }
  
  public static String escape(String name) {
    if (needsEscape(name))
      return QuotedStringTokenizer.quote(name); 
    return name;
  }
  
  private static final Logger LOGGER = Logger.getLogger(LabelAtom.class.getName());
  
  private static final XStream2 XSTREAM = new XStream2();
  
  static  {
    XSTREAM.registerConverter(new LabelAtomConverter(), 100);
  }
}
