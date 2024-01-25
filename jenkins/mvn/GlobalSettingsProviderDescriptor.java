package jenkins.mvn;

import com.infradna.tool.bridge_method_injector.BridgeMethodsAdded;
import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import java.util.List;
import jenkins.model.Jenkins;

@BridgeMethodsAdded
public abstract class GlobalSettingsProviderDescriptor extends Descriptor<GlobalSettingsProvider> {
  @WithBridgeMethods({List.class})
  public static DescriptorExtensionList<GlobalSettingsProvider, GlobalSettingsProviderDescriptor> all() { return Jenkins.get().getDescriptorList(GlobalSettingsProvider.class); }
}
