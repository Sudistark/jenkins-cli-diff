package hudson;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class PluginManagerStaplerOverride implements ExtensionPoint {
  @NonNull
  public static ExtensionList<PluginManagerStaplerOverride> all() { return ExtensionList.lookup(PluginManagerStaplerOverride.class); }
}
