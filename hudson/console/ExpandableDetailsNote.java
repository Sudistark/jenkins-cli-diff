package hudson.console;

import hudson.Functions;
import hudson.MarkupText;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpandableDetailsNote extends ConsoleNote {
  private final String caption;
  
  private final String html;
  
  public ExpandableDetailsNote(String caption, String html) {
    this.caption = caption;
    this.html = html;
  }
  
  public ConsoleAnnotator annotate(Object context, MarkupText text, int charPos) {
    text.addMarkup(charPos, "<input type=button value='" + 
        Functions.htmlAttributeEscape(this.caption) + "' class='reveal-expandable-detail'><div class='expandable-detail'>" + this.html + "</div>");
    return null;
  }
  
  public static String encodeTo(String buttonCaption, String html) {
    try {
      return (new ExpandableDetailsNote(buttonCaption, html)).encode();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to serialize " + HyperlinkNote.class, e);
      return "";
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(ExpandableDetailsNote.class.getName());
}
