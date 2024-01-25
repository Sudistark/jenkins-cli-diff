package hudson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkupText extends AbstractMarkupText {
  private final String text;
  
  private final List<Tag> tags;
  
  public MarkupText(String text) {
    this.tags = new ArrayList();
    this.text = text;
  }
  
  public String getText() { return this.text; }
  
  public SubText subText(int start, int end) { return new SubText(this, start, (end < 0) ? (this.text.length() + 1 + end) : end); }
  
  public void addMarkup(int startPos, int endPos, String startTag, String endTag) {
    rangeCheck(startPos);
    rangeCheck(endPos);
    if (startPos > endPos)
      throw new IndexOutOfBoundsException(); 
    this.tags.add(new Tag(startPos, startTag));
    this.tags.add(0, new Tag(endPos, endTag));
  }
  
  public void addMarkup(int pos, String tag) {
    rangeCheck(pos);
    this.tags.add(new Tag(pos, tag));
  }
  
  private void rangeCheck(int pos) {
    if (pos < 0 || pos > this.text.length())
      throw new IndexOutOfBoundsException(); 
  }
  
  @Deprecated
  public String toString() { return toString(false); }
  
  public String toString(boolean preEscape) {
    if (this.tags.isEmpty())
      return preEscape ? Util.xmlEscape(this.text) : Util.escape(this.text); 
    Collections.sort(this.tags);
    StringBuilder buf = new StringBuilder();
    int copied = 0;
    for (Tag tag : this.tags) {
      if (copied < tag.pos) {
        String portion = this.text.substring(copied, tag.pos);
        buf.append(preEscape ? Util.xmlEscape(portion) : Util.escape(portion));
        copied = tag.pos;
      } 
      buf.append(tag.markup);
    } 
    if (copied < this.text.length()) {
      String portion = this.text.substring(copied);
      buf.append(preEscape ? Util.xmlEscape(portion) : Util.escape(portion));
    } 
    return buf.toString();
  }
  
  public List<SubText> findTokens(Pattern pattern) { return super.findTokens(pattern); }
  
  protected SubText createSubText(Matcher m) { return new SubText(this, m, 0); }
}
