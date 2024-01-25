package hudson.model;

import hudson.tasks.BuildWrapper;
import hudson.util.DescribableList;

public interface BuildableItemWithBuildWrappers extends BuildableItem {
  AbstractProject<?, ?> asProject();
  
  DescribableList<BuildWrapper, Descriptor<BuildWrapper>> getBuildWrappersList();
}
