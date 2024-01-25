package hudson.init;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Reactor;

public class TerminatorFinder extends TaskMethodFinder<Terminator> {
  public TerminatorFinder(ClassLoader cl) { super(Terminator.class, TermMilestone.class, cl); }
  
  protected String displayNameOf(Terminator i) { return i.displayName(); }
  
  protected String[] requiresOf(Terminator i) { return i.requires(); }
  
  protected String[] attainsOf(Terminator i) { return i.attains(); }
  
  protected Milestone afterOf(Terminator i) { return i.after(); }
  
  protected Milestone beforeOf(Terminator i) { return i.before(); }
  
  protected boolean fatalOf(Terminator i) { return false; }
}
