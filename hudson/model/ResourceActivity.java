package hudson.model;

public interface ResourceActivity {
  default ResourceList getResourceList() { return ResourceList.EMPTY; }
  
  String getDisplayName();
}
