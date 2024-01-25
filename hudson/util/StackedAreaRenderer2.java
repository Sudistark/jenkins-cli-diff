package hudson.util;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;

public class StackedAreaRenderer2 extends StackedAreaRenderer implements CategoryToolTipGenerator, CategoryURLGenerator {
  public StackedAreaRenderer2() {
    setEndType(AreaRendererEndType.TRUNCATE);
    setItemURLGenerator(this);
    setToolTipGenerator(this);
  }
  
  public String generateURL(CategoryDataset dataset, int row, int column) { return null; }
  
  public String generateToolTip(CategoryDataset dataset, int row, int column) { return null; }
  
  public Paint getItemPaint(int row, int column) { return super.getItemPaint(row, column); }
  
  public void drawItem(Graphics2D g2, CategoryItemRendererState state, Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis, ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, int pass) {
    Number dataValue = dataset.getValue(row, column);
    if (dataValue == null)
      return; 
    double value = dataValue.doubleValue();
    double xx1 = domainAxis.getCategoryMiddle(column, getColumnCount(), dataArea, plot
        .getDomainAxisEdge());
    double previousHeightx1 = getPreviousHeight(dataset, row, column);
    double y1 = value + previousHeightx1;
    RectangleEdge location = plot.getRangeAxisEdge();
    double yy1 = rangeAxis.valueToJava2D(y1, dataArea, location);
    g2.setPaint(getItemPaint(row, column));
    g2.setStroke(getItemStroke(row, column));
    EntityCollection entities = state.getEntityCollection();
    if (column == 0) {
      if (pass == 1)
        if (isItemLabelVisible(row, column))
          drawItemLabel(g2, plot.getOrientation(), dataset, row, column, xx1, yy1, (y1 < 0.0D));  
    } else {
      Number previousValue = dataset.getValue(row, column - 1);
      if (previousValue != null) {
        double xx0 = domainAxis.getCategoryMiddle(column - 1, 
            getColumnCount(), dataArea, plot.getDomainAxisEdge());
        double y0 = previousValue.doubleValue();
        double previousHeightx0 = getPreviousHeight(dataset, row, column - 1);
        y0 += previousHeightx0;
        double previousHeightxx0 = rangeAxis.valueToJava2D(previousHeightx0, dataArea, location);
        double previousHeightxx1 = rangeAxis.valueToJava2D(previousHeightx1, dataArea, location);
        double yy0 = rangeAxis.valueToJava2D(y0, dataArea, location);
        if (pass == 0) {
          Polygon p = new Polygon();
          p.addPoint((int)xx0, (int)yy0);
          p.addPoint((int)(xx0 + xx1) / 2, (int)(yy0 + yy1) / 2);
          p.addPoint((int)(xx0 + xx1) / 2, (int)(previousHeightxx0 + previousHeightxx1) / 2);
          p.addPoint((int)xx0, (int)previousHeightxx0);
          g2.setPaint(getItemPaint(row, column - 1));
          g2.setStroke(getItemStroke(row, column - 1));
          g2.fill(p);
          if (entities != null)
            addItemEntity(entities, dataset, row, column - 1, p); 
          p = new Polygon();
          p.addPoint((int)xx1, (int)yy1);
          p.addPoint((int)(xx0 + xx1) / 2, (int)(yy0 + yy1) / 2);
          p.addPoint((int)(xx0 + xx1) / 2, (int)(previousHeightxx0 + previousHeightxx1) / 2);
          p.addPoint((int)xx1, (int)previousHeightxx1);
          g2.setPaint(getItemPaint(row, column));
          g2.setStroke(getItemStroke(row, column));
          g2.fill(p);
          if (entities != null)
            addItemEntity(entities, dataset, row, column, p); 
        } else if (isItemLabelVisible(row, column)) {
          drawItemLabel(g2, plot.getOrientation(), dataset, row, column, xx1, yy1, (y1 < 0.0D));
        } 
      } 
    } 
  }
}
