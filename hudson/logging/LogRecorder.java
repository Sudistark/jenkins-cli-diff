package hudson.logging;

import com.google.common.annotations.VisibleForTesting;
import com.thoughtworks.xstream.XStream;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.BulkChange;
import hudson.RestrictedSince;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.AbstractModelObject;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Computer;
import hudson.model.Saveable;
import hudson.model.listeners.SaveableListener;
import hudson.util.CopyOnWriteList;
import hudson.util.FormValidation;
import hudson.util.HttpResponses;
import hudson.util.RingBufferLogHandler;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.Jenkins;
import jenkins.util.MemoryReductionUtil;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.verb.POST;

public class LogRecorder extends AbstractModelObject implements Saveable {
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @RestrictedSince("2.324")
  public final CopyOnWriteList<Target> targets;
  
  private List<Target> loggers;
  
  private static final TargetComparator TARGET_COMPARATOR = new TargetComparator();
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  RingBufferLogHandler handler;
  
  @DataBoundConstructor
  public LogRecorder(String name) {
    this.targets = new CopyOnWriteList();
    this.loggers = new ArrayList();
    this.handler = new Object(this);
    this.name = name;
    new WeakLogHandler(this.handler, Logger.getLogger(""));
  }
  
  private Object readResolve() {
    if (this.loggers == null)
      this.loggers = new ArrayList(); 
    List<Target> tempLoggers = new ArrayList<Target>(this.loggers);
    if (!this.targets.isEmpty())
      this.loggers.addAll(this.targets.getView()); 
    if (!tempLoggers.isEmpty() && !this.targets.getView().equals(tempLoggers))
      this.targets.addAll(tempLoggers); 
    return this;
  }
  
  public List<Target> getLoggers() { return this.loggers; }
  
  public void setLoggers(List<Target> loggers) { this.loggers = loggers; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  Target[] orderedTargets() {
    Target[] ts = (Target[])this.loggers.toArray(new Target[0]);
    Arrays.sort(ts, TARGET_COMPARATOR);
    return ts;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @VisibleForTesting
  public static Set<String> getAutoCompletionCandidates(List<String> loggerNamesList) {
    Set<String> loggerNames = new HashSet<String>(loggerNamesList);
    HashMap<String, Integer> seenPrefixes = new HashMap<String, Integer>();
    SortedSet<String> relevantPrefixes = new TreeSet<String>();
    for (String loggerName : loggerNames) {
      String[] loggerNameParts = loggerName.split("[.]");
      String longerPrefix = null;
      for (int i = loggerNameParts.length; i > 0; i--) {
        String loggerNamePrefix = String.join(".", (CharSequence[])Arrays.copyOf(loggerNameParts, i));
        seenPrefixes.put(loggerNamePrefix, Integer.valueOf(((Integer)seenPrefixes.getOrDefault(loggerNamePrefix, Integer.valueOf(0))).intValue() + 1));
        if (longerPrefix == null) {
          relevantPrefixes.add(loggerNamePrefix);
          longerPrefix = loggerNamePrefix;
        } else {
          if (((Integer)seenPrefixes.get(loggerNamePrefix)).intValue() > ((Integer)seenPrefixes.get(longerPrefix)).intValue())
            relevantPrefixes.add(loggerNamePrefix); 
          longerPrefix = loggerNamePrefix;
        } 
      } 
    } 
    return relevantPrefixes;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  @VisibleForTesting
  public FormValidation doCheckName(@QueryParameter String value, @QueryParameter String level) {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    try {
      if ((Util.fixEmpty(level) == null || Level.parse(level).intValue() <= Level.FINE.intValue()) && Util.fixEmpty(value) == null)
        return FormValidation.warning(Messages.LogRecorder_Target_Empty_Warning()); 
    } catch (IllegalArgumentException iae) {
      if (Util.fixEmpty(value) == null)
        return FormValidation.warning(Messages.LogRecorder_Target_Empty_Warning()); 
    } 
    return FormValidation.ok();
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public AutoCompletionCandidates doAutoCompleteLoggerName(@QueryParameter String value) {
    if (value == null)
      return new AutoCompletionCandidates(); 
    Set<String> candidateNames = new LinkedHashSet<String>(getAutoCompletionCandidates(Collections.list(LogManager.getLogManager().getLoggerNames())));
    for (String part : value.split("[ ]+")) {
      HashSet<String> partCandidates = new HashSet<String>();
      String lowercaseValue = part.toLowerCase(Locale.ENGLISH);
      for (String loggerName : candidateNames) {
        if (loggerName.toLowerCase(Locale.ENGLISH).contains(lowercaseValue))
          partCandidates.add(loggerName); 
      } 
      candidateNames.retainAll(partCandidates);
    } 
    AutoCompletionCandidates candidates = new AutoCompletionCandidates();
    candidates.add((String[])candidateNames.toArray(MemoryReductionUtil.EMPTY_STRING_ARRAY));
    return candidates;
  }
  
  public String getDisplayName() { return this.name; }
  
  public String getSearchUrl() { return Util.rawEncode(this.name); }
  
  public String getName() { return this.name; }
  
  public LogRecorderManager getParent() { return Jenkins.get().getLog(); }
  
  @POST
  public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    JSONObject src = req.getSubmittedForm();
    String newName = src.getString("name"), redirect = ".";
    XmlFile oldFile = null;
    if (!this.name.equals(newName)) {
      Jenkins.checkGoodName(newName);
      oldFile = getConfigFile();
      List<LogRecorder> recorders = getParent().getRecorders();
      recorders.remove(new LogRecorder(this.name));
      this.name = newName;
      recorders.add(this);
      getParent().setRecorders(recorders);
      redirect = "../" + Util.rawEncode(newName) + "/";
    } 
    List<Target> newTargets = req.bindJSONToList(Target.class, src.get("loggers"));
    setLoggers(newTargets);
    save();
    if (oldFile != null)
      oldFile.delete(); 
    rsp.sendRedirect2(redirect);
  }
  
  @RequirePOST
  public HttpResponse doClear() throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    this.handler.clear();
    return HttpResponses.redirectToDot();
  }
  
  public void load() throws IOException {
    getConfigFile().unmarshal(this);
    this.loggers.forEach(Target::enable);
  }
  
  public void save() throws IOException {
    if (BulkChange.contains(this))
      return; 
    handlePluginUpdatingLegacyLogManagerMap();
    getConfigFile().write(this);
    this.loggers.forEach(Target::enable);
    SaveableListener.fireOnChange(this, getConfigFile());
  }
  
  private void handlePluginUpdatingLegacyLogManagerMap() throws IOException {
    if ((getParent()).logRecorders.size() > getParent().getRecorders().size())
      for (LogRecorder logRecorder : (getParent()).logRecorders.values()) {
        if (!getParent().getRecorders().contains(logRecorder))
          getParent().getRecorders().add(logRecorder); 
      }  
    if (getParent().getRecorders().size() > (getParent()).logRecorders.size())
      for (LogRecorder logRecorder : getParent().getRecorders()) {
        if (!(getParent()).logRecorders.containsKey(logRecorder.getName()))
          (getParent()).logRecorders.put(logRecorder.getName(), logRecorder); 
      }  
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    LogRecorder that = (LogRecorder)o;
    return this.name.equals(that.name);
  }
  
  public int hashCode() { return Objects.hash(new Object[] { this.name }); }
  
  @RequirePOST
  public void doDoDelete(StaplerResponse rsp) throws IOException, ServletException {
    delete();
    rsp.sendRedirect2("..");
  }
  
  public void delete() throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    getConfigFile().delete();
    getParent().getRecorders().remove(new LogRecorder(this.name));
    this.loggers.forEach(Target::disable);
    getParent().getRecorders().forEach(logRecorder -> logRecorder.getLoggers().forEach(Target::enable));
    SaveableListener.fireOnChange(this, getConfigFile());
  }
  
  public void doRss(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { LogRecorderManager.doRss(req, rsp, getLogRecords()); }
  
  private XmlFile getConfigFile() {
    return new XmlFile(XSTREAM, new File(LogRecorderManager.configDir(), this.name + ".xml"));
  }
  
  public List<LogRecord> getLogRecords() { return this.handler.getView(); }
  
  public Map<Computer, List<LogRecord>> getSlaveLogRecords() {
    Map<Computer, List<LogRecord>> result = new TreeMap<Computer, List<LogRecord>>(new Object(this));
    for (Computer c : Jenkins.get().getComputers()) {
      if (c.getName().length() != 0) {
        List<LogRecord> recs = new ArrayList<LogRecord>();
        try {
          for (LogRecord rec : c.getLogRecords()) {
            for (Target t : this.loggers) {
              if (t.includes(rec))
                recs.add(rec); 
            } 
          } 
        } catch (IOException|InterruptedException x) {}
        if (!recs.isEmpty())
          result.put(c, recs); 
      } 
    } 
    return result;
  }
  
  public static final XStream XSTREAM = new XStream2();
  
  public static List<Level> LEVELS;
  
  static  {
    XSTREAM.alias("log", LogRecorder.class);
    XSTREAM.alias("target", Target.class);
    LEVELS = Arrays.asList(new Level[] { Level.ALL, Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG, Level.INFO, Level.WARNING, Level.SEVERE, Level.OFF });
  }
}
