package jenkins.views;

import hudson.ExtensionList;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.util.AdministrativeError;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

public abstract class PartialHeader extends Header {
  private static Logger LOGGER = Logger.getLogger(PartialHeader.class.getName());
  
  private static final int compatibilityHeaderVersion = 1;
  
  public final boolean isCompatible() { return (1 == getSupportedHeaderVersion()); }
  
  @Initializer(after = InitMilestone.JOB_LOADED, before = InitMilestone.JOB_CONFIG_ADAPTED)
  public static void incompatibleHeaders() {
    ExtensionList.lookup(PartialHeader.class).stream().filter(h -> !h.isCompatible()).forEach(header -> {
          LOGGER.warning(String.format("%s:%s not compatible with %s", new Object[] { header
                  
                  .getClass().getName(), 
                  Integer.valueOf(header.getSupportedHeaderVersion()), 
                  Integer.valueOf(1) }));
          new AdministrativeError(header
              .getClass().getName(), "Incompatible Header", 
              
              String.format("The plugin %s is attempting to replace the Jenkins header but is not compatible with this version of Jenkins. The plugin should be updated or removed.", new Object[] { Jenkins.get().getPluginManager().whichPlugin(header.getClass()) }), null);
        });
  }
  
  public abstract int getSupportedHeaderVersion();
}
