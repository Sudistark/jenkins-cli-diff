package jenkins.model;

import hudson.model.ModifiableItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import java.io.IOException;
import java.io.InputStream;

public interface ModifiableTopLevelItemGroup extends ModifiableItemGroup<TopLevelItem> {
  <T extends TopLevelItem> T copy(T paramT, String paramString) throws IOException;
  
  TopLevelItem createProjectFromXML(String paramString, InputStream paramInputStream) throws IOException;
  
  TopLevelItem createProject(TopLevelItemDescriptor paramTopLevelItemDescriptor, String paramString, boolean paramBoolean) throws IOException;
}
