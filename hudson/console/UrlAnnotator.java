package hudson.console;

import hudson.Extension;
import org.jenkinsci.Symbol;

@Extension
@Symbol({"url"})
public class UrlAnnotator extends ConsoleAnnotatorFactory<Object> {
  public ConsoleAnnotator newInstance(Object context) { return new UrlConsoleAnnotator(); }
}
