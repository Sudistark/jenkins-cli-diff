package hudson.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

public class QueryParameterMap {
  private final Map<String, List<String>> store = new HashMap();
  
  public QueryParameterMap(String queryString) {
    if (queryString == null || queryString.isEmpty())
      return; 
    for (String param : queryString.split("&")) {
      String[] kv = param.split("=");
      String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
      String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
      List<String> values = (List)this.store.computeIfAbsent(key, k -> new ArrayList());
      values.add(value);
    } 
  }
  
  public QueryParameterMap(HttpServletRequest req) { this(req.getQueryString()); }
  
  public String get(String name) {
    List<String> v = (List)this.store.get(name);
    return (v != null) ? (String)v.get(0) : null;
  }
  
  public List<String> getAll(String name) {
    List<String> v = (List)this.store.get(name);
    return (v != null) ? Collections.unmodifiableList(v) : Collections.emptyList();
  }
}
