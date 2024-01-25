package hudson;

import java.util.concurrent.ConcurrentHashMap;

public class Lookup {
  private final ConcurrentHashMap<Class, Object> data = new ConcurrentHashMap();
  
  public <T> T get(Class<T> type) { return (T)type.cast(this.data.get(type)); }
  
  public <T> T set(Class<T> type, T instance) { return (T)type.cast(this.data.put(type, instance)); }
  
  public <T> T setIfNull(Class<T> type, T instance) {
    Object o = this.data.putIfAbsent(type, instance);
    if (o != null)
      return (T)type.cast(o); 
    return instance;
  }
}
