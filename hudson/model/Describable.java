package hudson.model;

public interface Describable<T extends Describable<T>> {
  Descriptor<T> getDescriptor();
}
