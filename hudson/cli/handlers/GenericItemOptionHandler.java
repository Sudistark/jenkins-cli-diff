package hudson.cli.handlers;

import hudson.model.Item;
import hudson.model.Items;
import hudson.security.ACL;
import hudson.security.ACLContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;
import org.springframework.security.core.Authentication;

public abstract class GenericItemOptionHandler<T extends Item> extends OptionHandler<T> {
  private static final Logger LOGGER = Logger.getLogger(GenericItemOptionHandler.class.getName());
  
  protected GenericItemOptionHandler(CmdLineParser parser, OptionDef option, Setter<T> setter) { super(parser, option, setter); }
  
  protected abstract Class<T> type();
  
  public int parseArguments(Parameters params) throws CmdLineException {
    Jenkins j = Jenkins.get();
    String src = params.getParameter(0);
    T s = (T)j.getItemByFullName(src, type());
    if (s == null) {
      Authentication who = Jenkins.getAuthentication2();
      ACLContext acl = ACL.as2(ACL.SYSTEM2);
      try {
        Item actual = j.getItemByFullName(src);
        if (actual == null) {
          LOGGER.log(Level.FINE, "really no item exists named {0}", src);
        } else {
          LOGGER.log(Level.WARNING, "running as {0} could not find {1} of {2}", new Object[] { who.getPrincipal(), actual, type() });
        } 
        if (acl != null)
          acl.close(); 
      } catch (Throwable throwable) {
        if (acl != null)
          try {
            acl.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
      T nearest = (T)Items.findNearest(type(), src, j);
      if (nearest != null)
        throw new IllegalArgumentException("No such job '" + src + "'; perhaps you meant '" + nearest.getFullName() + "'?"); 
      throw new IllegalArgumentException("No such job '" + src + "'");
    } 
    this.setter.addValue(s);
    return 1;
  }
  
  public String getDefaultMetaVariable() { return "ITEM"; }
}
