package hudson.scm;

import hudson.model.Run;
import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;

public class NullChangeLogParser extends ChangeLogParser {
  public static final NullChangeLogParser INSTANCE = new NullChangeLogParser();
  
  public ChangeLogSet<? extends ChangeLogSet.Entry> parse(Run build, RepositoryBrowser<?> browser, File changelogFile) throws IOException, SAXException { return ChangeLogSet.createEmpty(build); }
  
  protected Object readResolve() { return INSTANCE; }
}
