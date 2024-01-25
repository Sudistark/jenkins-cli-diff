package hudson.cli;

public abstract class CloneableCLICommand extends CLICommand implements Cloneable {
  protected CLICommand createClone() {
    try {
      return (CLICommand)clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    } 
  }
}
