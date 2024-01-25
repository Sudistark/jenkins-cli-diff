package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.Map;

public abstract class BuildVariableContributor implements ExtensionPoint {
  public abstract void buildVariablesFor(AbstractBuild paramAbstractBuild, Map<String, String> paramMap);
  
  public static ExtensionList<BuildVariableContributor> all() { return ExtensionList.lookup(BuildVariableContributor.class); }
}
