package hudson.model;

public abstract class UserPropertyDescriptor extends Descriptor<UserProperty> {
  protected UserPropertyDescriptor(Class<? extends UserProperty> clazz) { super(clazz); }
  
  protected UserPropertyDescriptor() {}
  
  public abstract UserProperty newInstance(User paramUser);
  
  public boolean isEnabled() { return true; }
}
