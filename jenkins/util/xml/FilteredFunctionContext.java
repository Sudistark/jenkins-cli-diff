package jenkins.util.xml;

import java.util.Locale;
import java.util.Set;
import org.jaxen.Function;
import org.jaxen.FunctionContext;
import org.jaxen.UnresolvableException;
import org.jaxen.XPathFunctionContext;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class FilteredFunctionContext implements FunctionContext {
  private static final Set<String> DEFAULT_ILLEGAL_FUNCTIONS = Set.of("document");
  
  private final FunctionContext base;
  
  private final Set<String> illegalFunctions;
  
  public FilteredFunctionContext(Set<String> illegalFunctions) {
    this.illegalFunctions = illegalFunctions;
    this.base = XPathFunctionContext.getInstance();
  }
  
  public FilteredFunctionContext() { this(DEFAULT_ILLEGAL_FUNCTIONS); }
  
  public Function getFunction(String namespaceURI, String prefix, String localName) throws UnresolvableException {
    if (localName != null && this.illegalFunctions.contains(localName.toLowerCase(Locale.ENGLISH)))
      throw new UnresolvableException("Illegal function: " + localName); 
    return this.base.getFunction(namespaceURI, prefix, localName);
  }
}
