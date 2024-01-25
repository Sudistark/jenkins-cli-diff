package hudson.util;

public final class OneShotEvent {
  private boolean signaled;
  
  private final Object lock;
  
  public OneShotEvent() { this.lock = this; }
  
  public OneShotEvent(Object lock) { this.lock = lock; }
  
  public void signal() {
    synchronized (this.lock) {
      if (this.signaled)
        return; 
      this.signaled = true;
      this.lock.notifyAll();
    } 
  }
  
  public void block() {
    synchronized (this.lock) {
      while (!this.signaled)
        this.lock.wait(); 
    } 
  }
  
  public void block(long timeout) throws InterruptedException {
    synchronized (this.lock) {
      if (!this.signaled)
        this.lock.wait(timeout); 
    } 
  }
  
  public boolean isSignaled() {
    synchronized (this.lock) {
      return this.signaled;
    } 
  }
}
