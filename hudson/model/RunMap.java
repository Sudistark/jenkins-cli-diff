package hudson.model;

import hudson.Util;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.RunIdMigrator;
import jenkins.model.lazy.AbstractLazyLoadRunMap;
import jenkins.model.lazy.BuildReference;
import org.kohsuke.accmod.Restricted;

public final class RunMap<R extends Run<?, R>> extends AbstractLazyLoadRunMap<R> implements Iterable<R> {
  private final SortedMap<Integer, R> view = Collections.unmodifiableSortedMap(this);
  
  private Constructor<R> cons;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public RunIdMigrator runIdMigrator = new RunIdMigrator();
  
  @Deprecated
  public RunMap() { super(null); }
  
  public RunMap(File baseDir, Constructor cons) {
    super(baseDir);
    this.cons = cons;
  }
  
  public boolean remove(R run) { return removeValue(run); }
  
  public Iterator<R> iterator() { return new Object(this); }
  
  public boolean removeValue(R run) {
    run.dropLinks();
    this.runIdMigrator.delete(this.dir, run.getId());
    return super.removeValue(run);
  }
  
  public SortedMap<Integer, R> getView() { return this.view; }
  
  public R newestValue() { return (R)(Run)search(2147483647, AbstractLazyLoadRunMap.Direction.DESC); }
  
  public R oldestValue() { return (R)(Run)search(-2147483648, AbstractLazyLoadRunMap.Direction.ASC); }
  
  @Deprecated
  public static final Comparator<Comparable> COMPARATOR = Comparator.reverseOrder();
  
  protected int getNumberOf(R r) { return r.getNumber(); }
  
  protected String getIdOf(R r) { return r.getId(); }
  
  public R put(R r) {
    File rootDir = r.getRootDir();
    if (Files.isDirectory(rootDir.toPath(), new java.nio.file.LinkOption[0]))
      throw new IllegalStateException("JENKINS-23152: " + rootDir + " already existed; will not overwrite with " + r); 
    if (!r.getClass().getName().equals("hudson.matrix.MatrixRun"))
      proposeNewNumber(r.getNumber()); 
    try {
      Util.createDirectories(rootDir.toPath(), new java.nio.file.attribute.FileAttribute[0]);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } 
    return (R)(Run)_put(r);
  }
  
  public R getById(String id) {
    int n;
    try {
      n = Integer.parseInt(id);
    } catch (NumberFormatException x) {
      n = this.runIdMigrator.findNumber(id);
    } 
    return (R)(Run)getByNumber(n);
  }
  
  protected BuildReference<R> createReference(R r) { return r.createReference(); }
  
  protected R retrieve(File d) throws IOException {
    if ((new File(d, "build.xml")).exists())
      try {
        R b = (R)this.cons.create(d);
        b.onLoad();
        if (LOGGER.isLoggable(Level.FINEST))
          LOGGER.log(Level.FINEST, "Loaded " + b.getFullDisplayName() + " in " + Thread.currentThread().getName(), new ThisIsHowItsLoaded()); 
        return b;
      } catch (Exception|InstantiationError e) {
        LOGGER.log(Level.WARNING, "could not load " + d, e);
      }  
    return null;
  }
  
  @Deprecated
  public void load(Job job, Constructor<R> cons) {
    this.cons = cons;
    initBaseDir(job.getBuildDir());
  }
  
  private static final Logger LOGGER = Logger.getLogger(RunMap.class.getName());
}
