package hudson.slaves;

public abstract class AbstractCloudImpl extends Cloud {
  private int instanceCap;
  
  protected AbstractCloudImpl(String name, String instanceCapStr) {
    super(name);
    setInstanceCapStr(instanceCapStr);
  }
  
  protected void setInstanceCapStr(String value) {
    if (value == null || value.isEmpty()) {
      this.instanceCap = Integer.MAX_VALUE;
    } else {
      this.instanceCap = Integer.parseInt(value);
    } 
  }
  
  public String getInstanceCapStr() {
    if (this.instanceCap == Integer.MAX_VALUE)
      return ""; 
    return String.valueOf(this.instanceCap);
  }
  
  public int getInstanceCap() { return this.instanceCap; }
  
  protected void setInstanceCap(int v) { this.instanceCap = v; }
}
