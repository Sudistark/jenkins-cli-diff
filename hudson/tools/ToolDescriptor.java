package hudson.tools;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Descriptor;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public abstract class ToolDescriptor<T extends ToolInstallation> extends Descriptor<ToolInstallation> {
  private T[] installations;
  
  protected ToolDescriptor() {}
  
  protected ToolDescriptor(Class<T> clazz) { super(clazz); }
  
  public T[] getInstallations() {
    if (this.installations != null)
      return (T[])(ToolInstallation[])this.installations.clone(); 
    Type bt = Types.getBaseClass(getClass(), ToolDescriptor.class);
    if (bt instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType)bt;
      Class t = Types.erasure(pt.getActualTypeArguments()[0]);
      return (T[])(ToolInstallation[])Array.newInstance(t, 0);
    } 
    return (T[])emptyArray_unsafeCast();
  }
  
  @SuppressFBWarnings(value = {"BC_IMPOSSIBLE_DOWNCAST"}, justification = "Such casting is generally unsafe, but we use it as a last resort.")
  private T[] emptyArray_unsafeCast() { return (T[])(ToolInstallation[])new Object[0]; }
  
  public void setInstallations(T... installations) { this.installations = (ToolInstallation[])installations.clone(); }
  
  public List<ToolPropertyDescriptor> getPropertyDescriptors() { return PropertyDescriptor.for_(ToolProperty.all(), this.clazz); }
  
  @NonNull
  public GlobalConfigurationCategory getCategory() { return GlobalConfigurationCategory.get(jenkins.tools.ToolConfigurationCategory.class); }
  
  public List<? extends ToolInstaller> getDefaultInstallers() { return Collections.emptyList(); }
  
  public DescribableList<ToolProperty<?>, ToolPropertyDescriptor> getDefaultProperties() throws IOException {
    DescribableList<ToolProperty<?>, ToolPropertyDescriptor> r = new DescribableList<ToolProperty<?>, ToolPropertyDescriptor>(NOOP);
    List<? extends ToolInstaller> installers = getDefaultInstallers();
    if (!installers.isEmpty())
      r.add(new InstallSourceProperty(installers)); 
    return r;
  }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    setInstallations((ToolInstallation[])req.bindJSONToList(this.clazz, json.get("tool")).toArray((ToolInstallation[])Array.newInstance(this.clazz, 0)));
    return true;
  }
  
  public FormValidation doCheckHome(@QueryParameter File value) {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    if (value.getPath().isEmpty())
      return FormValidation.ok(); 
    if (!value.isDirectory())
      return FormValidation.warning(Messages.ToolDescriptor_NotADirectory(value)); 
    return checkHomeDirectory(value);
  }
  
  protected FormValidation checkHomeDirectory(File home) { return FormValidation.ok(); }
  
  public FormValidation doCheckName(@QueryParameter String value) { return FormValidation.validateRequired(value); }
}
