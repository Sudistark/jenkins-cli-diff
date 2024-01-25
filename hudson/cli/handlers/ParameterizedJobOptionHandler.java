package hudson.cli.handlers;

import jenkins.model.ParameterizedJobMixIn;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Setter;

@Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
public class ParameterizedJobOptionHandler extends GenericItemOptionHandler<ParameterizedJobMixIn.ParameterizedJob> {
  public ParameterizedJobOptionHandler(CmdLineParser parser, OptionDef option, Setter<ParameterizedJobMixIn.ParameterizedJob> setter) { super(parser, option, setter); }
  
  protected Class<ParameterizedJobMixIn.ParameterizedJob> type() { return ParameterizedJobMixIn.ParameterizedJob.class; }
  
  public String getDefaultMetaVariable() { return "JOB"; }
}
