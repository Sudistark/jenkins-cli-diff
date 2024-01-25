package hudson.util;

import java.util.concurrent.ThreadFactory;

public class ClassLoaderSanityThreadFactory implements ThreadFactory {
  private final ThreadFactory delegate;
  
  public ClassLoaderSanityThreadFactory(ThreadFactory delegate) { this.delegate = delegate; }
  
  public Thread newThread(Runnable r) {
    Thread t = this.delegate.newThread(r);
    t.setContextClassLoader(ClassLoaderSanityThreadFactory.class.getClassLoader());
    return t;
  }
}
