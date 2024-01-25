package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class KeyedDataStorage<T, P> extends Object {
  private final ConcurrentHashMap<String, Object> core = new ConcurrentHashMap();
  
  @NonNull
  public T getOrCreate(String key, P createParams) throws IOException { return (T)get(key, true, createParams); }
  
  @CheckForNull
  public T get(String key) throws IOException { return (T)get(key, false, null); }
  
  @CheckForNull
  protected T get(@NonNull String key, boolean createIfNotExist, P createParams) throws IOException {
    while (true) {
      this.totalQuery.incrementAndGet();
      Object value = this.core.get(key);
      if (value instanceof SoftReference) {
        SoftReference<T> wfp = (SoftReference)value;
        T t = (T)wfp.get();
        if (t != null) {
          this.cacheHit.incrementAndGet();
          return t;
        } 
        this.weakRefLost.incrementAndGet();
      } 
      if (value instanceof Loading) {
        T t = (T)((Loading)value).get();
        if (t != null || !createIfNotExist)
          return t; 
      } 
      l = new Loading<T>();
      if ((value == null) ? (this.core.putIfAbsent(key, l) != null) : !this.core.replace(key, value, l))
        continue; 
      break;
    } 
    t = null;
    try {
      t = (T)load(key);
      if (t == null && createIfNotExist) {
        t = (T)create(key, createParams);
        if (t == null)
          throw new IllegalStateException("Bug in the derived class"); 
      } 
    } catch (IOException e) {
      this.loadFailure.incrementAndGet();
      throw e;
    } finally {
      l.set(t);
    } 
    if (t != null) {
      this.core.put(key, new SoftReference(t));
    } else {
      this.core.remove(key);
    } 
    return t;
  }
  
  @CheckForNull
  protected abstract T load(String paramString) throws IOException;
  
  @NonNull
  protected abstract T create(@NonNull String paramString, @NonNull P paramP) throws IOException;
  
  public void resetPerformanceStats() {
    this.totalQuery.set(0);
    this.cacheHit.set(0);
    this.weakRefLost.set(0);
    this.loadFailure.set(0);
  }
  
  public String getPerformanceStats() {
    int total = this.totalQuery.get();
    int hit = this.cacheHit.get();
    int weakRef = this.weakRefLost.get();
    int failure = this.loadFailure.get();
    int miss = total - hit - weakRef;
    return MessageFormat.format("total={0} hit={1}% lostRef={2}% failure={3}% miss={4}%", new Object[] { Integer.valueOf(total), Integer.valueOf(hit), Integer.valueOf(weakRef), Integer.valueOf(failure), Integer.valueOf(miss) });
  }
  
  public final AtomicInteger totalQuery = new AtomicInteger();
  
  public final AtomicInteger cacheHit = new AtomicInteger();
  
  public final AtomicInteger weakRefLost = new AtomicInteger();
  
  public final AtomicInteger loadFailure = new AtomicInteger();
}
