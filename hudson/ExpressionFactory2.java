package hudson;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jexl.ExpressionFactory;

final class ExpressionFactory2 implements ExpressionFactory {
  public Expression createExpression(String text) throws JellyException {
    try {
      return new JexlExpression(
          ExpressionFactory.createExpression(text));
    } catch (Exception e) {
      throw new JellyException("Unable to create expression: " + text, e);
    } 
  }
  
  protected static final ThreadLocal<JellyContext> CURRENT_CONTEXT = new ThreadLocal();
}
