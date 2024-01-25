package jenkins.util.groovy;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import groovy.lang.GroovyObjectSupport;
import lib.FormTagLib;
import lib.JenkinsTagLib;
import lib.LayoutTagLib;
import org.kohsuke.stapler.jelly.groovy.JellyBuilder;
import org.kohsuke.stapler.jelly.groovy.Namespace;

public abstract class AbstractGroovyViewModule extends GroovyObjectSupport {
  public JellyBuilder builder;
  
  @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "read by Stapler")
  public FormTagLib f;
  
  @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "read by Stapler")
  public LayoutTagLib l;
  
  @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "read by Stapler")
  public JenkinsTagLib t;
  
  @SuppressFBWarnings(value = {"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"}, justification = "read by Stapler")
  public Namespace st;
  
  protected AbstractGroovyViewModule(JellyBuilder b) {
    this.builder = b;
    this.f = (FormTagLib)this.builder.namespace(FormTagLib.class);
    this.l = (LayoutTagLib)this.builder.namespace(LayoutTagLib.class);
    this.t = (JenkinsTagLib)this.builder.namespace(JenkinsTagLib.class);
    this.st = this.builder.namespace("jelly:stapler");
  }
  
  public Object methodMissing(String name, Object args) { return this.builder.invokeMethod(name, args); }
  
  public Object propertyMissing(String name) { return this.builder.getProperty(name); }
  
  public void propertyMissing(String name, Object value) { this.builder.setProperty(name, value); }
}
