package hudson.model;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;

public abstract class Build<P extends Project<P, B>, B extends Build<P, B>> extends AbstractBuild<P, B> {
  protected Build(P project) throws IOException { super(project); }
  
  protected Build(P job, Calendar timestamp) { super(job, timestamp); }
  
  protected Build(P project, File buildDir) throws IOException { super(project, buildDir); }
  
  public void run() { execute(createRunner()); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  protected Run<P, B>.Runner createRunner() { return new BuildExecution(this); }
  
  private static final Logger LOGGER = Logger.getLogger(Build.class.getName());
}
