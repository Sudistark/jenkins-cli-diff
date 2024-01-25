package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class CheckPoint {
  private final Object identity;
  
  private final String internalName;
  
  public CheckPoint(String internalName, Object identity) {
    this.internalName = internalName;
    this.identity = identity;
  }
  
  public CheckPoint(String internalName) { this(internalName, new Object()); }
  
  public boolean equals(Object that) {
    if (that == null || getClass() != that.getClass())
      return false; 
    return (this.identity == ((CheckPoint)that).identity);
  }
  
  public int hashCode() { return this.identity.hashCode(); }
  
  public String toString() { return "Check point " + this.internalName; }
  
  public void report() { Run.reportCheckpoint(this); }
  
  public void block() { Run.waitForCheckpoint(this, null, null); }
  
  public void block(@NonNull BuildListener listener, @NonNull String waiter) throws InterruptedException { Run.waitForCheckpoint(this, listener, waiter); }
  
  public static final CheckPoint CULPRITS_DETERMINED = new CheckPoint("CULPRITS_DETERMINED");
  
  public static final CheckPoint COMPLETED = new CheckPoint("COMPLETED");
  
  public static final CheckPoint MAIN_COMPLETED = new CheckPoint("MAIN_COMPLETED");
}
