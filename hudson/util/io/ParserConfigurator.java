package hudson.util.io;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.remoting.Channel;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.dom4j.io.SAXReader;

@Deprecated
public abstract class ParserConfigurator implements ExtensionPoint, Serializable {
  private static final long serialVersionUID = -2523542286453177108L;
  
  public void configure(SAXReader reader, Object context) {}
  
  public static ExtensionList<ParserConfigurator> all() { return ExtensionList.lookup(ParserConfigurator.class); }
  
  public static void applyConfiguration(SAXReader reader, Object context) {
    ExtensionList extensionList = Collections.emptyList();
    if (Jenkins.getInstanceOrNull() == null) {
      Channel ch = Channel.current();
      if (ch != null)
        extensionList = (Collection)ch.call(new GetParserConfigurators()); 
    } else {
      extensionList = all();
    } 
    for (ParserConfigurator pc : extensionList)
      pc.configure(reader, context); 
  }
}
