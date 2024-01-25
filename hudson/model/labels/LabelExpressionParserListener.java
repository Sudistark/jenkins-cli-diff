package hudson.model.labels;

import org.antlr.v4.runtime.tree.ParseTreeListener;

public interface LabelExpressionParserListener extends ParseTreeListener {
  void enterExpr(LabelExpressionParser.ExprContext paramExprContext);
  
  void exitExpr(LabelExpressionParser.ExprContext paramExprContext);
  
  void enterTerm1(LabelExpressionParser.Term1Context paramTerm1Context);
  
  void exitTerm1(LabelExpressionParser.Term1Context paramTerm1Context);
  
  void enterTerm2(LabelExpressionParser.Term2Context paramTerm2Context);
  
  void exitTerm2(LabelExpressionParser.Term2Context paramTerm2Context);
  
  void enterTerm3(LabelExpressionParser.Term3Context paramTerm3Context);
  
  void exitTerm3(LabelExpressionParser.Term3Context paramTerm3Context);
  
  void enterTerm4(LabelExpressionParser.Term4Context paramTerm4Context);
  
  void exitTerm4(LabelExpressionParser.Term4Context paramTerm4Context);
  
  void enterTerm5(LabelExpressionParser.Term5Context paramTerm5Context);
  
  void exitTerm5(LabelExpressionParser.Term5Context paramTerm5Context);
  
  void enterTerm6(LabelExpressionParser.Term6Context paramTerm6Context);
  
  void exitTerm6(LabelExpressionParser.Term6Context paramTerm6Context);
}
