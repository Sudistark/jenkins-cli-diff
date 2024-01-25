package hudson.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Functions;
import hudson.model.Api;
import hudson.model.UnprotectedRootAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import jenkins.model.Jenkins;
import jenkins.util.MemoryReductionUtil;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Extension
@Symbol({"whoAmI"})
@ExportedBean
public class WhoAmI implements UnprotectedRootAction {
  private static final Set<String> dangerousHeaders = Set.of("cookie", "authorization", "www-authenticate", "proxy-authenticate", "proxy-authorization");
  
  public Api getApi() { return new Api(this); }
  
  @Exported
  public String getName() { return auth().getName(); }
  
  @Exported
  public boolean isAuthenticated() { return auth().isAuthenticated(); }
  
  @Exported
  public boolean isAnonymous() { return Functions.isAnonymous(); }
  
  public String getDetails() { return (auth().getDetails() != null) ? auth().getDetails().toString() : null; }
  
  public String getToString() { return auth().toString(); }
  
  @NonNull
  private Authentication auth() { return Jenkins.getAuthentication2(); }
  
  @Exported
  public String[] getAuthorities() {
    if (auth().getAuthorities() == null)
      return MemoryReductionUtil.EMPTY_STRING_ARRAY; 
    List<String> authorities = new ArrayList<String>();
    for (GrantedAuthority a : auth().getAuthorities())
      authorities.add(a.getAuthority()); 
    return (String[])authorities.toArray(new String[0]);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isHeaderDangerous(@NonNull String name) { return dangerousHeaders.contains(name.toLowerCase(Locale.ENGLISH)); }
  
  public String getIconFileName() { return null; }
  
  public String getDisplayName() { return "Who Am I"; }
  
  public String getUrlName() { return "whoAmI"; }
}
