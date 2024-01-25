package hudson.views;

import hudson.model.Descriptor;

public abstract class ListViewColumnDescriptor extends Descriptor<ListViewColumn> {
  public boolean shownByDefault() { return true; }
}
