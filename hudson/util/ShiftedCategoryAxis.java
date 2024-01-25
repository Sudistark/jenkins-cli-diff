package hudson.util;

import java.awt.geom.Rectangle2D;
import org.jfree.ui.RectangleEdge;

public final class ShiftedCategoryAxis extends NoOverlapCategoryAxis {
  public ShiftedCategoryAxis(String label) { super(label); }
  
  protected double calculateCategorySize(int categoryCount, Rectangle2D area, RectangleEdge edge) { return super.calculateCategorySize(categoryCount - 1, area, edge); }
  
  public double getCategoryEnd(int category, int categoryCount, Rectangle2D area, RectangleEdge edge) { return super.getCategoryStart(category, categoryCount, area, edge) + 
      calculateCategorySize(categoryCount, area, edge) / 2.0D; }
  
  public double getCategoryMiddle(int category, int categoryCount, Rectangle2D area, RectangleEdge edge) { return super.getCategoryStart(category, categoryCount, area, edge); }
  
  public double getCategoryStart(int category, int categoryCount, Rectangle2D area, RectangleEdge edge) { return super.getCategoryStart(category, categoryCount, area, edge) - 
      calculateCategorySize(categoryCount, area, edge) / 2.0D; }
}
