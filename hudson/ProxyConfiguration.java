package hudson;

import com.thoughtworks.xstream.XStream;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.util.DaemonThreadFactory;
import hudson.util.NamingThreadFactory;
import hudson.util.Scrambler;
import hudson.util.Secret;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import jenkins.UserAgentURLConnectionDecorator;
import jenkins.model.Jenkins;
import jenkins.security.stapler.StaplerAccessibleType;
import jenkins.util.JenkinsJVM;
import jenkins.util.SystemProperties;
import org.jvnet.robust_http_client.RetryableHttpStream;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@StaplerAccessibleType
public final class ProxyConfiguration extends AbstractDescribableImpl<ProxyConfiguration> implements Saveable, Serializable {
  private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = SystemProperties.getInteger("hudson.ProxyConfiguration.DEFAULT_CONNECT_TIMEOUT_MILLIS", Integer.valueOf((int)TimeUnit.SECONDS.toMillis(20L))).intValue();
  
  public final String name;
  
  public final int port;
  
  private String userName;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public String noProxyHost;
  
  @Deprecated
  private String password;
  
  private Secret secretPassword;
  
  private String testUrl;
  
  private Authenticator authenticator;
  
  private boolean authCacheSeeded;
  
  @DataBoundConstructor
  public ProxyConfiguration(String name, int port) { this(name, port, null, null); }
  
  public ProxyConfiguration(String name, int port, String userName, String password) { this(name, port, userName, password, null); }
  
  public ProxyConfiguration(String name, int port, String userName, String password, String noProxyHost) { this(name, port, userName, password, noProxyHost, null); }
  
  public ProxyConfiguration(String name, int port, String userName, String password, String noProxyHost, String testUrl) {
    this.name = Util.fixEmptyAndTrim(name);
    this.port = port;
    this.userName = Util.fixEmptyAndTrim(userName);
    String tempPassword = Util.fixEmptyAndTrim(password);
    this.secretPassword = (tempPassword != null) ? Secret.fromString(tempPassword) : null;
    this.noProxyHost = Util.fixEmptyAndTrim(noProxyHost);
    this.testUrl = Util.fixEmptyAndTrim(testUrl);
    this.authenticator = newAuthenticator();
  }
  
  private Authenticator newAuthenticator() { return new Object(this); }
  
  public String getUserName() { return this.userName; }
  
  public Secret getSecretPassword() { return this.secretPassword; }
  
  @Deprecated
  public String getPassword() { return Secret.toString(this.secretPassword); }
  
  @Deprecated
  public String getEncryptedPassword() { return (this.secretPassword == null) ? null : this.secretPassword.getEncryptedValue(); }
  
  public String getTestUrl() { return this.testUrl; }
  
  public int getPort() { return this.port; }
  
  public String getName() { return this.name; }
  
  public List<Pattern> getNoProxyHostPatterns() { return getNoProxyHostPatterns(this.noProxyHost); }
  
  public String getNoProxyHost() { return this.noProxyHost; }
  
  public static List<Pattern> getNoProxyHostPatterns(String noProxyHost) {
    if (noProxyHost == null)
      return Collections.emptyList(); 
    List<Pattern> r = new ArrayList<Pattern>();
    for (String s : noProxyHost.split("[ \t\n,|]+")) {
      if (!s.isEmpty())
        r.add(Pattern.compile(s.replace(".", "\\.").replace("*", ".*"))); 
    } 
    return r;
  }
  
  private static boolean isExcluded(String needle, String haystack) { return getNoProxyHostPatterns(haystack).stream().anyMatch(p -> p.matcher(needle).matches()); }
  
  @DataBoundSetter
  public void setSecretPassword(Secret secretPassword) { this.secretPassword = secretPassword; }
  
  @DataBoundSetter
  public void setTestUrl(String testUrl) { this.testUrl = testUrl; }
  
  @DataBoundSetter
  public void setUserName(String userName) { this.userName = userName; }
  
  @DataBoundSetter
  public void setNoProxyHost(String noProxyHost) { this.noProxyHost = noProxyHost; }
  
  @Deprecated
  public Proxy createProxy() { return createProxy(null); }
  
  public Proxy createProxy(String host) { return createProxy(host, this.name, this.port, this.noProxyHost); }
  
  public static Proxy createProxy(String host, String name, int port, String noProxyHost) {
    if (host != null && noProxyHost != null && isExcluded(host, noProxyHost))
      return Proxy.NO_PROXY; 
    return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(name, port));
  }
  
  public void save() throws IOException {
    if (BulkChange.contains(this))
      return; 
    XmlFile config = getXmlFile();
    config.write(this);
    SaveableListener.fireOnChange(this, config);
  }
  
  private Object readResolve() {
    if (this.secretPassword == null)
      this.secretPassword = Secret.fromString(Scrambler.descramble(this.password)); 
    this.password = null;
    this.authenticator = newAuthenticator();
    return this;
  }
  
  public static XmlFile getXmlFile() { return new XmlFile(XSTREAM, new File(Jenkins.get().getRootDir(), "proxy.xml")); }
  
  public static ProxyConfiguration load() throws IOException {
    f = getXmlFile();
    if (f.exists())
      return (ProxyConfiguration)f.read(); 
    return null;
  }
  
  @Deprecated
  public static URLConnection open(URL url) throws IOException {
    URLConnection con;
    ProxyConfiguration p = get();
    if (p == null) {
      con = url.openConnection();
    } else {
      Proxy proxy = p.createProxy(url.getHost());
      con = url.openConnection(proxy);
      if (p.getUserName() != null) {
        Authenticator.setDefault(p.authenticator);
        p.jenkins48775workaround(proxy, url);
      } 
    } 
    if (DEFAULT_CONNECT_TIMEOUT_MILLIS > 0)
      con.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS); 
    if (JenkinsJVM.isJenkinsJVM())
      decorate(con); 
    return con;
  }
  
  @Deprecated
  public static InputStream getInputStream(URL url) throws IOException {
    ProxyConfiguration p = get();
    if (p == null)
      return new RetryableHttpStream(url); 
    Proxy proxy = p.createProxy(url.getHost());
    RetryableHttpStream retryableHttpStream = new RetryableHttpStream(url, proxy);
    if (p.getUserName() != null) {
      Authenticator.setDefault(p.authenticator);
      p.jenkins48775workaround(proxy, url);
    } 
    return retryableHttpStream;
  }
  
  public static HttpClient newHttpClient() { return newHttpClientBuilder().followRedirects(HttpClient.Redirect.NORMAL).build(); }
  
  private static final Executor httpClientExecutor = Executors.newCachedThreadPool(new NamingThreadFactory(new DaemonThreadFactory(), "Jenkins HttpClient"));
  
  public static HttpClient.Builder newHttpClientBuilder() {
    httpClientBuilder = HttpClient.newBuilder();
    ProxyConfiguration proxyConfiguration = get();
    if (proxyConfiguration != null) {
      if (proxyConfiguration.getName() != null)
        httpClientBuilder.proxy(new JenkinsProxySelector(proxyConfiguration
              .getName(), proxyConfiguration
              .getPort(), proxyConfiguration
              .getNoProxyHost())); 
      if (proxyConfiguration.getUserName() != null)
        httpClientBuilder.authenticator(proxyConfiguration.authenticator); 
    } 
    if (DEFAULT_CONNECT_TIMEOUT_MILLIS > 0)
      httpClientBuilder.connectTimeout(Duration.ofMillis(DEFAULT_CONNECT_TIMEOUT_MILLIS)); 
    httpClientBuilder.executor(httpClientExecutor);
    return httpClientBuilder;
  }
  
  public static HttpRequest.Builder newHttpRequestBuilder(URI uri) {
    HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(uri);
    if (JenkinsJVM.isJenkinsJVM() && !UserAgentURLConnectionDecorator.DISABLED)
      httpRequestBuilder.setHeader("User-Agent", UserAgentURLConnectionDecorator.getUserAgent()); 
    return httpRequestBuilder;
  }
  
  private void jenkins48775workaround(Proxy proxy, URL url) {
    if ("https".equals(url.getProtocol()) && !this.authCacheSeeded && proxy != Proxy.NO_PROXY) {
      preAuth = null;
      try {
        preAuth = (HttpURLConnection)(new URL("http", url.getHost(), -1, "/")).openConnection(proxy);
        preAuth.setRequestMethod("HEAD");
        preAuth.connect();
      } catch (IOException iOException) {
      
      } finally {
        if (preAuth != null)
          preAuth.disconnect(); 
      } 
      this.authCacheSeeded = true;
    } else if ("https".equals(url.getProtocol())) {
      this.authCacheSeeded = (this.authCacheSeeded || proxy != Proxy.NO_PROXY);
    } 
  }
  
  @CheckForNull
  private static ProxyConfiguration get() throws IOException {
    if (JenkinsJVM.isJenkinsJVM())
      return _get(); 
    return null;
  }
  
  @CheckForNull
  private static ProxyConfiguration _get() throws IOException {
    JenkinsJVM.checkJenkinsJVM();
    jenkins = Jenkins.getInstanceOrNull();
    return (jenkins == null) ? null : jenkins.proxy;
  }
  
  private static void decorate(URLConnection con) throws IOException {
    for (URLConnectionDecorator d : URLConnectionDecorator.all())
      d.decorate(con); 
  }
  
  private static final XStream XSTREAM = new XStream2();
  
  private static final long serialVersionUID = 1L;
  
  static  {
    XSTREAM.alias("proxy", ProxyConfiguration.class);
  }
}
