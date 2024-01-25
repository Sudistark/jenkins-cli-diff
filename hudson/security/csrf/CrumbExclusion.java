package hudson.security.csrf;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class CrumbExclusion implements ExtensionPoint {
  public abstract boolean process(HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse, FilterChain paramFilterChain) throws IOException, ServletException;
  
  public static ExtensionList<CrumbExclusion> all() { return ExtensionList.lookup(CrumbExclusion.class); }
}
