package hudson.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class NamingThreadFactory implements ThreadFactory {
  private final AtomicInteger threadNum;
  
  private final ThreadFactory delegate;
  
  private final String name;
  
  public NamingThreadFactory(ThreadFactory delegate, String name) {
    this.threadNum = new AtomicInteger();
    this.delegate = delegate;
    this.name = name;
  }
  
  public Thread newThread(Runnable r) {
    Thread t = this.delegate.newThread(r);
    t.setName(this.name + " [#" + this.name + "]");
    return t;
  }
}
