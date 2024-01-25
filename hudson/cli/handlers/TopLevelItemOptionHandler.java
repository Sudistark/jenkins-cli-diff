package hudson.cli.handlers;

import hudson.model.TopLevelItem;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Setter;

public class TopLevelItemOptionHandler extends GenericItemOptionHandler<TopLevelItem> {
  public TopLevelItemOptionHandler(CmdLineParser parser, OptionDef option, Setter<TopLevelItem> setter) { super(parser, option, setter); }
  
  protected Class<TopLevelItem> type() { return TopLevelItem.class; }
  
  public String getDefaultMetaVariable() { return "JOB"; }
}
