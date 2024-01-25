package jenkins.mvn;

import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import java.util.List;
import jenkins.model.Jenkins;

@BridgeMethodsAdded
public abstract class SettingsProviderDescriptor extends Descriptor<SettingsProvider> {
  @WithBridgeMethods({List.class})
  public static DescriptorExtensionList<SettingsProvider, SettingsProviderDescriptor> all() { return Jenkins.get().getDescriptorList(SettingsProvider.class); }
}
