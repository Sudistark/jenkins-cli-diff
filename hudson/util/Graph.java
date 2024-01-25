package hudson.util;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import jenkins.util.SystemProperties;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public abstract class Graph {
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  static int MAX_AREA = SystemProperties.getInteger(Graph.class.getName() + ".maxArea", Integer.valueOf(10000000)).intValue();
  
  private final long timestamp;
  
  private final int defaultWidth;
  
  private final int defaultHeight;
  
  private final int defaultScale = 1;
  
  protected Graph(long timestamp, int defaultWidth, int defaultHeight) {
    this.defaultScale = 1;
    this.timestamp = timestamp;
    this.defaultWidth = defaultWidth;
    this.defaultHeight = defaultHeight;
  }
  
  protected Graph(Calendar timestamp, int defaultWidth, int defaultHeight) { this(timestamp.getTimeInMillis(), defaultWidth, defaultHeight); }
  
  protected abstract JFreeChart createGraph();
  
  private BufferedImage render(StaplerRequest req, ChartRenderingInfo info) {
    String w = req.getParameter("width");
    if (w == null)
      w = String.valueOf(this.defaultWidth); 
    String h = req.getParameter("height");
    if (h == null)
      h = String.valueOf(this.defaultHeight); 
    String s = req.getParameter("scale");
    if (s == null)
      s = String.valueOf(1); 
    Color graphBg = stringToColor(req.getParameter("graphBg"));
    Color plotBg = stringToColor(req.getParameter("plotBg"));
    if (this.graph == null)
      this.graph = createGraph(); 
    this.graph.setBackgroundPaint(graphBg);
    Plot p = this.graph.getPlot();
    p.setBackgroundPaint(plotBg);
    int width = Math.min(Integer.parseInt(w), 2560);
    int height = Math.min(Integer.parseInt(h), 1440);
    int scale = Math.min(Integer.parseInt(s), 3);
    Dimension safeDimension = safeDimension(width, height, this.defaultWidth, this.defaultHeight);
    return this.graph.createBufferedImage(safeDimension.width * scale, safeDimension.height * scale, safeDimension.width, safeDimension.height, info);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @VisibleForTesting
  public static Dimension safeDimension(int width, int height, int defaultWidth, int defaultHeight) {
    if (width <= 0 || height <= 0 || width > MAX_AREA / height) {
      width = defaultWidth;
      height = defaultHeight;
    } 
    return new Dimension(width, height);
  }
  
  @NonNull
  private static Color stringToColor(@CheckForNull String s) {
    if (s != null)
      try {
        return Color.decode("0x" + s);
      } catch (NumberFormatException e) {
        return Color.WHITE;
      }  
    return Color.WHITE;
  }
  
  public void doPng(StaplerRequest req, StaplerResponse rsp) throws IOException {
    if (req.checkIfModified(this.timestamp, rsp))
      return; 
    try {
      BufferedImage image = render(req, null);
      rsp.setContentType("image/png");
      ServletOutputStream os = rsp.getOutputStream();
      ImageIO.write(image, "PNG", os);
      os.close();
    } catch (Error e) {
      if (e.getMessage().contains("Probable fatal error:No fonts found")) {
        rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
        return;
      } 
      throw e;
    } catch (HeadlessException e) {
      rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
    } 
  }
  
  public void doMap(StaplerRequest req, StaplerResponse rsp) throws IOException {
    if (req.checkIfModified(this.timestamp, rsp))
      return; 
    ChartRenderingInfo info = new ChartRenderingInfo();
    render(req, info);
    rsp.setContentType("text/plain;charset=UTF-8");
    rsp.getWriter().println(ChartUtilities.getImageMap("map", info));
  }
}
