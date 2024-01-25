package hudson.model;

import hudson.ExtensionPoint;

public interface TopLevelItem extends Item, ExtensionPoint, Describable<TopLevelItem> {
  TopLevelItemDescriptor getDescriptor();
}
