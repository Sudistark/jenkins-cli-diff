package hudson.tasks._maven;

import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;
import java.util.regex.Pattern;

public class MavenWarningNote extends ConsoleNote {
  public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
    text.addMarkup(0, text.length(), "<span class=warning-inline>", "</span>");
    return null;
  }
  
  public static final Pattern PATTERN = Pattern.compile("^\\[WARNING\\]");
}
