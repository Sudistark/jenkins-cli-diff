package hudson.model.listeners;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Failure;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.Items;
import hudson.security.ACL;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.util.Listeners;

public class ItemListener implements ExtensionPoint {
  private static final Logger LOGGER = Logger.getLogger(ItemListener.class.getName());
  
  public void onCreated(Item item) {}
  
  public void onCheckCopy(Item src, ItemGroup parent) throws Failure {}
  
  public void onCopied(Item src, Item item) { onCreated(item); }
  
  public void onLoaded() {}
  
  public void onDeleted(Item item) {}
  
  public void onRenamed(Item item, String oldName, String newName) {}
  
  public void onLocationChanged(Item item, String oldFullName, String newFullName) {}
  
  public void onUpdated(Item item) {}
  
  public void onBeforeShutdown() {}
  
  @Deprecated
  public void register() { all().add(this); }
  
  public static ExtensionList<ItemListener> all() { return ExtensionList.lookup(ItemListener.class); }
  
  public static void fireOnCopied(Item src, Item result) { Listeners.notify(ItemListener.class, false, l -> l.onCopied(src, result)); }
  
  public static void checkBeforeCopy(Item src, ItemGroup parent) throws Failure {
    for (ItemListener l : all()) {
      try {
        l.onCheckCopy(src, parent);
      } catch (Failure e) {
        throw e;
      } catch (RuntimeException x) {
        LOGGER.log(Level.WARNING, "failed to send event to listener of " + l.getClass(), x);
      } 
    } 
  }
  
  public static void fireOnCreated(Item item) { Listeners.notify(ItemListener.class, false, l -> l.onCreated(item)); }
  
  public static void fireOnUpdated(Item item) { Listeners.notify(ItemListener.class, false, l -> l.onUpdated(item)); }
  
  public static void fireOnDeleted(Item item) { Listeners.notify(ItemListener.class, false, l -> l.onDeleted(item)); }
  
  public static void fireLocationChange(Item rootItem, String oldFullName) {
    String prefix = rootItem.getParent().getFullName();
    if (!prefix.isEmpty())
      prefix = prefix + "/"; 
    String newFullName = rootItem.getFullName();
    assert newFullName.startsWith(prefix);
    int prefixS = prefix.length();
    if (oldFullName.startsWith(prefix) && oldFullName.indexOf('/', prefixS) == -1) {
      String oldName = oldFullName.substring(prefixS);
      String newName = rootItem.getName();
      assert newName.equals(newFullName.substring(prefixS));
      Listeners.notify(ItemListener.class, false, l -> l.onRenamed(rootItem, oldName, newName));
    } 
    Listeners.notify(ItemListener.class, false, l -> l.onLocationChanged(rootItem, oldFullName, newFullName));
    if (rootItem instanceof ItemGroup)
      for (Item child : Items.allItems2(ACL.SYSTEM2, (ItemGroup)rootItem, Item.class)) {
        String childNew = child.getFullName();
        assert childNew.startsWith(newFullName);
        assert childNew.charAt(newFullName.length()) == '/';
        String childOld = oldFullName + oldFullName;
        Listeners.notify(ItemListener.class, false, l -> l.onLocationChanged(child, childOld, childNew));
      }  
  }
}
