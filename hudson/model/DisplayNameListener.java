package hudson.model;

import hudson.Extension;
import hudson.model.listeners.ItemListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class DisplayNameListener extends ItemListener {
  private static final Logger LOGGER = Logger.getLogger(DisplayNameListener.class.getName());
  
  public void onCopied(Item src, Item item) {
    if (item instanceof AbstractItem && src.getParent() == item.getParent()) {
      AbstractItem dest = (AbstractItem)item;
      try {
        dest.setDisplayName(null);
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, String.format("onCopied():Exception while trying to clear the displayName for Item.name:%s", new Object[] { item.getName() }), ioe);
      } 
    } 
  }
  
  public void onRenamed(Item item, String oldName, String newName) {
    if (item instanceof AbstractItem) {
      AbstractItem abstractItem = (AbstractItem)item;
      if (oldName.equals(abstractItem.getDisplayName()))
        try {
          LOGGER.info(String.format("onRenamed():Setting displayname to null for item.name=%s", new Object[] { item.getName() }));
          abstractItem.setDisplayName(null);
        } catch (IOException ioe) {
          LOGGER.log(Level.WARNING, String.format("onRenamed():Exception while trying to clear the displayName for Item.name:%s", new Object[] { item
                  .getName() }), ioe);
        }  
    } 
  }
}
