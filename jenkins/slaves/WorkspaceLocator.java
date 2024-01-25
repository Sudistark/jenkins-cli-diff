package jenkins.slaves;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.TopLevelItem;

public abstract class WorkspaceLocator implements ExtensionPoint {
  public abstract FilePath locate(TopLevelItem paramTopLevelItem, Node paramNode);
  
  public static ExtensionList<WorkspaceLocator> all() { return ExtensionList.lookup(WorkspaceLocator.class); }
}
