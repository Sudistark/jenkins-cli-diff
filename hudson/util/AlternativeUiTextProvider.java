package hudson.util;

import hudson.ExtensionList;
import hudson.ExtensionPoint;

public abstract class AlternativeUiTextProvider implements ExtensionPoint {
  public abstract <T> String getText(Message<T> paramMessage, T paramT);
  
  public static ExtensionList<AlternativeUiTextProvider> all() { return ExtensionList.lookup(AlternativeUiTextProvider.class); }
  
  public static <T> String get(Message<T> text, T context, String defaultValue) {
    String s = get(text, context);
    return (s != null) ? s : defaultValue;
  }
  
  public static <T> String get(Message<T> text, T context) {
    for (AlternativeUiTextProvider p : all()) {
      String s = p.getText(text, context);
      if (s != null)
        return s; 
    } 
    return null;
  }
}
