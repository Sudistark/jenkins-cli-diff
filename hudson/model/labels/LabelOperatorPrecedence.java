package hudson.model.labels;

public static enum LabelOperatorPrecedence {
  ATOM(null),
  NOT("!"),
  AND("&&"),
  OR("||"),
  IMPLIES("->"),
  IFF("<->");
  
  public final String str;
  
  LabelOperatorPrecedence(String str) { this.str = str; }
}
