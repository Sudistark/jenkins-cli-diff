package jenkins.views;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import java.util.Optional;
import java.util.function.Supplier;
import org.kohsuke.accmod.Restricted;

public abstract class Header implements ExtensionPoint {
  public boolean isAvailable() { return (isCompatible() && isEnabled()); }
  
  public abstract boolean isCompatible();
  
  public abstract boolean isEnabled();
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static Header get() {
    header = ExtensionList.lookup(Header.class).stream().filter(Header::isAvailable).findFirst();
    return (Header)header.orElseGet(JenkinsHeader::new);
  }
}
