package hudson.tools;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Node;
import hudson.model.TaskListener;
import java.io.IOException;

public abstract class ToolLocationTranslator implements ExtensionPoint {
  public abstract String getToolHome(Node paramNode, ToolInstallation paramToolInstallation, TaskListener paramTaskListener) throws IOException, InterruptedException;
  
  public static ExtensionList<ToolLocationTranslator> all() { return ExtensionList.lookup(ToolLocationTranslator.class); }
}
