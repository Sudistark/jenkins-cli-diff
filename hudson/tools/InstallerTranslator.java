package hudson.tools;

import hudson.Extension;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Semaphore;

@Extension
public class InstallerTranslator extends ToolLocationTranslator {
  private static final Map<Node, Map<ToolInstallation, Semaphore>> mutexByNode = new WeakHashMap();
  
  public String getToolHome(Node node, ToolInstallation tool, TaskListener log) throws IOException, InterruptedException {
    if (node.getRootPath() == null) {
      log.error(node.getDisplayName() + " is offline; cannot locate " + node.getDisplayName());
      return null;
    } 
    InstallSourceProperty isp = (InstallSourceProperty)tool.getProperties().get(InstallSourceProperty.class);
    if (isp == null)
      return null; 
    ArrayList<String> inapplicableInstallersMessages = new ArrayList<String>();
    for (ToolInstaller installer : isp.installers) {
      if (installer.appliesTo(node)) {
        synchronized (mutexByNode) {
          Map<ToolInstallation, Semaphore> mutexByTool = (Map)mutexByNode.computeIfAbsent(node, k -> new WeakHashMap());
          semaphore = (Semaphore)mutexByTool.get(tool);
          if (semaphore == null)
            mutexByTool.put(tool, semaphore = new Semaphore(1)); 
        } 
        semaphore.acquire();
        try {
          return installer.performInstallation(tool, node, log).getRemote();
        } finally {
          semaphore.release();
        } 
      } 
      inapplicableInstallersMessages.add(Messages.CannotBeInstalled(installer
            .getDescriptor().getDisplayName(), tool
            .getName(), node
            .getDisplayName()));
    } 
    for (String message : inapplicableInstallersMessages)
      log.getLogger().println(message); 
    return null;
  }
}
