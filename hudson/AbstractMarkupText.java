package hudson;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractMarkupText {
  public abstract String getText();
  
  public char charAt(int idx) { return getText().charAt(idx); }
  
  public final int length() { return getText().length(); }
  
  public abstract MarkupText.SubText subText(int paramInt1, int paramInt2);
  
  public abstract void addMarkup(int paramInt1, int paramInt2, String paramString1, String paramString2);
  
  public void addHyperlink(int startPos, int endPos, String url) { addMarkup(startPos, endPos, "<a href='" + Functions.htmlAttributeEscape(url) + "'>", "</a>"); }
  
  public void addHyperlinkLowKey(int startPos, int endPos, String url) { addMarkup(startPos, endPos, "<a class='lowkey' href='" + Functions.htmlAttributeEscape(url) + "'>", "</a>"); }
  
  public void hide(int startPos, int endPos) { addMarkup(startPos, endPos, "<span style='display:none'>", "</span>"); }
  
  public final void wrapBy(String startTag, String endTag) { addMarkup(0, length(), startTag, endTag); }
  
  public MarkupText.SubText findToken(Pattern pattern) {
    String text = getText();
    Matcher m = pattern.matcher(text);
    if (m.find())
      return createSubText(m); 
    return null;
  }
  
  public List<MarkupText.SubText> findTokens(Pattern pattern) {
    String text = getText();
    Matcher m = pattern.matcher(text);
    List<MarkupText.SubText> r = new ArrayList<MarkupText.SubText>();
    while (m.find()) {
      int idx = m.start();
      if (idx > 0) {
        char ch = text.charAt(idx - 1);
        if (Character.isLetter(ch) || Character.isDigit(ch))
          continue; 
      } 
      idx = m.end();
      if (idx < text.length()) {
        char ch = text.charAt(idx);
        if (Character.isLetter(ch) || Character.isDigit(ch))
          continue; 
      } 
      r.add(createSubText(m));
    } 
    return r;
  }
  
  protected abstract MarkupText.SubText createSubText(Matcher paramMatcher);
}
