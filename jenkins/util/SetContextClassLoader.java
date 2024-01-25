package jenkins.util;

public final class SetContextClassLoader implements AutoCloseable {
  private final Thread t;
  
  private final ClassLoader orig;
  
  public SetContextClassLoader() { this(StackWalker.getInstance().getCallerClass()); }
  
  public SetContextClassLoader(Class<?> clazz) { this(clazz.getClassLoader()); }
  
  public SetContextClassLoader(ClassLoader cl) {
    this.t = Thread.currentThread();
    this.orig = this.t.getContextClassLoader();
    this.t.setContextClassLoader(cl);
  }
  
  public void close() { this.t.setContextClassLoader(this.orig); }
}
