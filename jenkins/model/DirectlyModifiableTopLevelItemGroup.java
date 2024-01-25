package jenkins.model;

import hudson.model.TopLevelItem;
import java.io.IOException;

public interface DirectlyModifiableTopLevelItemGroup extends ModifiableTopLevelItemGroup {
  boolean canAdd(TopLevelItem paramTopLevelItem);
  
  <I extends TopLevelItem> I add(I paramI, String paramString) throws IOException, IllegalArgumentException;
  
  void remove(TopLevelItem paramTopLevelItem) throws IOException, IllegalArgumentException;
}
