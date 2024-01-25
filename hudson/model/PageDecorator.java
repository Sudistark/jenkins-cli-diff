package hudson.model;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.util.DescriptorList;
import java.util.List;
import jenkins.model.Jenkins;

public abstract class PageDecorator extends Descriptor<PageDecorator> implements ExtensionPoint, Describable<PageDecorator> {
  @Deprecated
  protected PageDecorator(Class<? extends PageDecorator> yourClass) { super(yourClass); }
  
  protected PageDecorator() { super(self()); }
  
  public final Descriptor<PageDecorator> getDescriptor() { return this; }
  
  public final String getUrl() { return "descriptor/" + this.clazz.getName(); }
  
  @Deprecated
  public static final List<PageDecorator> ALL = new DescriptorList(PageDecorator.class);
  
  public static ExtensionList<PageDecorator> all() { return Jenkins.get().getDescriptorList(PageDecorator.class); }
}
