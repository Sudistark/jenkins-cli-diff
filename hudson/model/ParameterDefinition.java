package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.cli.CLICommand;
import hudson.util.DescriptorList;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 3)
public abstract class ParameterDefinition extends Object implements Describable<ParameterDefinition>, ExtensionPoint, Serializable {
  private final String name;
  
  private String description;
  
  protected ParameterDefinition(@NonNull String name) {
    if (name == null)
      throw new IllegalArgumentException("Parameter name must be non-null"); 
    this.name = name;
  }
  
  @Deprecated
  protected ParameterDefinition(@NonNull String name, String description) {
    this(name);
    setDescription(description);
  }
  
  public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) { return this; }
  
  @Exported
  public String getType() { return getClass().getSimpleName(); }
  
  @Exported
  @NonNull
  public String getName() { return this.name; }
  
  @Exported
  @CheckForNull
  public String getDescription() { return this.description; }
  
  @DataBoundSetter
  public void setDescription(@CheckForNull String description) { this.description = Util.fixEmpty(description); }
  
  @CheckForNull
  public String getFormattedDescription() {
    try {
      return Jenkins.get().getMarkupFormatter().translate(getDescription());
    } catch (IOException e) {
      LOGGER.warning("failed to translate description using configured markup formatter");
      return "";
    } 
  }
  
  @NonNull
  public ParameterDescriptor getDescriptor() { return (ParameterDescriptor)Jenkins.get().getDescriptorOrDie(getClass()); }
  
  @CheckForNull
  public ParameterValue createValue(CLICommand command, String value) throws IOException, InterruptedException {
    throw new AbortException("CLI parameter submission is not supported for the " + getClass() + " type. Please file a bug report for this");
  }
  
  @Exported
  @CheckForNull
  public ParameterValue getDefaultParameterValue() { return null; }
  
  public boolean isValid(ParameterValue value) { return true; }
  
  public int hashCode() { return Jenkins.XSTREAM2.toXML(this).hashCode(); }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    ParameterDefinition other = (ParameterDefinition)obj;
    if (!Objects.equals(getName(), other.getName()))
      return false; 
    if (!Objects.equals(getDescription(), other.getDescription()))
      return false; 
    String thisXml = Jenkins.XSTREAM2.toXML(this);
    String otherXml = Jenkins.XSTREAM2.toXML(other);
    return thisXml.equals(otherXml);
  }
  
  public static DescriptorExtensionList<ParameterDefinition, ParameterDescriptor> all() { return Jenkins.get().getDescriptorList(ParameterDefinition.class); }
  
  @Deprecated
  public static final DescriptorList<ParameterDefinition> LIST = new DescriptorList(ParameterDefinition.class);
  
  private static final Logger LOGGER = Logger.getLogger(ParameterDefinition.class.getName());
  
  @CheckForNull
  public abstract ParameterValue createValue(StaplerRequest paramStaplerRequest, JSONObject paramJSONObject);
  
  @CheckForNull
  public abstract ParameterValue createValue(StaplerRequest paramStaplerRequest);
}
