package hudson.cli;

import hudson.Extension;
import jenkins.model.Jenkins;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Extension
public class WhoAmICommand extends CLICommand {
  public String getShortDescription() { return Messages.WhoAmICommand_ShortDescription(); }
  
  protected int run() {
    Authentication a = Jenkins.getAuthentication2();
    this.stdout.println("Authenticated as: " + a.getName());
    this.stdout.println("Authorities:");
    for (GrantedAuthority ga : a.getAuthorities())
      this.stdout.println("  " + ga.getAuthority()); 
    return 0;
  }
}
