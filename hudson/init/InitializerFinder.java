package hudson.init;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Reactor;

public class InitializerFinder extends TaskMethodFinder<Initializer> {
  public InitializerFinder(ClassLoader cl) { super(Initializer.class, InitMilestone.class, cl); }
  
  public InitializerFinder() { this(Thread.currentThread().getContextClassLoader()); }
  
  protected String displayNameOf(Initializer i) { return i.displayName(); }
  
  protected String[] requiresOf(Initializer i) { return i.requires(); }
  
  protected String[] attainsOf(Initializer i) { return i.attains(); }
  
  protected Milestone afterOf(Initializer i) { return i.after(); }
  
  protected Milestone beforeOf(Initializer i) { return i.before(); }
  
  protected boolean fatalOf(Initializer i) { return i.fatal(); }
}
