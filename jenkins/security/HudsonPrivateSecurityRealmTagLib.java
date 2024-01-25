package jenkins.security;

import groovy.lang.Closure;
import java.util.Map;
import org.kohsuke.stapler.jelly.groovy.TagLibraryUri;
import org.kohsuke.stapler.jelly.groovy.TypedTagLibrary;

@TagLibraryUri("/hudson/security/HudsonPrivateSecurityRealm")
public interface HudsonPrivateSecurityRealmTagLib extends TypedTagLibrary {
  void config(Map paramMap, Closure paramClosure);
  
  void config(Closure paramClosure);
  
  void config(Map paramMap);
  
  void config();
  
  void _entryForm(Map paramMap, Closure paramClosure);
  
  void _entryForm(Closure paramClosure);
  
  void _entryForm(Map paramMap);
  
  void _entryForm();
  
  void success(Map paramMap, Closure paramClosure);
  
  void success(Closure paramClosure);
  
  void success(Map paramMap);
  
  void success();
  
  void index(Map paramMap, Closure paramClosure);
  
  void index(Closure paramClosure);
  
  void index(Map paramMap);
  
  void index();
  
  void _entryFormPage(Map paramMap, Closure paramClosure);
  
  void _entryFormPage(Closure paramClosure);
  
  void _entryFormPage(Map paramMap);
  
  void _entryFormPage();
  
  void addUser(Map paramMap, Closure paramClosure);
  
  void addUser(Closure paramClosure);
  
  void addUser(Map paramMap);
  
  void addUser();
  
  void loginLink(Map paramMap, Closure paramClosure);
  
  void loginLink(Closure paramClosure);
  
  void loginLink(Map paramMap);
  
  void loginLink();
  
  void signup(Map paramMap, Closure paramClosure);
  
  void signup(Closure paramClosure);
  
  void signup(Map paramMap);
  
  void signup();
  
  void signupWithFederatedIdentity(Map paramMap, Closure paramClosure);
  
  void signupWithFederatedIdentity(Closure paramClosure);
  
  void signupWithFederatedIdentity(Map paramMap);
  
  void signupWithFederatedIdentity();
  
  void firstUser(Map paramMap, Closure paramClosure);
  
  void firstUser(Closure paramClosure);
  
  void firstUser(Map paramMap);
  
  void firstUser();
}
