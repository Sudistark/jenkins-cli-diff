package hudson.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.util.List;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

public class ColorPalette {
  public static final Color RED = new Color(204, 0, 3);
  
  public static final Color YELLOW = new Color(255, 152, 0);
  
  public static final Color BLUE = new Color(19, 131, 71);
  
  public static final Color GREY = new Color(150, 150, 150);
  
  public static final Color DARK_GREY = new Color(51, 51, 51);
  
  public static final Color LIGHT_GREY = new Color(204, 204, 204);
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "used in several plugins")
  public static List<Color> LINE_GRAPH = List.of(new Color(13369344), new Color(3433892), new Color(7590422), new Color(15586304));
  
  public static void apply(LineAndShapeRenderer renderer) {
    int n = 0;
    for (Color c : LINE_GRAPH)
      renderer.setSeriesPaint(n++, c); 
  }
}
