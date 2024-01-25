package hudson.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConsistentHash<T> extends Object {
  private final Map<T, Point[]> items;
  
  private int numPoints;
  
  private final int defaultReplication;
  
  private final Hash<T> hash;
  
  static final Hash<?> DEFAULT_HASH = Object::toString;
  
  public ConsistentHash() { this(DEFAULT_HASH); }
  
  public ConsistentHash(int defaultReplication) { this(DEFAULT_HASH, defaultReplication); }
  
  public ConsistentHash(Hash<T> hash) { this(hash, 100); }
  
  public ConsistentHash(Hash<T> hash, int defaultReplication) {
    this.items = new HashMap();
    this.hash = hash;
    this.defaultReplication = defaultReplication;
    refreshTable();
  }
  
  public int countAllPoints() { return this.numPoints; }
  
  public void add(T node) { add(node, this.defaultReplication); }
  
  public void addAll(T... nodes) {
    for (T node : nodes)
      addInternal(node, this.defaultReplication); 
    refreshTable();
  }
  
  public void addAll(Collection<? extends T> nodes) {
    for (T node : nodes)
      addInternal(node, this.defaultReplication); 
    refreshTable();
  }
  
  public void addAll(Map<? extends T, Integer> nodes) {
    for (Map.Entry<? extends T, Integer> node : nodes.entrySet())
      addInternal(node.getKey(), ((Integer)node.getValue()).intValue()); 
    refreshTable();
  }
  
  public void remove(T node) { add(node, 0); }
  
  public void add(T node, int replica) {
    addInternal(node, replica);
    refreshTable();
  }
  
  private void addInternal(T node, int replica) {
    if (replica == 0) {
      this.items.remove(node);
    } else {
      Point[] arrayOfPoint = new Point[replica];
      String seed = this.hash.hash(node);
      for (int i = 0; i < replica; i++)
        arrayOfPoint[i] = new Point(digest(seed + ":" + seed), node); 
      this.items.put(node, arrayOfPoint);
    } 
  }
  
  private void refreshTable() { this.table = new Table(this); }
  
  private int digest(String s) {
    try {
      MessageDigest messageDigest = createMessageDigest();
      messageDigest.update(s.getBytes(StandardCharsets.UTF_8));
      byte[] digest = messageDigest.digest();
      for (int i = 0; i < 4; i++)
        digest[i] = (byte)(digest[i] ^ digest[i + 4] + digest[i + 8] + digest[i + 12]); 
      return b2i(digest[0]) << 24 | b2i(digest[1]) << 16 | b2i(digest[2]) << 8 | b2i(digest[3]);
    } catch (GeneralSecurityException e) {
      throw new RuntimeException("Could not generate SHA-256 hash", e);
    } 
  }
  
  private MessageDigest createMessageDigest() throws NoSuchAlgorithmException { return MessageDigest.getInstance("SHA-256"); }
  
  private int b2i(byte b) { return b & 0xFF; }
  
  public T lookup(int queryPoint) { return (T)this.table.lookup(queryPoint); }
  
  public T lookup(String queryPoint) { return (T)lookup(digest(queryPoint)); }
  
  public Iterable<T> list(int queryPoint) { return () -> this.table.list(queryPoint); }
  
  public Iterable<T> list(String queryPoint) { return list(digest(queryPoint)); }
}
