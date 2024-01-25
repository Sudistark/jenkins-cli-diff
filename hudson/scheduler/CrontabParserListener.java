package hudson.scheduler;

import org.antlr.v4.runtime.tree.ParseTreeListener;

public interface CrontabParserListener extends ParseTreeListener {
  void enterStartRule(CrontabParser.StartRuleContext paramStartRuleContext);
  
  void exitStartRule(CrontabParser.StartRuleContext paramStartRuleContext);
  
  void enterExpr(CrontabParser.ExprContext paramExprContext);
  
  void exitExpr(CrontabParser.ExprContext paramExprContext);
  
  void enterTerm(CrontabParser.TermContext paramTermContext);
  
  void exitTerm(CrontabParser.TermContext paramTermContext);
  
  void enterToken(CrontabParser.TokenContext paramTokenContext);
  
  void exitToken(CrontabParser.TokenContext paramTokenContext);
}
