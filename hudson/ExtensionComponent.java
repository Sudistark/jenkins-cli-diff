package hudson;

import hudson.model.Describable;
import hudson.model.Descriptor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtensionComponent<T> extends Object implements Comparable<ExtensionComponent<T>> {
  private static final Logger LOG = Logger.getLogger(ExtensionComponent.class.getName());
  
  private final T instance;
  
  private final double ordinal;
  
  public ExtensionComponent(T instance, double ordinal) {
    this.instance = instance;
    this.ordinal = ordinal;
  }
  
  public ExtensionComponent(T instance, Extension annotation) { this(instance, annotation.ordinal()); }
  
  public ExtensionComponent(T instance) { this(instance, 0.0D); }
  
  public double ordinal() { return this.ordinal; }
  
  public T getInstance() { return (T)this.instance; }
  
  public boolean isDescriptorOf(Class<? extends Describable> c) { return (this.instance instanceof Descriptor && ((Descriptor)this.instance).isSubTypeOf(c)); }
  
  public int compareTo(ExtensionComponent<T> that) {
    double a = ordinal();
    double b = that.ordinal();
    if (Double.compare(a, b) > 0)
      return -1; 
    if (Double.compare(a, b) < 0)
      return 1; 
    boolean thisIsDescriptor = false;
    String thisLabel = this.instance.getClass().getName();
    if (this.instance instanceof Descriptor)
      try {
        thisLabel = Util.fixNull(((Descriptor)this.instance).getDisplayName());
        thisIsDescriptor = true;
      } catch (RuntimeException|LinkageError x) {
        LOG.log(Level.WARNING, "Failure during Descriptor#getDisplayName for " + this.instance.getClass().getName(), x);
      }  
    boolean thatIsDescriptor = false;
    String thatLabel = that.instance.getClass().getName();
    if (that.instance instanceof Descriptor)
      try {
        thatLabel = Util.fixNull(((Descriptor)that.instance).getDisplayName());
        thatIsDescriptor = true;
      } catch (RuntimeException|LinkageError x) {
        LOG.log(Level.WARNING, "Failure during Descriptor#getDisplayName for " + that.instance.getClass().getName(), x);
      }  
    if (thisIsDescriptor) {
      if (thatIsDescriptor)
        return thisLabel.compareTo(thatLabel); 
      return 1;
    } 
    if (thatIsDescriptor)
      return -1; 
    return thisLabel.compareTo(thatLabel);
  }
}
