package jenkins.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import hudson.util.AtomicFileWriter;
import hudson.util.StreamTaskListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public final class RunIdMigrator {
  private final DateFormat legacyIdFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
  
  static final Logger LOGGER = Logger.getLogger(RunIdMigrator.class.getName());
  
  private static final String MAP_FILE = "legacyIds";
  
  private static final Map<String, Integer> EMPTY = new TreeMap();
  
  private static final Set<File> offeredToUnmigrate = Collections.synchronizedSet(new HashSet());
  
  @NonNull
  private Map<String, Integer> idToNumber = EMPTY;
  
  private boolean load(File dir) {
    File f = new File(dir, "legacyIds");
    if (!f.isFile())
      return false; 
    if (f.length() == 0L)
      return true; 
    this.idToNumber = new TreeMap();
    try {
      for (String line : Files.readAllLines(Util.fileToPath(f), StandardCharsets.UTF_8)) {
        int i = line.indexOf(' ');
        this.idToNumber.put(line.substring(0, i), Integer.valueOf(Integer.parseInt(line.substring(i + 1))));
      } 
    } catch (Exception x) {
      LOGGER.log(Level.WARNING, "could not read from " + f, x);
    } 
    return true;
  }
  
  private void save(File dir) {
    File f = new File(dir, "legacyIds");
    try {
      w = new AtomicFileWriter(f);
      try {
        try {
          synchronized (this) {
            for (Map.Entry<String, Integer> entry : this.idToNumber.entrySet())
              w.write((String)entry.getKey() + " " + (String)entry.getKey() + "\n"); 
          } 
          w.commit();
        } finally {
          w.abort();
        } 
        w.close();
      } catch (Throwable throwable) {
        try {
          w.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (IOException x) {
      LOGGER.log(Level.WARNING, "could not save changes to " + f, x);
    } 
  }
  
  public void created(File dir) { save(dir); }
  
  public boolean migrate(File dir, @CheckForNull File jenkinsHome) {
    if (load(dir)) {
      LOGGER.log(Level.FINER, "migration already performed for {0}", dir);
      return false;
    } 
    if (!dir.isDirectory()) {
      LOGGER.log(Level.FINE, "{0} was unexpectedly missing", dir);
      return false;
    } 
    LOGGER.log(Level.INFO, "Migrating build records in {0}", dir);
    doMigrate(dir);
    save(dir);
    if (jenkinsHome != null && offeredToUnmigrate.add(jenkinsHome))
      LOGGER.log(Level.WARNING, "Build record migration (https://www.jenkins.io/redirect/build-record-migration) is one-way. If you need to downgrade Jenkins, run: {0}", getUnmigrationCommandLine(jenkinsHome)); 
    return true;
  }
  
  private static String getUnmigrationCommandLine(File jenkinsHome) {
    StringBuilder cp = new StringBuilder();
    for (Class<?> c : new Class[] { RunIdMigrator.class, org.kohsuke.stapler.framework.io.WriterOutputStream.class, org.apache.tools.ant.BuildException.class, org.apache.commons.lang.time.FastDateFormat.class }) {
      URL location = c.getProtectionDomain().getCodeSource().getLocation();
      String locationS = location.toString();
      if (location.getProtocol().equals("file"))
        try {
          locationS = (new File(location.toURI())).getAbsolutePath();
        } catch (URISyntaxException uRISyntaxException) {} 
      if (cp.length() > 0)
        cp.append(File.pathSeparator); 
      cp.append(locationS);
    } 
    return String.format("java -classpath \"%s\" %s \"%s\"", new Object[] { cp, RunIdMigrator.class.getName(), jenkinsHome });
  }
  
  private static final Pattern NUMBER_ELT = Pattern.compile("(?m)^  <number>(\\d+)</number>(\r?\n)");
  
  private void doMigrate(File dir) {
    this.idToNumber = new TreeMap();
    File[] kids = dir.listFiles();
    List<File> kidsList = new ArrayList<File>(Arrays.asList(kids));
    Iterator<File> it = kidsList.iterator();
    while (it.hasNext()) {
      File kid = (File)it.next();
      String name = kid.getName();
      try {
        Integer.parseInt(name);
      } catch (NumberFormatException x) {
        LOGGER.log(Level.FINE, "ignoring nonnumeric entry {0}", name);
        continue;
      } 
      try {
        if (Util.isSymlink(kid)) {
          LOGGER.log(Level.FINE, "deleting build number symlink {0} → {1}", new Object[] { name, Util.resolveSymlink(kid) });
        } else {
          if (kid.isDirectory()) {
            LOGGER.log(Level.FINE, "ignoring build directory {0}", name);
            continue;
          } 
          LOGGER.log(Level.WARNING, "need to delete anomalous file entry {0}", name);
        } 
        Util.deleteFile(kid);
        it.remove();
      } catch (Exception x) {
        LOGGER.log(Level.WARNING, "failed to process " + kid, x);
      } 
    } 
    it = kidsList.iterator();
    while (it.hasNext()) {
      File kid = (File)it.next();
      try {
        String name = kid.getName();
        try {
          Integer.parseInt(name);
          LOGGER.log(Level.FINE, "skipping new build dir {0}", name);
        } catch (NumberFormatException numberFormatException) {
          long timestamp;
          if (!kid.isDirectory()) {
            LOGGER.log(Level.FINE, "skipping non-directory {0}", name);
            continue;
          } 
          try {
            synchronized (this.legacyIdFormatter) {
              timestamp = this.legacyIdFormatter.parse(name).getTime();
            } 
          } catch (ParseException x) {
            LOGGER.log(Level.WARNING, "found unexpected dir {0}", name);
            continue;
          } 
          File buildXml = new File(kid, "build.xml");
          if (!buildXml.isFile()) {
            LOGGER.log(Level.WARNING, "found no build.xml in {0}", name);
            continue;
          } 
          String xml = Files.readString(Util.fileToPath(buildXml), StandardCharsets.UTF_8);
          Matcher m = NUMBER_ELT.matcher(xml);
          if (!m.find()) {
            LOGGER.log(Level.WARNING, "could not find <number> in {0}/build.xml", name);
            continue;
          } 
          int number = Integer.parseInt(m.group(1));
          String nl = m.group(2);
          xml = m.replaceFirst("  <id>" + name + "</id>" + nl + "  <timestamp>" + timestamp + "</timestamp>" + nl);
          File newKid = new File(dir, Integer.toString(number));
          move(kid, newKid);
          Files.writeString(Util.fileToPath(newKid).resolve("build.xml"), xml, StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
          LOGGER.log(Level.FINE, "fully processed {0} → {1}", new Object[] { name, Integer.valueOf(number) });
          this.idToNumber.put(name, Integer.valueOf(number));
        } 
      } catch (Exception x) {
        LOGGER.log(Level.WARNING, "failed to process " + kid, x);
      } 
    } 
  }
  
  static void move(File src, File dest) throws IOException {
    try {
      Files.move(src.toPath(), dest.toPath(), new java.nio.file.CopyOption[0]);
    } catch (IOException x) {
      throw x;
    } catch (RuntimeException x) {
      throw new IOException(x);
    } 
  }
  
  public int findNumber(@NonNull String id) {
    Integer number = (Integer)this.idToNumber.get(id);
    return (number != null) ? number.intValue() : 0;
  }
  
  public void delete(File dir, String id) {
    if (this.idToNumber.remove(id) != null)
      save(dir); 
  }
  
  public static void main(String... args) throws Exception {
    if (args.length != 1)
      throw new Exception("pass one parameter, $JENKINS_HOME"); 
    File root = constructFile(args[0]);
    File jobs = new File(root, "jobs");
    if (!jobs.isDirectory())
      throw new FileNotFoundException("no such $JENKINS_HOME " + root); 
    (new RunIdMigrator()).unmigrateJobsDir(jobs);
  }
  
  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"}, justification = "Only invoked from the command line as a standalone utility")
  private static File constructFile(String arg) { return new File(arg); }
  
  private void unmigrateJobsDir(File jobs) {
    File[] jobDirs = jobs.listFiles();
    if (jobDirs == null) {
      System.err.println("" + jobs + " claimed to exist, but cannot be listed");
      return;
    } 
    for (File job : jobDirs) {
      if (job.getName().equals("builds"))
        unmigrateBuildsDir(job); 
      File[] kids = job.listFiles();
      if (kids != null)
        for (File kid : kids) {
          if (kid.isDirectory())
            if (kid.getName().equals("builds")) {
              unmigrateBuildsDir(kid);
            } else {
              unmigrateJobsDir(kid);
            }  
        }  
    } 
  }
  
  private static final Pattern ID_ELT = Pattern.compile("(?m)^  <id>([0-9_-]+)</id>(\r?\n)");
  
  private static final Pattern TIMESTAMP_ELT = Pattern.compile("(?m)^  <timestamp>(\\d+)</timestamp>(\r?\n)");
  
  private void unmigrateBuildsDir(File builds) {
    File mapFile = new File(builds, "legacyIds");
    if (!mapFile.isFile()) {
      System.err.println("" + builds + " does not look to have been migrated yet; skipping");
      return;
    } 
    for (File build : builds.listFiles()) {
      int number;
      try {
        number = Integer.parseInt(build.getName());
      } catch (NumberFormatException x) {}
      File buildXml = new File(build, "build.xml");
      if (!buildXml.isFile()) {
        System.err.println("" + buildXml + " did not exist");
      } else {
        String xml = Files.readString(Util.fileToPath(buildXml), StandardCharsets.UTF_8);
        Matcher m = TIMESTAMP_ELT.matcher(xml);
        if (!m.find()) {
          System.err.println("" + buildXml + " did not contain <timestamp> as expected");
        } else {
          String id;
          long timestamp = Long.parseLong(m.group(1));
          String nl = m.group(2);
          xml = m.replaceFirst("  <number>" + number + "</number>" + nl);
          m = ID_ELT.matcher(xml);
          if (m.find()) {
            id = m.group(1);
            xml = m.replaceFirst("");
          } else {
            id = this.legacyIdFormatter.format(new Date(timestamp));
          } 
          Files.writeString(Util.fileToPath(buildXml), xml, StandardCharsets.UTF_8, new java.nio.file.OpenOption[0]);
          if (!build.renameTo(new File(builds, id)))
            System.err.println("" + build + " could not be renamed"); 
          Util.createSymlink(builds, id, Integer.toString(number), StreamTaskListener.fromStderr());
        } 
      } 
    } 
    Util.deleteFile(mapFile);
    System.err.println("" + builds + " has been restored to its original format");
  }
}
