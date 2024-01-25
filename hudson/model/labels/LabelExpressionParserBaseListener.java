package hudson.model.labels;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class LabelExpressionParserBaseListener implements LabelExpressionParserListener {
  public void enterExpr(LabelExpressionParser.ExprContext ctx) {}
  
  public void exitExpr(LabelExpressionParser.ExprContext ctx) {}
  
  public void enterTerm1(LabelExpressionParser.Term1Context ctx) {}
  
  public void exitTerm1(LabelExpressionParser.Term1Context ctx) {}
  
  public void enterTerm2(LabelExpressionParser.Term2Context ctx) {}
  
  public void exitTerm2(LabelExpressionParser.Term2Context ctx) {}
  
  public void enterTerm3(LabelExpressionParser.Term3Context ctx) {}
  
  public void exitTerm3(LabelExpressionParser.Term3Context ctx) {}
  
  public void enterTerm4(LabelExpressionParser.Term4Context ctx) {}
  
  public void exitTerm4(LabelExpressionParser.Term4Context ctx) {}
  
  public void enterTerm5(LabelExpressionParser.Term5Context ctx) {}
  
  public void exitTerm5(LabelExpressionParser.Term5Context ctx) {}
  
  public void enterTerm6(LabelExpressionParser.Term6Context ctx) {}
  
  public void exitTerm6(LabelExpressionParser.Term6Context ctx) {}
  
  public void enterEveryRule(ParserRuleContext ctx) {}
  
  public void exitEveryRule(ParserRuleContext ctx) {}
  
  public void visitTerminal(TerminalNode node) {}
  
  public void visitErrorNode(ErrorNode node) {}
}
