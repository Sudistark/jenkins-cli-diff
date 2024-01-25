package jenkins.model.lazy;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import jenkins.util.MemoryReductionUtil;
import org.kohsuke.accmod.Restricted;

public abstract class AbstractLazyLoadRunMap<R> extends AbstractMap<Integer, R> implements SortedMap<Integer, R> {
  private LazyLoadRunMapEntrySet<R> entrySet;
  
  protected File dir;
  
  public Set<Integer> keySet() {
    Object object = this.keySet;
    if (object == null) {
      object = new Object(this);
      this.keySet = object;
    } 
    return object;
  }
  
  public Collection<R> values() {
    Object object = this.values;
    if (object == null) {
      object = new Object(this);
      this.values = object;
    } 
    return object;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  protected AbstractLazyLoadRunMap(File dir) {
    this.index = new Index(this);
    this.entrySet = new LazyLoadRunMapEntrySet(this);
    this.numberOnDisk = new SortedIntList(0);
    initBaseDir(dir);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  protected void initBaseDir(File dir) {
    assert this.dir == null;
    this.dir = dir;
    if (dir != null)
      loadNumberOnDisk(); 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public final boolean baseDirInitialized() { return (this.dir != null); }
  
  public final void updateBaseDir(File dir) { this.dir = dir; }
  
  public void purgeCache() {
    this.index = new Index(this);
    this.fullyLoaded = false;
    loadNumberOnDisk();
  }
  
  private static final Pattern BUILD_NUMBER = Pattern.compile("[0-9]+");
  
  private void loadNumberOnDisk() {
    String[] kids = this.dir.list();
    if (kids == null)
      kids = MemoryReductionUtil.EMPTY_STRING_ARRAY; 
    SortedIntList list = new SortedIntList(kids.length / 2);
    for (String s : kids) {
      if (BUILD_NUMBER.matcher(s).matches())
        try {
          list.add(Integer.parseInt(s));
        } catch (NumberFormatException numberFormatException) {} 
    } 
    list.sort();
    this.numberOnDisk = list;
  }
  
  public Comparator<? super Integer> comparator() { return Collections.reverseOrder(); }
  
  public boolean isEmpty() { return (search(2147483647, Direction.DESC) == null); }
  
  public Set<Map.Entry<Integer, R>> entrySet() {
    assert baseDirInitialized();
    return this.entrySet;
  }
  
  public SortedMap<Integer, R> getLoadedBuilds() { return Collections.unmodifiableSortedMap(new BuildReferenceMapAdapter(this, this.index.byNumber)); }
  
  public SortedMap<Integer, R> subMap(Integer fromKey, Integer toKey) {
    R start = (R)search(fromKey.intValue(), Direction.DESC);
    if (start == null)
      return EMPTY_SORTED_MAP; 
    R end = (R)search(toKey.intValue(), Direction.ASC);
    if (end == null)
      return EMPTY_SORTED_MAP; 
    for (R i = start; i != end; ) {
      i = (R)search(getNumberOf(i) - 1, Direction.DESC);
      assert i != null;
    } 
    return Collections.unmodifiableSortedMap(new BuildReferenceMapAdapter(this, this.index.byNumber.subMap(fromKey, toKey)));
  }
  
  public SortedMap<Integer, R> headMap(Integer toKey) { return subMap(Integer.valueOf(2147483647), toKey); }
  
  public SortedMap<Integer, R> tailMap(Integer fromKey) { return subMap(fromKey, Integer.valueOf(-2147483648)); }
  
  public Integer firstKey() {
    R r = (R)newestBuild();
    if (r == null)
      throw new NoSuchElementException(); 
    return Integer.valueOf(getNumberOf(r));
  }
  
  public Integer lastKey() {
    R r = (R)oldestBuild();
    if (r == null)
      throw new NoSuchElementException(); 
    return Integer.valueOf(getNumberOf(r));
  }
  
  public R newestBuild() { return (R)search(2147483647, Direction.DESC); }
  
  public R oldestBuild() { return (R)search(-2147483648, Direction.ASC); }
  
  public R get(Object key) {
    if (key instanceof Integer) {
      int n = ((Integer)key).intValue();
      return (R)get(n);
    } 
    return (R)super.get(key);
  }
  
  public R get(int n) { return (R)getByNumber(n); }
  
  public boolean runExists(int number) { return this.numberOnDisk.contains(number); }
  
  @CheckForNull
  public R search(int n, Direction d) {
    Iterator iterator1;
    ListIterator<Integer> iterator;
    switch (null.$SwitchMap$jenkins$model$lazy$AbstractLazyLoadRunMap$Direction[d.ordinal()]) {
      case 1:
        return (R)getByNumber(n);
      case 2:
        for (iterator1 = this.numberOnDisk.iterator(); iterator1.hasNext(); ) {
          int m = ((Integer)iterator1.next()).intValue();
          if (m < n)
            continue; 
          R r = (R)getByNumber(m);
          if (r != null)
            return r; 
        } 
        return null;
      case 3:
        iterator = this.numberOnDisk.listIterator(this.numberOnDisk.size());
        while (iterator.hasPrevious()) {
          int m = ((Integer)iterator.previous()).intValue();
          if (m > n)
            continue; 
          R r = (R)getByNumber(m);
          if (r != null)
            return r; 
        } 
        return null;
    } 
    throw new AssertionError();
  }
  
  public R getById(String id) { return (R)getByNumber(Integer.parseInt(id)); }
  
  public R getByNumber(int n) {
    Index snapshot = this.index;
    if (snapshot.byNumber.containsKey(Integer.valueOf(n))) {
      BuildReference<R> ref = (BuildReference)snapshot.byNumber.get(Integer.valueOf(n));
      if (ref == null)
        return null; 
      R v = (R)unwrap(ref);
      if (v != null)
        return v; 
    } 
    synchronized (this) {
      if (this.index.byNumber.containsKey(Integer.valueOf(n))) {
        BuildReference<R> ref = (BuildReference)this.index.byNumber.get(Integer.valueOf(n));
        if (ref == null)
          return null; 
        R v = (R)unwrap(ref);
        if (v != null)
          return v; 
      } 
      return (R)load(n, null);
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public int maxNumberOnDisk() { return this.numberOnDisk.max(); }
  
  protected final void proposeNewNumber(int number) throws IllegalStateException {
    if (number <= maxNumberOnDisk())
      throw new IllegalStateException("JENKINS-27530: cannot create a build with number " + number + " since that (or higher) is already in use among " + this.numberOnDisk); 
  }
  
  public R put(R value) { return (R)_put(value); }
  
  protected R _put(R value) { return (R)put(Integer.valueOf(getNumberOf(value)), value); }
  
  public R put(Integer key, R r) {
    int n = getNumberOf(r);
    Index copy = copy();
    BuildReference<R> ref = createReference(r);
    BuildReference<R> old = (BuildReference)copy.byNumber.put(Integer.valueOf(n), ref);
    this.index = copy;
    if (!this.numberOnDisk.contains(n)) {
      SortedIntList a = new SortedIntList(this.numberOnDisk);
      a.add(n);
      a.sort();
      this.numberOnDisk = a;
    } 
    this.entrySet.clearCache();
    return (R)unwrap(old);
  }
  
  private R unwrap(BuildReference<R> ref) { return (R)((ref != null) ? ref.get() : null); }
  
  public void putAll(Map<? extends Integer, ? extends R> rhs) {
    Index copy = copy();
    for (R r : rhs.values()) {
      BuildReference<R> ref = createReference(r);
      copy.byNumber.put(Integer.valueOf(getNumberOf(r)), ref);
    } 
    this.index = copy;
  }
  
  TreeMap<Integer, BuildReference<R>> all() {
    if (!this.fullyLoaded)
      synchronized (this) {
        if (!this.fullyLoaded) {
          Index copy = copy();
          for (Integer number : this.numberOnDisk) {
            if (!copy.byNumber.containsKey(number))
              load(number.intValue(), copy); 
          } 
          this.index = copy;
          this.fullyLoaded = true;
        } 
      }  
    return this.index.byNumber;
  }
  
  private Index copy() { return new Index(this, this.index); }
  
  private R load(int n, Index editInPlace) {
    assert Thread.holdsLock(this);
    assert this.dir != null;
    R v = (R)load(new File(this.dir, String.valueOf(n)), editInPlace);
    if (v == null && editInPlace != null)
      editInPlace.byNumber.put(Integer.valueOf(n), null); 
    return v;
  }
  
  private R load(File dataDir, Index editInPlace) {
    assert Thread.holdsLock(this);
    try {
      R r = (R)retrieve(dataDir);
      if (r == null)
        return null; 
      Index copy = (editInPlace != null) ? editInPlace : new Index(this, this.index);
      BuildReference<R> ref = createReference(r);
      BuildReference<R> old = (BuildReference)copy.byNumber.put(Integer.valueOf(getNumberOf(r)), ref);
      assert old == null || old.get() == null : "tried to overwrite " + old + " with " + ref;
      if (editInPlace == null)
        this.index = copy; 
      return r;
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Failed to load " + dataDir, e);
      return null;
    } 
  }
  
  protected String getIdOf(R r) { return String.valueOf(getNumberOf(r)); }
  
  protected BuildReference<R> createReference(R r) { return new BuildReference(getIdOf(r), r); }
  
  public boolean removeValue(R run) {
    Index copy = copy();
    int n = getNumberOf(run);
    BuildReference<R> old = (BuildReference)copy.byNumber.remove(Integer.valueOf(n));
    SortedIntList a = new SortedIntList(this.numberOnDisk);
    a.removeValue(n);
    this.numberOnDisk = a;
    this.index = copy;
    this.entrySet.clearCache();
    return (old != null);
  }
  
  public void reset(TreeMap<Integer, R> builds) {
    Index index = new Index(this);
    for (R r : builds.values()) {
      BuildReference<R> ref = createReference(r);
      index.byNumber.put(Integer.valueOf(getNumberOf(r)), ref);
    } 
    this.index = index;
  }
  
  public int hashCode() { return System.identityHashCode(this); }
  
  public boolean equals(Object o) { return (o == this); }
  
  private static final SortedMap EMPTY_SORTED_MAP = Collections.unmodifiableSortedMap(new TreeMap());
  
  static final Logger LOGGER = Logger.getLogger(AbstractLazyLoadRunMap.class.getName());
  
  protected abstract int getNumberOf(R paramR);
  
  protected abstract R retrieve(File paramFile) throws IOException;
}
