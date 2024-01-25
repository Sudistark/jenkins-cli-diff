package hudson.cli.declarative;

import hudson.cli.CLICommand;
import hudson.util.ReflectionUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

class MethodBinder {
  private final CLICommand command;
  
  private final Method method;
  
  private final Object[] arguments;
  
  MethodBinder(Method method, CLICommand command, CmdLineParser parser) {
    this.method = method;
    this.command = command;
    List<ReflectionUtils.Parameter> params = ReflectionUtils.getParameters(method);
    this.arguments = new Object[params.size()];
    int bias = parser.getArguments().size();
    for (ReflectionUtils.Parameter p : params) {
      int index = p.index();
      Object object = new Object(this, index, p);
      Option option = (Option)p.annotation(Option.class);
      if (option != null)
        parser.addOption(object, option); 
      Argument arg = (Argument)p.annotation(Argument.class);
      if (arg != null) {
        ArgumentImpl argumentImpl;
        if (bias > 0)
          argumentImpl = new ArgumentImpl(arg, bias); 
        parser.addArgument(object, argumentImpl);
      } 
      if (p.type() == CLICommand.class)
        this.arguments[index] = command; 
      if (p.type().isPrimitive())
        this.arguments[index] = ReflectionUtils.getVmDefaultValueForPrimitiveType(p.type()); 
    } 
  }
  
  public Object call(Object instance) throws Exception {
    try {
      return this.method.invoke(instance, this.arguments);
    } catch (InvocationTargetException e) {
      Throwable t = e.getTargetException();
      if (t instanceof Exception)
        throw (Exception)t; 
      throw e;
    } 
  }
}
