package hudson.model;

public interface BuildableItem extends Item, Queue.Task {
  @Deprecated
  default boolean scheduleBuild() { return scheduleBuild(new Cause.LegacyCodeCause()); }
  
  boolean scheduleBuild(Cause paramCause);
  
  @Deprecated
  default boolean scheduleBuild(int quietPeriod) { return scheduleBuild(quietPeriod, new Cause.LegacyCodeCause()); }
  
  boolean scheduleBuild(int paramInt, Cause paramCause);
}
