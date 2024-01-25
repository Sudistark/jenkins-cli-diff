package jenkins.model;

import hudson.model.ModelObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public interface ModelObjectWithContextMenu extends ModelObject {
  ContextMenu doContextMenu(StaplerRequest paramStaplerRequest, StaplerResponse paramStaplerResponse) throws Exception;
}
