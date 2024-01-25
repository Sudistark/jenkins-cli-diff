package hudson.scm;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;

public abstract class ChangeLogParser {
  public ChangeLogSet<? extends ChangeLogSet.Entry> parse(Run build, RepositoryBrowser<?> browser, File changelogFile) throws IOException, SAXException {
    if (build instanceof AbstractBuild && Util.isOverridden(ChangeLogParser.class, getClass(), "parse", new Class[] { AbstractBuild.class, File.class }))
      return parse((AbstractBuild)build, changelogFile); 
    throw new AbstractMethodError("You must override the newer overload of parse");
  }
  
  @Deprecated
  public ChangeLogSet<? extends ChangeLogSet.Entry> parse(AbstractBuild build, File changelogFile) throws IOException, SAXException { return parse(build, build.getProject().getScm().getEffectiveBrowser(), changelogFile); }
}
