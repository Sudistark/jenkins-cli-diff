package hudson.util;

import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;

public class FormApply {
  public static HttpResponses.HttpResponseException success(String destination) { return new Object(destination); }
  
  public static boolean isApply(StaplerRequest req) { return Boolean.parseBoolean(req.getParameter("core:apply")); }
  
  public static HttpResponses.HttpResponseException applyResponse(String script) { return new Object(script); }
}
