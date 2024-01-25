package jenkins;

import hudson.init.InitMilestone;
import hudson.security.ACL;
import hudson.util.DaemonThreadFactory;
import hudson.util.NamingThreadFactory;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import jenkins.model.Jenkins;
import jenkins.security.ImpersonatingExecutorService;
import jenkins.util.SystemProperties;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorException;
import org.jvnet.hudson.reactor.ReactorListener;
import org.jvnet.hudson.reactor.Task;
import org.kohsuke.accmod.Restricted;

public class InitReactorRunner {
  public void run(Reactor reactor) throws InterruptedException, ReactorException, IOException {
    reactor.addAll(InitMilestone.ordering().discoverTasks(reactor));
    if (Jenkins.PARALLEL_LOAD) {
      es = new ThreadPoolExecutor(TWICE_CPU_NUM, TWICE_CPU_NUM, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new DaemonThreadFactory());
    } else {
      es = Executors.newSingleThreadExecutor(new NamingThreadFactory(new DaemonThreadFactory(), "InitReactorRunner"));
    } 
    try {
      reactor.execute(new ImpersonatingExecutorService(es, ACL.SYSTEM2), buildReactorListener());
    } finally {
      es.shutdownNow();
    } 
  }
  
  private ReactorListener buildReactorListener() throws IOException {
    List<ReactorListener> r = (List)StreamSupport.stream(ServiceLoader.load(hudson.init.InitReactorListener.class, Thread.currentThread().getContextClassLoader()).spliterator(), false).collect(Collectors.toList());
    r.add(new Object(this));
    return new ReactorListener.Aggregator(r);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String getDisplayName(Task t) {
    try {
      return t.getDisplayName();
    } catch (RuntimeException|Error x) {
      LOGGER.log(Level.WARNING, "failed to find displayName of " + t, x);
      return t.toString();
    } 
  }
  
  protected void onInitMilestoneAttained(InitMilestone milestone) {}
  
  private static final int TWICE_CPU_NUM = SystemProperties.getInteger(InitReactorRunner.class
      .getName() + ".concurrency", 
      Integer.valueOf(Runtime.getRuntime().availableProcessors() * 2)).intValue();
  
  private static final Logger LOGGER = Logger.getLogger(InitReactorRunner.class.getName());
}
