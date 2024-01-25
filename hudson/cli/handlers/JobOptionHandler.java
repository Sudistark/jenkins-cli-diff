package hudson.cli.handlers;

import hudson.model.Job;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.Setter;

public class JobOptionHandler extends GenericItemOptionHandler<Job> {
  public JobOptionHandler(CmdLineParser parser, OptionDef option, Setter<Job> setter) { super(parser, option, setter); }
  
  protected Class<Job> type() { return Job.class; }
  
  public String getDefaultMetaVariable() { return "JOB"; }
}
