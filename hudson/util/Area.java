package hudson.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Area {
  public final int width;
  
  public final int height;
  
  public Area(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  public static Area parse(String s) {
    Matcher m = PATTERN.matcher(s);
    if (m.matches())
      return new Area(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))); 
    return null;
  }
  
  public int area() { return this.width * this.height; }
  
  public String toString() { return "" + this.width + "x" + this.width; }
  
  private static final Pattern PATTERN = Pattern.compile("(\\d+)x(\\d+)");
}
