package hudson.tools;

public abstract class ToolPropertyDescriptor extends PropertyDescriptor<ToolProperty<?>, ToolInstallation> {
  protected ToolPropertyDescriptor(Class<? extends ToolProperty<?>> clazz) { super(clazz); }
  
  protected ToolPropertyDescriptor() {}
}
