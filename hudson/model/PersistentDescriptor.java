package hudson.model;

import jakarta.annotation.PostConstruct;

public interface PersistentDescriptor extends Saveable {
  @PostConstruct
  void load();
}
