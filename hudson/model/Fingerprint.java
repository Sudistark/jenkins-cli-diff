package hudson.model;

import com.thoughtworks.xstream.converters.basic.DateConverter;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.ExtensionList;
import hudson.Util;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.util.HexBinaryConverter;
import hudson.util.PersistedList;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.fingerprints.FileFingerprintStorage;
import jenkins.fingerprints.FingerprintStorage;
import jenkins.model.FingerprintFacet;
import jenkins.model.Jenkins;
import jenkins.model.TransientFingerprintFacetFactory;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@ExportedBean
public class Fingerprint implements ModelObject, Saveable {
  private static final DateConverter DATE_CONVERTER = new DateConverter();
  
  @NonNull
  private final Date timestamp;
  
  @CheckForNull
  private final BuildPtr original;
  
  private final byte[] md5sum;
  
  private final String fileName;
  
  private Hashtable<String, RangeSet> usages;
  
  PersistedList<FingerprintFacet> facets;
  
  public Fingerprint(@CheckForNull Run build, @NonNull String fileName, @NonNull byte[] md5sum) throws IOException {
    this((build == null) ? null : new BuildPtr(build), fileName, md5sum);
    save();
  }
  
  Fingerprint(@CheckForNull BuildPtr original, @NonNull String fileName, @NonNull byte[] md5sum) {
    this.usages = new Hashtable();
    this.facets = new PersistedList(this);
    this.transientFacets = null;
    this.original = original;
    this.md5sum = md5sum;
    this.fileName = fileName;
    this.timestamp = new Date();
  }
  
  @Exported
  @CheckForNull
  public BuildPtr getOriginal() {
    if (this.original != null && this.original.hasPermissionToDiscoverBuild())
      return this.original; 
    return null;
  }
  
  @NonNull
  public String getDisplayName() { return this.fileName; }
  
  @Exported
  @NonNull
  public String getFileName() { return this.fileName; }
  
  @Exported(name = "hash")
  @NonNull
  public String getHashString() { return Util.toHexString(this.md5sum); }
  
  @Exported
  @NonNull
  public Date getTimestamp() { return this.timestamp; }
  
  @NonNull
  public String getTimestampString() {
    long duration = System.currentTimeMillis() - this.timestamp.getTime();
    return Util.getTimeSpanString(duration);
  }
  
  @NonNull
  public RangeSet getRangeSet(String jobFullName) {
    RangeSet r = (RangeSet)this.usages.get(jobFullName);
    if (r == null)
      r = new RangeSet(); 
    return r;
  }
  
  public RangeSet getRangeSet(Job job) { return getRangeSet(job.getFullName()); }
  
  @NonNull
  public List<String> getJobs() {
    List<String> r = new ArrayList<String>(this.usages.keySet());
    Collections.sort(r);
    return r;
  }
  
  @CheckForNull
  public Hashtable<String, RangeSet> getUsages() { return this.usages; }
  
  @Exported(name = "usage")
  @NonNull
  public List<RangeItem> _getUsages() {
    List<RangeItem> r = new ArrayList<RangeItem>();
    Jenkins instance = Jenkins.get();
    for (Map.Entry<String, RangeSet> e : this.usages.entrySet()) {
      String itemName = (String)e.getKey();
      if (instance.hasPermission(Jenkins.ADMINISTER) || canDiscoverItem(itemName))
        r.add(new RangeItem(itemName, (RangeSet)e.getValue())); 
    } 
    return r;
  }
  
  @Deprecated
  public void add(@NonNull AbstractBuild b) throws IOException { addFor(b); }
  
  public void addFor(@NonNull Run b) throws IOException { add(b.getParent().getFullName(), b.getNumber()); }
  
  public void add(@NonNull String jobFullName, int n) throws IOException {
    addWithoutSaving(jobFullName, n);
    save();
  }
  
  @SuppressFBWarnings(value = {"IS2_INCONSISTENT_SYNC"}, justification = "nothing should be competing with XStream during deserialization")
  protected Object readResolve() {
    if (this.usages == null)
      this.usages = new Hashtable(); 
    return this;
  }
  
  void addWithoutSaving(@NonNull String jobFullName, int n) throws IOException {
    synchronized (this.usages) {
      RangeSet r = (RangeSet)this.usages.get(jobFullName);
      if (r == null) {
        r = new RangeSet();
        this.usages.put(jobFullName, r);
      } 
      r.add(n);
    } 
  }
  
  public boolean isAlive() {
    if (this.original != null && this.original.isAlive())
      return true; 
    for (Map.Entry<String, RangeSet> e : this.usages.entrySet()) {
      Job j = (Job)Jenkins.get().getItemByFullName((String)e.getKey(), Job.class);
      if (j == null)
        continue; 
      Run firstBuild = j.getFirstBuild();
      if (firstBuild == null)
        continue; 
      int oldest = firstBuild.getNumber();
      if (!((RangeSet)e.getValue()).isSmallerThan(oldest))
        return true; 
    } 
    return false;
  }
  
  public boolean trim() {
    boolean modified = false;
    for (Map.Entry<String, RangeSet> e : (new Hashtable(this.usages)).entrySet()) {
      Job j = (Job)Jenkins.get().getItemByFullName((String)e.getKey(), Job.class);
      if (j == null) {
        modified = true;
        this.usages.remove(e.getKey());
        continue;
      } 
      Run firstBuild = j.getFirstBuild();
      if (firstBuild == null) {
        modified = true;
        this.usages.remove(e.getKey());
        continue;
      } 
      RangeSet cur = (RangeSet)e.getValue();
      RangeSet kept = new RangeSet();
      Run r = firstBuild;
      while (r != null && r.isKeepLog()) {
        kept.add(r.getNumber());
        r = r.getNextBuild();
      } 
      if (r == null) {
        modified |= cur.retainAll(kept);
      } else {
        RangeSet discarding = new RangeSet(new Range(-1, r.getNumber()));
        discarding.removeAll(kept);
        modified |= cur.removeAll(discarding);
      } 
      if (cur.isEmpty()) {
        this.usages.remove(e.getKey());
        modified = true;
      } 
    } 
    if (modified) {
      if (logger.isLoggable(Level.FINE))
        logger.log(Level.FINE, "Saving trimmed Fingerprint ", this.md5sum); 
      save();
    } 
    return modified;
  }
  
  @NonNull
  public Collection<FingerprintFacet> getFacets() {
    if (this.transientFacets == null) {
      List<FingerprintFacet> transientFacets = new ArrayList<FingerprintFacet>();
      for (TransientFingerprintFacetFactory fff : TransientFingerprintFacetFactory.all())
        fff.createFor(this, transientFacets); 
      this.transientFacets = Collections.unmodifiableList(transientFacets);
    } 
    return new Object(this);
  }
  
  @NonNull
  public final PersistedList<FingerprintFacet> getPersistedFacets() { return this.facets; }
  
  @NonNull
  public Collection<FingerprintFacet> getSortedFacets() {
    List<FingerprintFacet> r = new ArrayList<FingerprintFacet>(getFacets());
    r.sort(new Object(this));
    return r;
  }
  
  @CheckForNull
  public <T extends FingerprintFacet> T getFacet(Class<T> type) {
    for (FingerprintFacet f : getFacets()) {
      if (type.isInstance(f))
        return (T)(FingerprintFacet)type.cast(f); 
    } 
    return null;
  }
  
  @NonNull
  public List<Action> getActions() {
    List<Action> r = new ArrayList<Action>();
    for (FingerprintFacet ff : getFacets())
      ff.createActions(r); 
    return Collections.unmodifiableList(r);
  }
  
  public void save() throws IOException {
    if (BulkChange.contains(this))
      return; 
    long start = 0L;
    if (logger.isLoggable(Level.FINE))
      start = System.currentTimeMillis(); 
    FingerprintStorage configuredFingerprintStorage = FingerprintStorage.get();
    FingerprintStorage fileFingerprintStorage = (FingerprintStorage)ExtensionList.lookupSingleton(FileFingerprintStorage.class);
    configuredFingerprintStorage.save(this);
    if (!(configuredFingerprintStorage instanceof FileFingerprintStorage) && fileFingerprintStorage.isReady())
      fileFingerprintStorage.delete(getHashString()); 
    if (logger.isLoggable(Level.FINE))
      logger.fine("Saving fingerprint " + getHashString() + " took " + System.currentTimeMillis() - start + "ms"); 
  }
  
  @Deprecated
  void save(File file) throws IOException { FileFingerprintStorage.save(this, file); }
  
  @CheckForNull
  public FingerprintFacet getFacetBlockingDeletion() {
    for (FingerprintFacet facet : this.facets) {
      if (facet.isFingerprintDeletionBlocked())
        return facet; 
    } 
    return null;
  }
  
  public void rename(String oldName, String newName) throws IOException {
    boolean touched = false;
    if (this.original != null && 
      this.original.getName().equals(oldName)) {
      this.original.setName(newName);
      touched = true;
    } 
    if (this.usages != null) {
      RangeSet r = (RangeSet)this.usages.get(oldName);
      if (r != null) {
        this.usages.put(newName, r);
        this.usages.remove(oldName);
        touched = true;
      } 
    } 
    if (touched)
      save(); 
  }
  
  public Api getApi() { return new Api(this); }
  
  @CheckForNull
  public static Fingerprint load(@NonNull String id) throws IOException {
    long start = 0L;
    if (logger.isLoggable(Level.FINE))
      start = System.currentTimeMillis(); 
    FingerprintStorage configuredFingerprintStorage = FingerprintStorage.get();
    FingerprintStorage fileFingerprintStorage = (FingerprintStorage)ExtensionList.lookupSingleton(FileFingerprintStorage.class);
    Fingerprint loaded = configuredFingerprintStorage.load(id);
    if (loaded == null && !(configuredFingerprintStorage instanceof FileFingerprintStorage) && fileFingerprintStorage
      .isReady()) {
      loaded = fileFingerprintStorage.load(id);
      if (loaded != null) {
        initFacets(loaded);
        configuredFingerprintStorage.save(loaded);
        fileFingerprintStorage.delete(id);
      } 
    } else {
      initFacets(loaded);
    } 
    if (logger.isLoggable(Level.FINE))
      logger.fine("Loading fingerprint took " + System.currentTimeMillis() - start + "ms"); 
    return loaded;
  }
  
  @Deprecated
  @CheckForNull
  static Fingerprint load(@NonNull byte[] md5sum) throws IOException { return load(Util.toHexString(md5sum)); }
  
  @Deprecated
  @CheckForNull
  static Fingerprint load(@NonNull File file) throws IOException {
    Fingerprint fingerprint = FileFingerprintStorage.load(file);
    initFacets(fingerprint);
    return fingerprint;
  }
  
  public static void delete(@NonNull String id) throws IOException {
    FingerprintStorage configuredFingerprintStorage = FingerprintStorage.get();
    FingerprintStorage fileFingerprintStorage = (FingerprintStorage)ExtensionList.lookupSingleton(FileFingerprintStorage.class);
    configuredFingerprintStorage.delete(id);
    if (!(configuredFingerprintStorage instanceof FileFingerprintStorage) && fileFingerprintStorage.isReady())
      fileFingerprintStorage.delete(id); 
  }
  
  private static void initFacets(@CheckForNull Fingerprint fingerprint) {
    if (fingerprint == null)
      return; 
    for (FingerprintFacet facet : fingerprint.facets)
      facet._setOwner(fingerprint); 
  }
  
  public String toString() { return "Fingerprint[original=" + this.original + ",hash=" + 

      
      getHashString() + ",fileName=" + this.fileName + ",timestamp=" + DATE_CONVERTER


      
      .toString(this.timestamp) + ",usages=" + (
      
      (this.usages == null) ? "null" : new TreeMap(getUsages())) + ",facets=" + this.facets + "]"; }
  
  private static boolean canDiscoverItem(@NonNull String fullName) {
    Jenkins jenkins = Jenkins.get();
    Item item = null;
    try {
      item = jenkins.getItemByFullName(fullName);
    } catch (AccessDeniedException accessDeniedException) {}
    if (item != null)
      return true; 
    Authentication userAuth = Jenkins.getAuthentication2();
    ACLContext acl = ACL.as2(ACL.SYSTEM2);
    try {
      Item itemBySystemUser = jenkins.getItemByFullName(fullName);
      if (itemBySystemUser == null) {
        boolean bool1 = false;
        if (acl != null)
          acl.close(); 
        return bool1;
      } 
      boolean canDiscoverTheItem = itemBySystemUser.hasPermission2(userAuth, Item.DISCOVER);
      if (canDiscoverTheItem) {
        ItemGroup<?> current = itemBySystemUser.getParent();
        do {
          if (current instanceof Item) {
            Item i = (Item)current;
            current = i.getParent();
            if (!i.hasPermission2(userAuth, Item.READ))
              canDiscoverTheItem = false; 
          } else {
            current = null;
          } 
        } while (canDiscoverTheItem && current != null);
      } 
      boolean bool = canDiscoverTheItem;
      if (acl != null)
        acl.close(); 
      return bool;
    } catch (Throwable throwable) {
      if (acl != null)
        try {
          acl.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  private static final XStream2 XSTREAM = new XStream2();
  
  private static final Logger logger;
  
  @NonNull
  public static XStream2 getXStream() { return XSTREAM; }
  
  static  {
    XSTREAM.alias("fingerprint", Fingerprint.class);
    XSTREAM.alias("range", Range.class);
    XSTREAM.alias("ranges", RangeSet.class);
    XSTREAM.registerConverter(new HexBinaryConverter(), 10);
    XSTREAM.registerConverter(new RangeSet.ConverterImpl(new Object(XSTREAM
            .getMapper())), 10);
    logger = Logger.getLogger(Fingerprint.class.getName());
  }
}
