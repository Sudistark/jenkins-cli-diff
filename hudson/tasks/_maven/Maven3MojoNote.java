package hudson.tasks._maven;

import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;
import java.util.regex.Pattern;

public class Maven3MojoNote extends ConsoleNote {
  public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
    text.addMarkup(7, text.length(), "<b class=maven-mojo>", "</b>");
    return null;
  }
  
  public static final Pattern PATTERN = Pattern.compile("\\[INFO\\] --- .+-plugin:[^:]+:[^ ]+ \\(.+\\) @ .+ ---");
}
