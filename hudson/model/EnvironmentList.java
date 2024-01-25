package hudson.model;

import java.util.AbstractList;
import java.util.List;

public final class EnvironmentList extends AbstractList<Environment> {
  private final List<Environment> base;
  
  public EnvironmentList(List<Environment> base) { this.base = base; }
  
  public Environment get(int index) { return (Environment)this.base.get(index); }
  
  public int size() { return this.base.size(); }
  
  public <T extends Environment> T get(Class<T> type) {
    for (Environment e : this) {
      if (type.isInstance(e))
        return (T)(Environment)type.cast(e); 
    } 
    return null;
  }
  
  public Environment set(int index, Environment element) { return (Environment)this.base.set(index, element); }
  
  public void add(int index, Environment element) { this.base.add(index, element); }
  
  public Environment remove(int index) { return (Environment)this.base.remove(index); }
}
