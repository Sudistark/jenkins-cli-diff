package hudson.cli.handlers;

import hudson.model.AbstractItem;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Setter;

public class AbstractItemOptionHandler extends GenericItemOptionHandler<AbstractItem> {
  public AbstractItemOptionHandler(CmdLineParser parser, OptionDef option, Setter<AbstractItem> setter) { super(parser, option, setter); }
  
  protected Class<AbstractItem> type() { return AbstractItem.class; }
}
