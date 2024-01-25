package jenkins.security;

import org.jenkinsci.remoting.Role;

public class Roles {
  public static final Role MASTER = new Role("master");
  
  public static final Role SLAVE = new Role("slave");
}
