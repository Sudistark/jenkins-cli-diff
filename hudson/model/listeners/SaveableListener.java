package hudson.model.listeners;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.XmlFile;
import hudson.model.Saveable;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SaveableListener implements ExtensionPoint {
  public void onChange(Saveable o, XmlFile file) {}
  
  @Deprecated
  public void register() { all().add(this); }
  
  public void unregister() { all().remove(this); }
  
  public static void fireOnChange(Saveable o, XmlFile file) {
    for (SaveableListener l : all()) {
      try {
        l.onChange(o, file);
      } catch (Throwable t) {
        Logger.getLogger(SaveableListener.class.getName()).log(Level.WARNING, null, t);
      } 
    } 
  }
  
  public static ExtensionList<SaveableListener> all() { return ExtensionList.lookup(SaveableListener.class); }
}
