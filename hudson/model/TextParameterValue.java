package hudson.model;

import org.kohsuke.stapler.DataBoundConstructor;

public class TextParameterValue extends StringParameterValue {
  @DataBoundConstructor
  public TextParameterValue(String name, String value) { super(name, value); }
  
  public TextParameterValue(String name, String value, String description) { super(name, value, description); }
  
  public String toString() { return "(TextParameterValue) " + getName() + "='" + this.value + "'"; }
}
