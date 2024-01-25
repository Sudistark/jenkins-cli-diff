package jenkins.model;

import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.util.List;

public class SimplePageDecorator extends Descriptor<SimplePageDecorator> implements ExtensionPoint, Describable<SimplePageDecorator> {
  protected SimplePageDecorator() { super(self()); }
  
  public final Descriptor<SimplePageDecorator> getDescriptor() { return this; }
  
  public final String getUrl() { return "descriptor/" + this.clazz.getName(); }
  
  public static List<SimplePageDecorator> all() { return Jenkins.get().getDescriptorList(SimplePageDecorator.class); }
  
  public static SimplePageDecorator first() {
    decorators = all();
    return decorators.isEmpty() ? null : (SimplePageDecorator)decorators.get(0);
  }
}
