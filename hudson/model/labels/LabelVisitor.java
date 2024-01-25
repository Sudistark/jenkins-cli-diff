package hudson.model.labels;

public abstract class LabelVisitor<V, P> extends Object {
  public abstract V onAtom(LabelAtom paramLabelAtom, P paramP);
  
  public abstract V onParen(LabelExpression.Paren paramParen, P paramP);
  
  public abstract V onNot(LabelExpression.Not paramNot, P paramP);
  
  public abstract V onAnd(LabelExpression.And paramAnd, P paramP);
  
  public abstract V onOr(LabelExpression.Or paramOr, P paramP);
  
  public abstract V onIff(LabelExpression.Iff paramIff, P paramP);
  
  public abstract V onImplies(LabelExpression.Implies paramImplies, P paramP);
}
