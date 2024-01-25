package hudson.scheduler;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CrontabParserBaseListener implements CrontabParserListener {
  public void enterStartRule(CrontabParser.StartRuleContext ctx) {}
  
  public void exitStartRule(CrontabParser.StartRuleContext ctx) {}
  
  public void enterExpr(CrontabParser.ExprContext ctx) {}
  
  public void exitExpr(CrontabParser.ExprContext ctx) {}
  
  public void enterTerm(CrontabParser.TermContext ctx) {}
  
  public void exitTerm(CrontabParser.TermContext ctx) {}
  
  public void enterToken(CrontabParser.TokenContext ctx) {}
  
  public void exitToken(CrontabParser.TokenContext ctx) {}
  
  public void enterEveryRule(ParserRuleContext ctx) {}
  
  public void exitEveryRule(ParserRuleContext ctx) {}
  
  public void visitTerminal(TerminalNode node) {}
  
  public void visitErrorNode(ErrorNode node) {}
}
