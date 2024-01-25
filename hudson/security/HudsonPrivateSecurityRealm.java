package hudson.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import hudson.model.Messages;
import hudson.model.ModelObject;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.security.captcha.CaptchaSupport;
import hudson.util.PluginServletFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import jenkins.model.Jenkins;
import jenkins.security.SecurityListener;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class HudsonPrivateSecurityRealm extends AbstractPasswordBasedSecurityRealm implements ModelObject, AccessControlled {
  private static String ID_REGEX = System.getProperty(HudsonPrivateSecurityRealm.class.getName() + ".ID_REGEX");
  
  private static final String DEFAULT_ID_REGEX = "^[\\w-]+$";
  
  private final boolean disableSignup;
  
  private final boolean enableCaptcha;
  
  @Deprecated
  public HudsonPrivateSecurityRealm(boolean allowsSignup) { this(allowsSignup, false, (CaptchaSupport)null); }
  
  @DataBoundConstructor
  public HudsonPrivateSecurityRealm(boolean allowsSignup, boolean enableCaptcha, CaptchaSupport captchaSupport) {
    this.disableSignup = !allowsSignup;
    this.enableCaptcha = enableCaptcha;
    setCaptchaSupport(captchaSupport);
    if (!allowsSignup && !hasSomeUser())
      try {
        PluginServletFilter.addFilter(CREATE_FIRST_USER_FILTER);
      } catch (ServletException e) {
        throw new AssertionError(e);
      }  
  }
  
  public boolean allowsSignup() { return !this.disableSignup; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean getAllowsSignup() { return allowsSignup(); }
  
  public boolean isEnableCaptcha() { return this.enableCaptcha; }
  
  private static boolean hasSomeUser() {
    for (User u : User.getAll()) {
      if (u.getProperty(Details.class) != null)
        return true; 
    } 
    return false;
  }
  
  public GroupDetails loadGroupByGroupname2(String groupname, boolean fetchMembers) throws UsernameNotFoundException { throw new UsernameNotFoundException(groupname); }
  
  public UserDetails loadUserByUsername2(String username) throws UsernameNotFoundException { return load(username).asUserDetails(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Details load(String username) throws UsernameNotFoundException {
    User u = User.getById(username, false);
    Details p = (u != null) ? (Details)u.getProperty(Details.class) : null;
    if (p == null)
      throw new UsernameNotFoundException("Password is not set: " + username); 
    if (p.getUser() == null)
      throw new AssertionError(); 
    return p;
  }
  
  protected UserDetails authenticate2(String username, String password) throws AuthenticationException {
    Details u;
    try {
      u = load(username);
    } catch (UsernameNotFoundException ex) {
      PASSWORD_ENCODER.matches(password, ENCODED_INVALID_USER_PASSWORD);
      throw ex;
    } 
    if (!u.isPasswordCorrect(password))
      throw new BadCredentialsException("Bad credentials"); 
    return u.asUserDetails();
  }
  
  public HttpResponse commenceSignup(FederatedLoginService.FederatedIdentity identity) {
    Stapler.getCurrentRequest().getSession().setAttribute(FEDERATED_IDENTITY_SESSION_KEY, identity);
    return new Object(this, this, "signupWithFederatedIdentity.jelly", identity);
  }
  
  @RequirePOST
  public User doCreateAccountWithFederatedIdentity(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    User u = _doCreateAccount(req, rsp, "signupWithFederatedIdentity.jelly");
    if (u != null)
      ((FederatedLoginService.FederatedIdentity)req.getSession().getAttribute(FEDERATED_IDENTITY_SESSION_KEY)).addTo(u); 
    return u;
  }
  
  private static final String FEDERATED_IDENTITY_SESSION_KEY = HudsonPrivateSecurityRealm.class.getName() + ".federatedIdentity";
  
  @RequirePOST
  public User doCreateAccount(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { return _doCreateAccount(req, rsp, "signup.jelly"); }
  
  private User _doCreateAccount(StaplerRequest req, StaplerResponse rsp, String formView) throws ServletException, IOException {
    if (!allowsSignup())
      throw HttpResponses.errorWithoutStack(401, "User sign up is prohibited"); 
    boolean firstUser = !hasSomeUser();
    User u = createAccount(req, rsp, this.enableCaptcha, formView);
    if (u != null) {
      if (firstUser)
        tryToMakeAdmin(u); 
      loginAndTakeBack(req, rsp, u);
    } 
    return u;
  }
  
  private void loginAndTakeBack(StaplerRequest req, StaplerResponse rsp, User u) throws ServletException, IOException {
    HttpSession session = req.getSession(false);
    if (session != null)
      session.invalidate(); 
    req.getSession(true);
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(u.getId(), req.getParameter("password1"));
    Authentication authentication = (getSecurityComponents()).manager2.authenticate(usernamePasswordAuthenticationToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);
    SecurityListener.fireLoggedIn(u.getId());
    req.getView(this, "success.jelly").forward(req, rsp);
  }
  
  @RequirePOST
  public void doCreateAccountByAdmin(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { createAccountByAdmin(req, rsp, "addUser.jelly", "."); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public User createAccountByAdmin(StaplerRequest req, StaplerResponse rsp, String addUserView, String successView) throws IOException, ServletException {
    checkPermission(Jenkins.ADMINISTER);
    User u = createAccount(req, rsp, false, addUserView);
    if (u != null && successView != null)
      rsp.sendRedirect(successView); 
    return u;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public User createAccountFromSetupWizard(StaplerRequest req) throws IOException, AccountCreationFailedException {
    checkPermission(Jenkins.ADMINISTER);
    SignupInfo si = validateAccountCreationForm(req, false);
    if (!si.errors.isEmpty()) {
      String messages = getErrorMessages(si);
      throw new AccountCreationFailedException(messages);
    } 
    return createAccount(si);
  }
  
  private String getErrorMessages(SignupInfo si) {
    StringBuilder messages = new StringBuilder();
    for (String message : si.errors.values())
      messages.append(message).append(" | "); 
    return messages.toString();
  }
  
  @RequirePOST
  public void doCreateFirstAccount(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    if (hasSomeUser()) {
      rsp.sendError(401, "First user was already created");
      return;
    } 
    User u = createAccount(req, rsp, false, "firstUser.jelly");
    if (u != null) {
      tryToMakeAdmin(u);
      loginAndTakeBack(req, rsp, u);
    } 
  }
  
  private void tryToMakeAdmin(User u) {
    AuthorizationStrategy as = Jenkins.get().getAuthorizationStrategy();
    for (PermissionAdder adder : ExtensionList.lookup(PermissionAdder.class)) {
      if (adder.add(as, u, Jenkins.ADMINISTER))
        return; 
    } 
  }
  
  private User createAccount(StaplerRequest req, StaplerResponse rsp, boolean validateCaptcha, String formView) throws ServletException, IOException {
    SignupInfo si = validateAccountCreationForm(req, validateCaptcha);
    if (!si.errors.isEmpty()) {
      req.getView(this, formView).forward(req, rsp);
      return null;
    } 
    return createAccount(si);
  }
  
  @SuppressFBWarnings(value = {"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"}, justification = "written to by Stapler")
  private SignupInfo validateAccountCreationForm(StaplerRequest req, boolean validateCaptcha) {
    SignupInfo si = new SignupInfo(req);
    if (validateCaptcha && !validateCaptcha(si.captcha))
      si.errors.put("captcha", Messages.HudsonPrivateSecurityRealm_CreateAccount_TextNotMatchWordInImage()); 
    if (si.username == null || si.username.isEmpty()) {
      si.errors.put("username", Messages.HudsonPrivateSecurityRealm_CreateAccount_UserNameRequired());
    } else if (!containsOnlyAcceptableCharacters(si.username)) {
      if (ID_REGEX == null) {
        si.errors.put("username", Messages.HudsonPrivateSecurityRealm_CreateAccount_UserNameInvalidCharacters());
      } else {
        si.errors.put("username", Messages.HudsonPrivateSecurityRealm_CreateAccount_UserNameInvalidCharactersCustom(ID_REGEX));
      } 
    } else {
      User user = User.getById(si.username, false);
      if (null != user)
        if (user.getProperty(Details.class) != null)
          si.errors.put("username", Messages.HudsonPrivateSecurityRealm_CreateAccount_UserNameAlreadyTaken());  
    } 
    if (si.password1 != null && !si.password1.equals(si.password2))
      si.errors.put("password1", Messages.HudsonPrivateSecurityRealm_CreateAccount_PasswordNotMatch()); 
    if (si.password1 == null || si.password1.length() == 0)
      si.errors.put("password1", Messages.HudsonPrivateSecurityRealm_CreateAccount_PasswordRequired()); 
    if (si.fullname == null || si.fullname.isEmpty())
      si.fullname = si.username; 
    if (isMailerPluginPresent() && (si.email == null || !si.email.contains("@")))
      si.errors.put("email", Messages.HudsonPrivateSecurityRealm_CreateAccount_InvalidEmailAddress()); 
    if (!User.isIdOrFullnameAllowed(si.username))
      si.errors.put("username", Messages.User_IllegalUsername(si.username)); 
    if (!User.isIdOrFullnameAllowed(si.fullname))
      si.errors.put("fullname", Messages.User_IllegalFullname(si.fullname)); 
    req.setAttribute("data", si);
    return si;
  }
  
  private User createAccount(SignupInfo si) throws IOException {
    if (!si.errors.isEmpty()) {
      String messages = getErrorMessages(si);
      throw new IllegalArgumentException("invalid signup info passed to createAccount(si): " + messages);
    } 
    User user = createAccount(si.username, si.password1);
    user.setFullName(si.fullname);
    if (isMailerPluginPresent())
      try {
        Class<?> up = (Jenkins.get()).pluginManager.uberClassLoader.loadClass("hudson.tasks.Mailer$UserProperty");
        Constructor<?> c = up.getDeclaredConstructor(new Class[] { String.class });
        user.addProperty((UserProperty)c.newInstance(new Object[] { si.email }));
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }  
    user.save();
    return user;
  }
  
  private boolean containsOnlyAcceptableCharacters(@NonNull String value) {
    if (ID_REGEX == null)
      return value.matches("^[\\w-]+$"); 
    return value.matches(ID_REGEX);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isMailerPluginPresent() {
    try {
      return (null != (Jenkins.get()).pluginManager.uberClassLoader.loadClass("hudson.tasks.Mailer$UserProperty"));
    } catch (ClassNotFoundException e) {
      LOGGER.finer("Mailer plugin not present");
      return false;
    } 
  }
  
  public User createAccount(String userName, String password) throws IOException {
    User user = User.getById(userName, true);
    user.addProperty(Details.fromPlainPassword(password));
    SecurityListener.fireUserCreated(user.getId());
    return user;
  }
  
  public User createAccountWithHashedPassword(String userName, String hashedPassword) throws IOException {
    if (!PASSWORD_ENCODER.isPasswordHashed(hashedPassword))
      throw new IllegalArgumentException("this method should only be called with a pre-hashed password"); 
    User user = User.getById(userName, true);
    user.addProperty(Details.fromHashedPassword(hashedPassword));
    SecurityListener.fireUserCreated(user.getId());
    return user;
  }
  
  public String getDisplayName() { return Messages.HudsonPrivateSecurityRealm_DisplayName(); }
  
  public ACL getACL() { return Jenkins.get().getACL(); }
  
  public void checkPermission(Permission permission) { Jenkins.get().checkPermission(permission); }
  
  public boolean hasPermission(Permission permission) { return Jenkins.get().hasPermission(permission); }
  
  public List<User> getAllUsers() {
    List<User> r = new ArrayList<User>();
    for (User u : User.getAll()) {
      if (u.getProperty(Details.class) != null)
        r.add(u); 
    } 
    Collections.sort(r);
    return r;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public User getUser(String id) { return User.getById(id, (User.ALLOW_USER_CREATION_VIA_URL && hasPermission(Jenkins.ADMINISTER))); }
  
  private static final Collection<? extends GrantedAuthority> TEST_AUTHORITY = Set.of(AUTHENTICATED_AUTHORITY2);
  
  static final JBCryptEncoder JBCRYPT_ENCODER = new JBCryptEncoder();
  
  public static final MultiPasswordEncoder PASSWORD_ENCODER = new MultiPasswordEncoder();
  
  private static final String ENCODED_INVALID_USER_PASSWORD = PASSWORD_ENCODER.encode(generatePassword());
  
  @SuppressFBWarnings(value = {"DMI_RANDOM_USED_ONLY_ONCE", "PREDICTABLE_RANDOM"}, justification = "https://github.com/spotbugs/spotbugs/issues/1539 and doesn't need to be secure, we're just not hardcoding a 'wrong' password")
  private static String generatePassword() { return ((StringBuilder)(new Random()).ints(20L, 33, 127).mapToObj(i -> Character.valueOf((char)i)).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)).toString(); }
  
  private static final Filter CREATE_FIRST_USER_FILTER = new Object();
  
  private static final Logger LOGGER = Logger.getLogger(HudsonPrivateSecurityRealm.class.getName());
}
