package hudson.cli.handlers;

import hudson.model.Node;
import jenkins.model.Jenkins;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

public class NodeOptionHandler extends OptionHandler<Node> {
  public NodeOptionHandler(CmdLineParser parser, OptionDef option, Setter<Node> setter) { super(parser, option, setter); }
  
  public int parseArguments(Parameters params) throws CmdLineException {
    String nodeName = params.getParameter(0);
    Node node = Jenkins.get().getNode(nodeName);
    if (node == null)
      throw new IllegalArgumentException("No such node '" + nodeName + "'"); 
    this.setter.addValue(node);
    return 1;
  }
  
  public String getDefaultMetaVariable() { return "NODE"; }
}
