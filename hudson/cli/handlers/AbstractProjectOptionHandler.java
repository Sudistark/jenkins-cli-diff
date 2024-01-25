package hudson.cli.handlers;

import hudson.model.AbstractProject;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Setter;

public class AbstractProjectOptionHandler extends GenericItemOptionHandler<AbstractProject> {
  public AbstractProjectOptionHandler(CmdLineParser parser, OptionDef option, Setter<AbstractProject> setter) { super(parser, option, setter); }
  
  protected Class<AbstractProject> type() { return AbstractProject.class; }
  
  public String getDefaultMetaVariable() { return "JOB"; }
}
