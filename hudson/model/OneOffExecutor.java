package hudson.model;

public class OneOffExecutor extends Executor {
  public OneOffExecutor(Computer owner) { super(owner, -1); }
}
