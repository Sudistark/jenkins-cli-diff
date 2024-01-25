package hudson.security;

import hudson.Util;
import jenkins.model.Jenkins;
import jenkins.security.ImpersonatingUserDetailsService2;
import jenkins.security.SecurityListener;
import org.acegisecurity.AcegiSecurityException;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public abstract class AbstractPasswordBasedSecurityRealm extends SecurityRealm {
  public SecurityRealm.SecurityComponents createSecurityComponents() {
    Authenticator authenticator = new Authenticator(this);
    RememberMeAuthenticationProvider rmap = new RememberMeAuthenticationProvider(Jenkins.get().getSecretKey());
    AnonymousAuthenticationProvider aap = new AnonymousAuthenticationProvider("anonymous");
    ProviderManager providerManager = new ProviderManager(new AuthenticationProvider[] { authenticator, rmap, aap });
    return new SecurityRealm.SecurityComponents(providerManager, new ImpersonatingUserDetailsService2(this::loadUserByUsername2));
  }
  
  protected UserDetails authenticate2(String username, String password) throws AuthenticationException {
    if (Util.isOverridden(AbstractPasswordBasedSecurityRealm.class, getClass(), "authenticate", new Class[] { String.class, String.class }))
      try {
        return authenticate(username, password).toSpring();
      } catch (AcegiSecurityException x) {
        throw x.toSpring();
      }  
    throw new AbstractMethodError("Implement authenticate2");
  }
  
  @Deprecated
  protected UserDetails authenticate(String username, String password) throws AuthenticationException {
    try {
      return UserDetails.fromSpring(authenticate2(username, password));
    } catch (AuthenticationException x) {
      throw AuthenticationException.fromSpring(x);
    } 
  }
  
  private UserDetails doAuthenticate(String username, String password) throws AuthenticationException {
    try {
      UserDetails user = authenticate2(username, password);
      SecurityListener.fireAuthenticated2(user);
      return user;
    } catch (AuthenticationException x) {
      SecurityListener.fireFailedToAuthenticate(username);
      throw x;
    } 
  }
  
  public UserDetails loadUserByUsername2(String username) throws UsernameNotFoundException {
    if (Util.isOverridden(AbstractPasswordBasedSecurityRealm.class, getClass(), "loadUserByUsername", new Class[] { String.class }))
      try {
        return loadUserByUsername(username).toSpring();
      } catch (AcegiSecurityException x) {
        throw x.toSpring();
      } catch (DataAccessException x) {
        throw x.toSpring();
      }  
    throw new AbstractMethodError("Implement loadUserByUsername2");
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
    if (Util.isOverridden(AbstractPasswordBasedSecurityRealm.class, getClass(), "loadGroupByGroupname", new Class[] { String.class }))
      try {
        return loadGroupByGroupname(groupname);
      } catch (AcegiSecurityException x) {
        throw x.toSpring();
      } catch (DataAccessException x) {
        throw x.toSpring();
      }  
    throw new AbstractMethodError("Implement loadGroupByGroupname2");
  }
  
  @Deprecated
  public GroupDetails loadGroupByGroupname(String groupname) throws UsernameNotFoundException, DataAccessException {
    try {
      return loadGroupByGroupname2(groupname, false);
    } catch (AuthenticationException x) {
      throw AuthenticationException.fromSpring(x);
    } 
  }
}
