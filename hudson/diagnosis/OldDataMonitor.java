package hudson.diagnosis;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.Main;
import hudson.model.AdministrativeMonitor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.Saveable;
import hudson.model.listeners.ItemListener;
import hudson.model.listeners.RunListener;
import hudson.model.listeners.SaveableListener;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.RobustReflectionConverter;
import hudson.util.VersionNumber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

@Extension
@Symbol({"oldData"})
public class OldDataMonitor extends AdministrativeMonitor {
  private static final Logger LOGGER = Logger.getLogger(OldDataMonitor.class.getName());
  
  private ConcurrentMap<SaveableReference, VersionRange> data = new ConcurrentHashMap();
  
  @NonNull
  static OldDataMonitor get(Jenkins j) throws IllegalStateException { return (OldDataMonitor)ExtensionList.lookupSingleton(OldDataMonitor.class); }
  
  public OldDataMonitor() { super("OldData"); }
  
  public String getDisplayName() { return Messages.OldDataMonitor_DisplayName(); }
  
  public boolean isActivated() { return !this.data.isEmpty(); }
  
  public Map<Saveable, VersionRange> getData() {
    Map<Saveable, VersionRange> r = new HashMap<Saveable, VersionRange>();
    for (Map.Entry<SaveableReference, VersionRange> entry : this.data.entrySet()) {
      Saveable s = ((SaveableReference)entry.getKey()).get();
      if (s != null)
        r.put(s, (VersionRange)entry.getValue()); 
    } 
    return r;
  }
  
  private static void remove(Saveable obj, boolean isDelete) {
    Jenkins j = Jenkins.get();
    OldDataMonitor odm = get(j);
    ACLContext ctx = ACL.as2(ACL.SYSTEM2);
    try {
      odm.data.remove(referTo(obj));
      if (isDelete && obj instanceof Job)
        for (Run r : ((Job)obj).getBuilds())
          odm.data.remove(referTo(r));  
      if (ctx != null)
        ctx.close(); 
    } catch (Throwable throwable) {
      if (ctx != null)
        try {
          ctx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  @Extension
  public static final SaveableListener changeListener = new Object();
  
  @Extension
  public static final ItemListener itemDeleteListener = new Object();
  
  @Extension
  public static final RunListener<Run> runDeleteListener = new Object();
  
  public static void report(Saveable obj, String version) {
    OldDataMonitor odm = get(Jenkins.get());
    try {
      SaveableReference ref = referTo(obj);
      do {
        VersionRange vr = (VersionRange)odm.data.get(ref);
        if (vr != null && odm.data.replace(ref, vr, new VersionRange(vr, version, null)))
          break; 
      } while (odm.data.putIfAbsent(ref, new VersionRange(null, version, null)) != null);
    } catch (IllegalArgumentException ex) {
      LOGGER.log(Level.WARNING, "Bad parameter given to OldDataMonitor", ex);
    } 
  }
  
  public static void report(UnmarshallingContext context, String version) { RobustReflectionConverter.addErrorInContext(context, new ReportException(version)); }
  
  public static void report(Saveable obj, Collection<Throwable> errors) {
    StringBuilder buf = new StringBuilder();
    int i = 0;
    for (Throwable e : errors) {
      if (e instanceof ReportException) {
        report(obj, ((ReportException)e).version);
        continue;
      } 
      if (Main.isUnitTest)
        LOGGER.log(Level.INFO, "Trouble loading " + obj, e); 
      if (++i > 1)
        buf.append(", "); 
      buf.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
    } 
    if (buf.length() == 0)
      return; 
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j == null) {
      for (Throwable t : errors)
        LOGGER.log(Level.WARNING, "could not read " + obj + " (and Jenkins did not start up)", t); 
      return;
    } 
    OldDataMonitor odm = get(j);
    SaveableReference ref = referTo(obj);
    do {
      VersionRange vr = (VersionRange)odm.data.get(ref);
      if (vr != null && odm.data.replace(ref, vr, new VersionRange(vr, null, buf.toString())))
        break; 
    } while (odm.data.putIfAbsent(ref, new VersionRange(null, null, buf.toString())) != null);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Iterator<VersionNumber> getVersionList() {
    TreeSet<VersionNumber> set = new TreeSet<VersionNumber>();
    for (VersionRange vr : this.data.values()) {
      if (vr.max != null)
        set.add(vr.max); 
    } 
    return set.iterator();
  }
  
  @RequirePOST
  public HttpResponse doAct(StaplerRequest req, StaplerResponse rsp) throws IOException {
    if (req.hasParameter("no")) {
      disable(true);
      return HttpResponses.redirectViaContextPath("/manage");
    } 
    return new HttpRedirect("manage");
  }
  
  @RequirePOST
  public HttpResponse doUpgrade(StaplerRequest req, StaplerResponse rsp) throws IOException {
    String thruVerParam = req.getParameter("thruVer");
    VersionNumber thruVer = thruVerParam.equals("all") ? null : new VersionNumber(thruVerParam);
    saveAndRemoveEntries(entry -> {
          VersionNumber version = ((VersionRange)entry.getValue()).max;
          return (version != null && (thruVer == null || !version.isNewerThan(thruVer)));
        });
    return HttpResponses.forwardToPreviousPage();
  }
  
  @RequirePOST
  public HttpResponse doDiscard(StaplerRequest req, StaplerResponse rsp) throws IOException {
    saveAndRemoveEntries(entry -> (((VersionRange)entry.getValue()).max == null));
    return HttpResponses.forwardToPreviousPage();
  }
  
  private void saveAndRemoveEntries(Predicate<Map.Entry<SaveableReference, VersionRange>> matchingPredicate) {
    List<SaveableReference> removed = new ArrayList<SaveableReference>();
    for (Map.Entry<SaveableReference, VersionRange> entry : this.data.entrySet()) {
      if (matchingPredicate.test(entry)) {
        Saveable s = ((SaveableReference)entry.getKey()).get();
        if (s != null)
          try {
            s.save();
          } catch (Exception x) {
            LOGGER.log(Level.WARNING, "failed to save " + s, x);
          }  
        removed.add((SaveableReference)entry.getKey());
      } 
    } 
    this.data.keySet().removeAll(removed);
  }
  
  public HttpResponse doIndex(StaplerResponse rsp) throws IOException { return new HttpRedirect("manage"); }
  
  private static SaveableReference referTo(Saveable s) {
    if (s instanceof Run) {
      Job parent = ((Run)s).getParent();
      if (Jenkins.get().getItemByFullName(parent.getFullName()) == parent)
        return new RunSaveableReference((Run)s); 
    } 
    return new SimpleSaveableReference(s);
  }
}
