package hudson;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Node;

public abstract class LauncherDecorator implements ExtensionPoint {
  @NonNull
  public abstract Launcher decorate(@NonNull Launcher paramLauncher, @NonNull Node paramNode);
  
  public static ExtensionList<LauncherDecorator> all() { return ExtensionList.lookup(LauncherDecorator.class); }
}
