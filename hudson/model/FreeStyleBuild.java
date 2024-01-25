package hudson.model;

import java.io.File;
import java.io.IOException;

public class FreeStyleBuild extends Build<FreeStyleProject, FreeStyleBuild> {
  public FreeStyleBuild(FreeStyleProject project) throws IOException { super(project); }
  
  public FreeStyleBuild(FreeStyleProject project, File buildDir) throws IOException { super(project, buildDir); }
  
  public void run() { execute(new Build.BuildExecution(this)); }
}
