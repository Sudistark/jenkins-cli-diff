package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.cli.CLICommand;
import java.io.IOException;
import org.kohsuke.stapler.StaplerRequest;

public abstract class SimpleParameterDefinition extends ParameterDefinition {
  protected SimpleParameterDefinition(@NonNull String name) { super(name); }
  
  @Deprecated
  protected SimpleParameterDefinition(@NonNull String name, @CheckForNull String description) { super(name, description); }
  
  public abstract ParameterValue createValue(String paramString);
  
  public final ParameterValue createValue(StaplerRequest req) {
    String[] value = req.getParameterValues(getName());
    if (value == null)
      return getDefaultParameterValue(); 
    if (value.length != 1)
      throw new IllegalArgumentException("Illegal number of parameter values for " + getName() + ": " + value.length); 
    return createValue(value[0]);
  }
  
  public final ParameterValue createValue(CLICommand command, String value) throws IOException, InterruptedException { return createValue(value); }
}
