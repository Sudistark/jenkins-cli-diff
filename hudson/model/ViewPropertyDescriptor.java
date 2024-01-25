package hudson.model;

public abstract class ViewPropertyDescriptor extends Descriptor<ViewProperty> {
  protected ViewPropertyDescriptor(Class<? extends ViewProperty> clazz) { super(clazz); }
  
  protected ViewPropertyDescriptor() {}
  
  public ViewProperty newInstance(View view) { return null; }
  
  public boolean isEnabledFor(View view) { return true; }
}
