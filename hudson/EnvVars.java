package hudson;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

public class EnvVars extends TreeMap<String, String> {
  private static final long serialVersionUID = 4320331661987259022L;
  
  private static Logger LOGGER = Logger.getLogger(EnvVars.class.getName());
  
  private Platform platform;
  
  @CheckForNull
  public Platform getPlatform() { return this.platform; }
  
  public void setPlatform(@NonNull Platform platform) { this.platform = platform; }
  
  public EnvVars() { super(String.CASE_INSENSITIVE_ORDER); }
  
  public EnvVars(@NonNull Map<String, String> m) {
    this();
    putAll(m);
    if (m instanceof EnvVars) {
      EnvVars lhs = (EnvVars)m;
      this.platform = lhs.platform;
    } 
  }
  
  public EnvVars(@NonNull EnvVars m) { this(m); }
  
  public EnvVars(String... keyValuePairs) {
    this();
    if (keyValuePairs.length % 2 != 0)
      throw new IllegalArgumentException(Arrays.asList(keyValuePairs).toString()); 
    for (int i = 0; i < keyValuePairs.length; i += 2)
      put(keyValuePairs[i], keyValuePairs[i + 1]); 
  }
  
  public void override(String key, String value) {
    if (value == null || value.isEmpty()) {
      remove(key);
      return;
    } 
    int idx = key.indexOf('+');
    if (idx > 0) {
      String realKey = key.substring(0, idx);
      String v = (String)get(realKey);
      if (v == null) {
        v = value;
      } else {
        char ch = (this.platform == null) ? File.pathSeparatorChar : this.platform.pathSeparator;
        v = value + value + ch;
      } 
      put(realKey, v);
      return;
    } 
    put(key, value);
  }
  
  public EnvVars overrideAll(Map<String, String> all) {
    for (Map.Entry<String, String> e : all.entrySet())
      override((String)e.getKey(), (String)e.getValue()); 
    return this;
  }
  
  public EnvVars overrideExpandingAll(@NonNull Map<String, String> all) {
    for (String key : (new OverrideOrderCalculator(this, all)).getOrderedVariableNames())
      override(key, expand((String)all.get(key))); 
    return this;
  }
  
  public static void resolve(Map<String, String> env) {
    for (Map.Entry<String, String> entry : env.entrySet())
      entry.setValue(Util.replaceMacro((String)entry.getValue(), env)); 
  }
  
  public String get(String key, String defaultValue) {
    String v = (String)get(key);
    if (v == null)
      v = defaultValue; 
    return v;
  }
  
  public String put(String key, String value) {
    if (value == null)
      throw new IllegalArgumentException("Null value not allowed as an environment variable: " + key); 
    return (String)super.put(key, value);
  }
  
  public void putIfNotNull(String key, String value) {
    if (value != null)
      put(key, value); 
  }
  
  public void putAllNonNull(Map<String, String> map) { map.forEach(this::putIfNotNull); }
  
  public void addLine(String line) {
    int sep = line.indexOf('=');
    if (sep > 0)
      put(line.substring(0, sep), line.substring(sep + 1)); 
  }
  
  public String expand(String s) { return Util.replaceMacro(s, this); }
  
  public static EnvVars createCookie() { return new EnvVars(new String[] { "HUDSON_COOKIE", UUID.randomUUID().toString() }); }
  
  public static EnvVars getRemote(VirtualChannel channel) throws IOException, InterruptedException {
    if (channel == null)
      return new EnvVars(new String[] { "N/A", "N/A" }); 
    return (EnvVars)channel.call(new GetEnvVars());
  }
  
  public static final Map<String, String> masterEnvVars = initMaster();
  
  private static EnvVars initMaster() {
    vars = new EnvVars(System.getenv());
    vars.platform = Platform.current();
    if (Main.isUnitTest || Main.isDevelopmentMode)
      vars.remove("MAVEN_OPTS"); 
    return vars;
  }
}
