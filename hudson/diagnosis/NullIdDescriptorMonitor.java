package hudson.diagnosis;

import hudson.Extension;
import hudson.PluginWrapper;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.AdministrativeMonitor;
import hudson.model.Descriptor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"nullId"})
public class NullIdDescriptorMonitor extends AdministrativeMonitor {
  public String getDisplayName() { return Messages.NullIdDescriptorMonitor_DisplayName(); }
  
  private final List<Descriptor> problems = new ArrayList();
  
  public boolean isActivated() { return !this.problems.isEmpty(); }
  
  public List<Descriptor> getProblems() { return Collections.unmodifiableList(this.problems); }
  
  @Initializer(after = InitMilestone.EXTENSIONS_AUGMENTED)
  public void verify() {
    Jenkins h = Jenkins.get();
    for (Descriptor d : h.getExtensionList(Descriptor.class)) {
      String id;
      PluginWrapper p = h.getPluginManager().whichPlugin(d.getClass());
      try {
        id = d.getId();
      } catch (Throwable t) {
        LOGGER.log(Level.SEVERE, MessageFormat.format("Descriptor {0} from plugin {1} with display name {2} reported an exception for ID", new Object[] { d, 
                (p == null) ? "???" : p.getLongName(), d.getDisplayName() }), t);
        this.problems.add(d);
        continue;
      } 
      if (id == null) {
        LOGGER.severe(MessageFormat.format("Descriptor {0} from plugin {1} with display name {2} has null ID", new Object[] { d, 
                (p == null) ? "???" : p.getLongName(), d.getDisplayName() }));
        this.problems.add(d);
      } 
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(NullIdDescriptorMonitor.class.getName());
}
