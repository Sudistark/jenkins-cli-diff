package hudson.security;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.cli.CLICommand;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.captcha.CaptchaSupport;
import hudson.util.DescriptorList;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import jenkins.model.IdStrategy;
import jenkins.model.Jenkins;
import jenkins.security.AcegiSecurityExceptionFilter;
import jenkins.security.AuthenticationSuccessHandler;
import jenkins.security.BasicHeaderProcessor;
import jenkins.util.SystemProperties;
import org.acegisecurity.AcegiSecurityException;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public abstract class SecurityRealm extends AbstractDescribableImpl<SecurityRealm> implements ExtensionPoint {
  private CaptchaSupport captchaSupport;
  
  public IdStrategy getUserIdStrategy() { return IdStrategy.CASE_INSENSITIVE; }
  
  public IdStrategy getGroupIdStrategy() { return getUserIdStrategy(); }
  
  @Deprecated
  public CliAuthenticator createCliAuthenticator(CLICommand command) { throw new UnsupportedOperationException(); }
  
  public Descriptor<SecurityRealm> getDescriptor() { return super.getDescriptor(); }
  
  public String getAuthenticationGatewayUrl() { return "j_spring_security_check"; }
  
  public String getLoginUrl() { return "login"; }
  
  public boolean canLogOut() { return true; }
  
  protected String getPostLogOutUrl2(StaplerRequest req, Authentication auth) {
    if (Util.isOverridden(SecurityRealm.class, getClass(), "getPostLogOutUrl", new Class[] { StaplerRequest.class, Authentication.class }) && !((Boolean)insideGetPostLogOutUrl.get()).booleanValue()) {
      insideGetPostLogOutUrl.set(Boolean.valueOf(true));
      try {
        return getPostLogOutUrl(req, Authentication.fromSpring(auth));
      } finally {
        insideGetPostLogOutUrl.set(Boolean.valueOf(false));
      } 
    } 
    return req.getContextPath() + "/";
  }
  
  private static final ThreadLocal<Boolean> insideGetPostLogOutUrl = ThreadLocal.withInitial(() -> Boolean.valueOf(false));
  
  private SecurityComponents securityComponents;
  
  @Deprecated
  protected String getPostLogOutUrl(StaplerRequest req, Authentication auth) { return getPostLogOutUrl2(req, auth.toSpring()); }
  
  public CaptchaSupport getCaptchaSupport() { return this.captchaSupport; }
  
  public void setCaptchaSupport(CaptchaSupport captchaSupport) { this.captchaSupport = captchaSupport; }
  
  public List<Descriptor<CaptchaSupport>> getCaptchaSupportDescriptors() { return CaptchaSupport.all(); }
  
  public void doLogout(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    HttpSession session = req.getSession(false);
    if (session != null)
      session.invalidate(); 
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    SecurityContextHolder.clearContext();
    String contextPath = (req.getContextPath().length() > 0) ? req.getContextPath() : "/";
    resetRememberMeCookie(req, rsp, contextPath);
    clearStaleSessionCookies(req, rsp, contextPath);
    rsp.sendRedirect2(getPostLogOutUrl2(req, auth));
  }
  
  private void resetRememberMeCookie(StaplerRequest req, StaplerResponse rsp, String contextPath) {
    Cookie cookie = new Cookie("remember-me", "");
    cookie.setMaxAge(0);
    cookie.setSecure(req.isSecure());
    cookie.setHttpOnly(true);
    cookie.setPath(contextPath);
    rsp.addCookie(cookie);
  }
  
  private void clearStaleSessionCookies(StaplerRequest req, StaplerResponse rsp, String contextPath) {
    String cookieName = "JSESSIONID.";
    Cookie[] cookies = req.getCookies();
    if (cookies != null)
      for (Cookie cookie : cookies) {
        if (cookie.getName().startsWith("JSESSIONID.")) {
          LOGGER.log(Level.FINE, "Removing cookie {0} during logout", cookie.getName());
          cookie.setMaxAge(0);
          cookie.setValue("");
          rsp.addCookie(cookie);
        } 
      }  
  }
  
  public boolean allowsSignup() {
    Class clz = getClass();
    return (clz.getClassLoader().getResource(clz.getName().replace(46, 47) + "/signup.jelly") != null);
  }
  
  public UserDetails loadUserByUsername2(String username) throws UsernameNotFoundException {
    if (Util.isOverridden(SecurityRealm.class, getClass(), "loadUserByUsername", new Class[] { String.class }))
      try {
        return loadUserByUsername(username).toSpring();
      } catch (AcegiSecurityException x) {
        throw x.toSpring();
      } catch (DataAccessException x) {
        throw x.toSpring();
      }  
    return (getSecurityComponents()).userDetails2.loadUserByUsername(username);
  }
  
  @Deprecated
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    try {
      return UserDetails.fromSpring(loadUserByUsername2(username));
    } catch (AuthenticationException x) {
      throw AuthenticationException.fromSpring(x);
    } 
  }
  
  public GroupDetails loadGroupByGroupname2(String groupname, boolean fetchMembers) throws UsernameNotFoundException {
    if (Util.isOverridden(SecurityRealm.class, getClass(), "loadGroupByGroupname", new Class[] { String.class }))
      try {
        return loadGroupByGroupname(groupname);
      } catch (AcegiSecurityException x) {
        throw x.toSpring();
      } catch (DataAccessException x) {
        throw x.toSpring();
      }  
    if (Util.isOverridden(SecurityRealm.class, getClass(), "loadGroupByGroupname", new Class[] { String.class, boolean.class }))
      try {
        return loadGroupByGroupname(groupname, fetchMembers);
      } catch (AcegiSecurityException x) {
        throw x.toSpring();
      } catch (DataAccessException x) {
        throw x.toSpring();
      }  
    throw new UserMayOrMayNotExistException2(groupname);
  }
  
  @Deprecated
  public GroupDetails loadGroupByGroupname(String groupname) throws UsernameNotFoundException, DataAccessException {
    try {
      return loadGroupByGroupname2(groupname, false);
    } catch (AuthenticationException x) {
      throw AuthenticationException.fromSpring(x);
    } 
  }
  
  @Deprecated
  public GroupDetails loadGroupByGroupname(String groupname, boolean fetchMembers) throws UsernameNotFoundException {
    try {
      return loadGroupByGroupname2(groupname, fetchMembers);
    } catch (AuthenticationException x) {
      throw AuthenticationException.fromSpring(x);
    } 
  }
  
  public HttpResponse commenceSignup(FederatedLoginService.FederatedIdentity identity) { throw new UnsupportedOperationException(); }
  
  public final void doCaptcha(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    if (this.captchaSupport != null) {
      String id = req.getSession().getId();
      rsp.setContentType("image/png");
      rsp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
      rsp.setHeader("Pragma", "no-cache");
      rsp.setHeader("Expires", "0");
      this.captchaSupport.generateImage(id, rsp.getOutputStream());
    } 
  }
  
  protected final boolean validateCaptcha(String text) {
    if (this.captchaSupport != null) {
      String id = Stapler.getCurrentRequest().getSession().getId();
      return this.captchaSupport.validateCaptcha(id, text);
    } 
    return true;
  }
  
  public SecurityComponents getSecurityComponents() {
    if (this.securityComponents == null)
      this.securityComponents = createSecurityComponents(); 
    return this.securityComponents;
  }
  
  public Filter createFilter(FilterConfig filterConfig) {
    LOGGER.entering(SecurityRealm.class.getName(), "createFilter");
    SecurityComponents sc = getSecurityComponents();
    List<Filter> filters = new ArrayList<Filter>();
    HttpSessionSecurityContextRepository httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository();
    httpSessionSecurityContextRepository.setAllowSessionCreation(false);
    filters.add(new HttpSessionContextIntegrationFilter2(httpSessionSecurityContextRepository));
    BasicHeaderProcessor bhp = new BasicHeaderProcessor();
    BasicAuthenticationEntryPoint basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
    basicAuthenticationEntryPoint.setRealmName("Jenkins");
    bhp.setAuthenticationEntryPoint(basicAuthenticationEntryPoint);
    bhp.setRememberMeServices(sc.rememberMe2);
    filters.add(bhp);
    AuthenticationProcessingFilter2 apf = new AuthenticationProcessingFilter2(getAuthenticationGatewayUrl());
    apf.setAuthenticationManager(sc.manager2);
    if (SystemProperties.getInteger(SecurityRealm.class.getName() + ".sessionFixationProtectionMode", Integer.valueOf(1)).intValue() == 1)
      apf.setSessionAuthenticationStrategy(new SessionFixationProtectionStrategy()); 
    apf.setRememberMeServices(sc.rememberMe2);
    AuthenticationSuccessHandler successHandler = new AuthenticationSuccessHandler();
    successHandler.setTargetUrlParameter("from");
    apf.setAuthenticationSuccessHandler(successHandler);
    apf.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/loginError"));
    filters.add(apf);
    filters.add(new RememberMeAuthenticationFilter(sc.manager2, sc.rememberMe2));
    filters.addAll(commonFilters());
    return new ChainedServletFilter(filters);
  }
  
  protected final List<Filter> commonFilters() {
    AnonymousAuthenticationFilter apf = new AnonymousAuthenticationFilter("anonymous", "anonymous", List.of(new SimpleGrantedAuthority("anonymous")));
    ExceptionTranslationFilter etf = new ExceptionTranslationFilter(new HudsonAuthenticationEntryPoint("/" + getLoginUrl() + "?from={0}"));
    etf.setAccessDeniedHandler(new AccessDeniedHandlerImpl());
    UnwrapSecurityExceptionFilter usef = new UnwrapSecurityExceptionFilter();
    AcegiSecurityExceptionFilter asef = new AcegiSecurityExceptionFilter();
    return Arrays.asList(new Filter[] { apf, etf, usef, asef });
  }
  
  public static final SecurityRealm NO_AUTHENTICATION = new None();
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public static String getFrom() {
    from = null;
    StaplerRequest request = Stapler.getCurrentRequest();
    if (request != null)
      from = request.getParameter("from"); 
    if (request != null && request.getRequestURI().equals(request.getContextPath() + "/404")) {
      HttpSession session = request.getSession(false);
      if (session != null) {
        Object attribute = session.getAttribute("from");
        if (attribute != null)
          from = attribute.toString(); 
      } 
    } 
    if (from == null && request != null && request
      
      .getRequestURI() != null && 
      
      !request.getRequestURI().equals(request.getContextPath() + "/loginError") && 
      !request.getRequestURI().equals(request.getContextPath() + "/login") && 
      !request.getRequestURI().equals(request.getContextPath() + "/404"))
      from = request.getRequestURI(); 
    from = StringUtils.defaultIfBlank(from, "/").trim();
    String returnValue = URLEncoder.encode(from, StandardCharsets.UTF_8);
    return StringUtils.isBlank(returnValue) ? "/" : returnValue;
  }
  
  @Deprecated
  public static final DescriptorList<SecurityRealm> LIST = new DescriptorList(SecurityRealm.class);
  
  public static DescriptorExtensionList<SecurityRealm, Descriptor<SecurityRealm>> all() { return Jenkins.get().getDescriptorList(SecurityRealm.class); }
  
  private static final Logger LOGGER = Logger.getLogger(SecurityRealm.class.getName());
  
  public static final GrantedAuthority AUTHENTICATED_AUTHORITY2 = new SimpleGrantedAuthority("authenticated");
  
  @Deprecated
  public static final GrantedAuthority AUTHENTICATED_AUTHORITY = new GrantedAuthorityImpl("authenticated");
  
  public abstract SecurityComponents createSecurityComponents();
}
