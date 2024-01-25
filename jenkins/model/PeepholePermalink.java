package jenkins.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Job;
import hudson.model.PermalinkProjectAction;
import hudson.model.Run;
import hudson.util.AtomicFileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public abstract class PeepholePermalink extends PermalinkProjectAction.Permalink implements Predicate<Run<?, ?>> {
  private static final Map<File, Map<String, Integer>> caches = new HashMap();
  
  private static final int RESOLVES_TO_NONE = -1;
  
  public boolean test(Run<?, ?> run) { return apply(run); }
  
  @Deprecated
  protected File getPermalinkFile(Job<?, ?> job) { return new File(job.getBuildDir(), getId()); }
  
  public Run<?, ?> resolve(Job<?, ?> job) {
    int n;
    Map<String, Integer> cache = cacheFor(job.getBuildDir());
    synchronized (cache) {
      n = ((Integer)cache.getOrDefault(getId(), Integer.valueOf(0))).intValue();
    } 
    if (n == -1)
      return null; 
    if (n > 0) {
      b = job.getBuildByNumber(n);
      if (b != null && apply(b))
        return b; 
    } else {
      b = null;
    } 
    if (b == null)
      b = job.getNearestOldBuild(n); 
    if (b == null)
      b = job.getLastBuild(); 
    Run<?, ?> b = find(b);
    updateCache(job, b);
    return b;
  }
  
  private Run<?, ?> find(Run<?, ?> b) {
    for (; b != null && !apply(b); b = b.getPreviousBuild());
    return b;
  }
  
  @NonNull
  private static Map<String, Integer> cacheFor(@NonNull File buildDir) {
    synchronized (caches) {
      Map<String, Integer> cache = (Map)caches.get(buildDir);
      if (cache == null) {
        cache = load(buildDir);
        caches.put(buildDir, cache);
      } 
      return cache;
    } 
  }
  
  @NonNull
  private static Map<String, Integer> load(@NonNull File buildDir) {
    Map<String, Integer> cache = new TreeMap<String, Integer>();
    File storage = storageFor(buildDir);
    if (storage.isFile()) {
      try {
        Stream<String> lines = Files.lines(storage.toPath(), StandardCharsets.UTF_8);
        try {
          lines.forEach(line -> {
                int idx = line.indexOf(' ');
                if (idx == -1)
                  return; 
                try {
                  cache.put(line.substring(0, idx), Integer.valueOf(Integer.parseInt(line.substring(idx + 1))));
                } catch (NumberFormatException x) {
                  LOGGER.log(Level.WARNING, "failed to read " + storage, x);
                } 
              });
          if (lines != null)
            lines.close(); 
        } catch (Throwable throwable) {
          if (lines != null)
            try {
              lines.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } catch (IOException x) {
        LOGGER.log(Level.WARNING, "failed to read " + storage, x);
      } 
      LOGGER.fine(() -> "loading from " + storage + ": " + cache);
    } 
    return cache;
  }
  
  @NonNull
  static File storageFor(@NonNull File buildDir) { return new File(buildDir, "permalinks"); }
  
  protected void updateCache(@NonNull Job<?, ?> job, @CheckForNull Run<?, ?> b) {
    File buildDir = job.getBuildDir();
    Map<String, Integer> cache = cacheFor(buildDir);
    synchronized (cache) {
      cache.put(getId(), Integer.valueOf((b == null) ? -1 : b.getNumber()));
      File storage = storageFor(buildDir);
      LOGGER.fine(() -> "saving to " + storage + ": " + cache);
      try {
        cw = new AtomicFileWriter(storage);
        try {
          try {
            for (Map.Entry<String, Integer> entry : cache.entrySet()) {
              cw.write((String)entry.getKey());
              cw.write(32);
              cw.write(Integer.toString(((Integer)entry.getValue()).intValue()));
              cw.write(10);
            } 
            cw.commit();
          } finally {
            cw.abort();
          } 
          cw.close();
        } catch (Throwable throwable) {
          try {
            cw.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } catch (IOException x) {
        LOGGER.log(Level.WARNING, "failed to update " + storage, x);
      } 
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(PeepholePermalink.class.getName());
  
  public abstract boolean apply(Run<?, ?> paramRun);
}
