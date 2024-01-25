package jenkins.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.TokenList;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ProgressiveRendering {
  private static final Logger LOG = Logger.getLogger(ProgressiveRendering.class.getName());
  
  private static final Long DEBUG_SLEEP = SystemProperties.getLong("jenkins.util.ProgressiveRendering.DEBUG_SLEEP");
  
  private static final int CANCELED = -1;
  
  private static final int ERROR = -2;
  
  private double status = 0.0D;
  
  private long lastNewsTime;
  
  private final SecurityContext securityContext = SecurityContextHolder.getContext();
  
  private final RequestImpl request = createMockRequest();
  
  private final String uri = this.request.getRequestURI();
  
  private long start;
  
  private BoundObjectTable.Table boundObjectTable;
  
  private String boundId;
  
  @JavaScriptMethod
  public final void start() {
    Ancestor ancestor = Stapler.getCurrentRequest().findAncestor(BoundObjectTable.class);
    if (ancestor == null)
      throw new IllegalStateException("no BoundObjectTable"); 
    this.boundObjectTable = ((BoundObjectTable)ancestor.getObject()).getTable();
    this.boundId = ancestor.getNextToken(0);
    LOG.log(Level.FINE, "starting rendering {0} at {1}", new Object[] { this.uri, this.boundId });
    ExecutorService executorService = executorService();
    executorService.submit(new Object(this, executorService));
  }
  
  private void release() {
    try {
      Method release = BoundObjectTable.Table.class.getDeclaredMethod("release", new Class[] { String.class });
      release.setAccessible(true);
      release.invoke(this.boundObjectTable, new Object[] { this.boundId });
    } catch (Exception x) {
      LOG.log(Level.WARNING, "failed to unbind " + this.boundId, x);
    } 
  }
  
  private static RequestImpl createMockRequest() {
    currentRequest = (RequestImpl)Stapler.getCurrentRequest();
    HttpServletRequest original = (HttpServletRequest)currentRequest.getRequest();
    Map<String, Object> getters = new HashMap<String, Object>();
    for (Method method : HttpServletRequest.class.getMethods()) {
      String m = method.getName();
      if ((m.startsWith("get") || m.startsWith("is")) && method.getParameterTypes().length == 0) {
        Class<?> type = method.getReturnType();
        if (type.isPrimitive() || type == String.class || type == java.util.Locale.class)
          try {
            getters.put(m, method.invoke(original, new Object[0]));
          } catch (Exception x) {
            LOG.log(Level.WARNING, "cannot mock Stapler request " + method, x);
          }  
      } 
    } 
    List ancestors = currentRequest.ancestors;
    LOG.log(Level.FINER, "mocking ancestors {0} using {1}", new Object[] { ancestors, getters });
    TokenList tokens = currentRequest.tokens;
    return new RequestImpl(Stapler.getCurrent(), (HttpServletRequest)Proxy.newProxyInstance(ProgressiveRendering.class.getClassLoader(), new Class[] { HttpServletRequest.class }, new Object(getters)), ancestors, tokens);
  }
  
  private static void setCurrentRequest(RequestImpl request) {
    try {
      Field field = Stapler.class.getDeclaredField("CURRENT_REQUEST");
      field.setAccessible(true);
      ((ThreadLocal)field.get(null)).set(request);
    } catch (Exception x) {
      LOG.log(Level.WARNING, "cannot mock Stapler request", x);
    } 
  }
  
  protected abstract void compute();
  
  @NonNull
  protected abstract JSON data();
  
  protected final void progress(double completedFraction) {
    if (completedFraction < 0.0D || completedFraction > 1.0D)
      throw new IllegalArgumentException("" + completedFraction + " should be in [0,1]"); 
    this.status = completedFraction;
  }
  
  protected final boolean canceled() {
    if (DEBUG_SLEEP != null)
      try {
        Thread.sleep(DEBUG_SLEEP.longValue());
      } catch (InterruptedException interruptedException) {} 
    if (this.status == -2.0D)
      return true; 
    long now = System.currentTimeMillis();
    long elapsed = now - this.lastNewsTime;
    if (elapsed > timeout()) {
      this.status = -1.0D;
      LOG.log(Level.FINE, "{0} canceled due to {1}msec inactivity after {2}msec", new Object[] { this.uri, Long.valueOf(elapsed), Long.valueOf(now - this.start) });
      return true;
    } 
    return false;
  }
  
  @JavaScriptMethod
  public final JSONObject news() {
    this.lastNewsTime = System.currentTimeMillis();
    JSONObject r = new JSONObject();
    try {
      r.put("data", data());
    } catch (RuntimeException x) {
      LOG.log(Level.WARNING, "failed to update " + this.uri, x);
      this.status = -2.0D;
    } 
    Object statusJSON = (this.status == 1.0D) ? "done" : ((this.status == -1.0D) ? "canceled" : ((this.status == -2.0D) ? "error" : Double.valueOf(this.status)));
    r.put("status", statusJSON);
    if (statusJSON instanceof String) {
      LOG.log(Level.FINE, "finished in news so releasing {0}", this.boundId);
      release();
    } 
    this.lastNewsTime = System.currentTimeMillis();
    LOG.log(Level.FINER, "news from {0}", this.uri);
    return r;
  }
  
  protected ExecutorService executorService() { return Timer.get(); }
  
  protected long timeout() { return 15000L; }
}
