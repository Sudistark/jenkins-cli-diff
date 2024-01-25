package hudson.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.RestrictedSince;
import java.awt.Font;
import java.io.IOException;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.category.CategoryDataset;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class ChartUtil {
  @Deprecated
  public static boolean awtProblem = false;
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_REFACTORED_TO_BE_FINAL"}, justification = "It's actually being widely used by plugins. Obsolete approach, should be ideally replaced by Getter")
  public static Throwable awtProblemCause = null;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.301")
  public static final double CHEBYSHEV_N = 3.0D;
  
  @Deprecated
  public static void generateGraph(StaplerRequest req, StaplerResponse rsp, JFreeChart chart, Area defaultSize) throws IOException { generateGraph(req, rsp, chart, defaultSize.width, defaultSize.height); }
  
  @Deprecated
  public static void generateGraph(StaplerRequest req, StaplerResponse rsp, JFreeChart chart, int defaultW, int defaultH) throws IOException {
    (new Object(-1L, defaultW, defaultH, chart))



      
      .doPng(req, rsp);
  }
  
  @Deprecated
  public static void generateClickableMap(StaplerRequest req, StaplerResponse rsp, JFreeChart chart, Area defaultSize) throws IOException { generateClickableMap(req, rsp, chart, defaultSize.width, defaultSize.height); }
  
  @Deprecated
  public static void generateClickableMap(StaplerRequest req, StaplerResponse rsp, JFreeChart chart, int defaultW, int defaultH) throws IOException {
    (new Object(-1L, defaultW, defaultH, chart))



      
      .doMap(req, rsp);
  }
  
  public static void adjustChebyshev(CategoryDataset dataset, NumberAxis yAxis) {
    double sum = 0.0D, sum2 = 0.0D;
    int nColumns = dataset.getColumnCount();
    int nRows = dataset.getRowCount();
    for (int i = 0; i < nRows; i++) {
      Comparable rowKey = dataset.getRowKey(i);
      for (int j = 0; j < nColumns; j++) {
        Comparable columnKey = dataset.getColumnKey(j);
        double n = dataset.getValue(rowKey, columnKey).doubleValue();
        sum += n;
        sum2 += n * n;
      } 
    } 
    double average = sum / (nColumns * nRows);
    double stddev = Math.sqrt(sum2 / (nColumns * nRows) - average * average);
    double rangeMin = average - stddev * 3.0D;
    double rangeMax = average + stddev * 3.0D;
    boolean found = false;
    double min = 0.0D, max = 0.0D;
    for (int i = 0; i < nRows; i++) {
      Comparable rowKey = dataset.getRowKey(i);
      for (int j = 0; j < nColumns; j++) {
        Comparable columnKey = dataset.getColumnKey(j);
        double n = dataset.getValue(rowKey, columnKey).doubleValue();
        if (n < rangeMin || rangeMax < n) {
          found = true;
        } else {
          min = Math.min(min, n);
          max = Math.max(max, n);
        } 
      } 
    } 
    if (!found)
      return; 
    min = Math.min(0.0D, min);
    max += yAxis.getUpperMargin() * (max - min);
    yAxis.setRange(min, max);
  }
  
  static  {
    try {
      (new Font("SansSerif", 1, 18)).toString();
    } catch (Throwable t) {
      awtProblemCause = t;
      awtProblem = true;
    } 
  }
}
