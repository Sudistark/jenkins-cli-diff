package hudson.model;

import hudson.Functions;
import hudson.console.AnnotatedLargeText;
import java.io.IOException;
import java.io.Reader;
import java.lang.ref.WeakReference;
import org.kohsuke.stapler.framework.io.LargeText;

public abstract class TaskThread extends Thread {
  @Deprecated
  private final LargeText text;
  
  private final AnnotatedLargeText<TaskAction> log;
  
  private TaskListener listener;
  
  private final TaskAction owner;
  
  protected TaskThread(TaskAction owner, ListenerAndText output) {
    super(owner.getDisplayName());
    this.owner = owner;
    this.text = this.log = output.text;
    this.listener = output.listener;
    this.isRunning = true;
  }
  
  public Reader readAll() throws IOException { return this.text.readAll(); }
  
  protected final void associateWith(TaskAction action) {
    action.workerThread = this;
    action.log = new WeakReference(this.log);
  }
  
  public void start() {
    associateWith(this.owner);
    super.start();
  }
  
  public boolean isRunning() { return this.isRunning; }
  
  protected ListenerAndText createListener() throws IOException { return ListenerAndText.forMemory(); }
  
  public final void run() {
    this.isRunning = true;
    try {
      perform(this.listener);
      this.listener.getLogger().println("Completed");
      this.owner.workerThread = null;
    } catch (InterruptedException e) {
      this.listener.getLogger().println("Aborted");
    } catch (Exception e) {
      Functions.printStackTrace(e, this.listener.getLogger());
    } finally {
      this.listener = null;
      this.isRunning = false;
    } 
    this.log.markAsComplete();
  }
  
  protected abstract void perform(TaskListener paramTaskListener) throws Exception;
}
