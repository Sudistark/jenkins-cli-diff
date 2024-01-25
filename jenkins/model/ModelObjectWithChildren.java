package jenkins.model;

import hudson.model.ModelObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public interface ModelObjectWithChildren extends ModelObject {
  ModelObjectWithContextMenu.ContextMenu doChildrenContextMenu(StaplerRequest paramStaplerRequest, StaplerResponse paramStaplerResponse) throws Exception;
}
