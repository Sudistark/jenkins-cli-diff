package hudson.widgets;

import hudson.Util;
import hudson.model.Descriptor;
import hudson.util.PackedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.framework.adjunct.AdjunctsInPage;

public class RenderOnDemandClosure {
  private final Script[] bodyStack;
  
  private final Map<String, Object> variables;
  
  private final String currentDescriptorByNameUrl;
  
  private final String[] adjuncts;
  
  public RenderOnDemandClosure(JellyContext context, String attributesToCapture) {
    List<Script> bodyStack = new ArrayList<Script>();
    for (JellyContext c = context; c != null; c = c.getParent()) {
      Script script = (Script)c.getVariables().get("org.apache.commons.jelly.body");
      if (script != null)
        bodyStack.add(script); 
    } 
    this.bodyStack = (Script[])bodyStack.toArray(new Script[0]);
    assert !bodyStack.isEmpty();
    Map<String, Object> variables = new HashMap<String, Object>();
    for (String v : Util.fixNull(attributesToCapture).split(","))
      variables.put(v.intern(), context.getVariable(v)); 
    this.currentDescriptorByNameUrl = Descriptor.getCurrentDescriptorByNameUrl();
    this.variables = PackedMap.of(variables);
    Set<String> _adjuncts = AdjunctsInPage.get().getIncluded();
    this.adjuncts = new String[_adjuncts.size()];
    int i = 0;
    for (String adjunct : _adjuncts)
      this.adjuncts[i++] = adjunct.intern(); 
  }
  
  @JavaScriptMethod
  public HttpResponse render() { return new Object(this); }
  
  private static final Logger LOGGER = Logger.getLogger(RenderOnDemandClosure.class.getName());
}
