package hudson.model.queue;

import hudson.AbortException;

class Latch {
  private final int n;
  
  private int i;
  
  private Exception interrupted;
  
  Latch(int n) {
    this.i = 0;
    this.n = n;
  }
  
  public void abort(Throwable cause) {
    this.interrupted = new AbortException();
    if (cause != null)
      this.interrupted.initCause(cause); 
    notifyAll();
  }
  
  public void synchronize() throws InterruptedException {
    check(this.n);
    try {
      onCriteriaMet();
    } catch (Error|RuntimeException e) {
      abort(e);
      throw e;
    } 
    check(this.n * 2);
  }
  
  private void check(int threshold) {
    this.i++;
    if (this.i == threshold) {
      notifyAll();
    } else {
      while (this.i < threshold && this.interrupted == null) {
        try {
          wait();
        } catch (InterruptedException e) {
          this.interrupted = e;
          notifyAll();
          throw e;
        } 
      } 
    } 
    if (this.interrupted != null)
      throw (InterruptedException)(new InterruptedException()).initCause(this.interrupted); 
  }
  
  protected void onCriteriaMet() throws InterruptedException {}
}
