package jenkins.install;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import jakarta.inject.Provider;
import java.util.List;

public abstract class InstallStateFilter implements ExtensionPoint {
  public abstract InstallState getNextInstallState(InstallState paramInstallState, Provider<InstallState> paramProvider);
  
  public static List<InstallStateFilter> all() { return ExtensionList.lookup(InstallStateFilter.class); }
}
