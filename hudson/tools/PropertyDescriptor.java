package hudson.tools;

import hudson.Functions;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class PropertyDescriptor<P extends Describable<P>, T> extends Descriptor<P> {
  protected PropertyDescriptor(Class<? extends P> clazz) { super(clazz); }
  
  protected PropertyDescriptor() {}
  
  private Class<P> getP() { return Functions.getTypeParameter(getClass(), Descriptor.class, 0); }
  
  public boolean isApplicable(Class<? extends T> targetType) {
    Class<? extends T> applicable = Functions.getTypeParameter(this.clazz, getP(), 0);
    return applicable.isAssignableFrom(targetType);
  }
  
  public static <D extends PropertyDescriptor<?, T>, T> List<D> for_(List<D> all, Class<? extends T> target) {
    List<D> result = new ArrayList<D>();
    for (Iterator iterator = all.iterator(); iterator.hasNext(); ) {
      D d = (D)(PropertyDescriptor)iterator.next();
      if (d.isApplicable(target))
        result.add(d); 
    } 
    return result;
  }
  
  public static <D extends PropertyDescriptor<?, T>, T> List<D> for_(List<D> all, T target) { return for_(all, target.getClass()); }
}
