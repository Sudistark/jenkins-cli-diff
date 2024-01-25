package hudson;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Hudson;
import hudson.util.IOUtils;
import hudson.util.MaskingClassLoader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.ExtensionFilter;
import jenkins.plugins.DetachedPluginsUtil;
import jenkins.util.URLClassLoader2;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.resources.MappedResourceCollection;
import org.apache.tools.ant.util.GlobPatternMapper;

public class ClassicPluginStrategy implements PluginStrategy {
  private static final Logger LOGGER = Logger.getLogger(ClassicPluginStrategy.class.getName());
  
  private static final FilenameFilter JAR_FILTER = (dir, name) -> name.endsWith(".jar");
  
  private final PluginManager pluginManager;
  
  private final MaskingClassLoader coreClassLoader;
  
  public ClassicPluginStrategy(PluginManager pluginManager) {
    this.coreClassLoader = new MaskingClassLoader(getClass().getClassLoader(), new String[0]);
    this.pluginManager = pluginManager;
  }
  
  public String getShortName(File archive) throws IOException {
    Manifest manifest;
    if (!archive.exists())
      throw new FileNotFoundException("Failed to load " + archive + ". The file does not exist"); 
    if (!archive.isFile())
      throw new FileNotFoundException("Failed to load " + archive + ". It is not a file"); 
    if (isLinked(archive)) {
      manifest = loadLinkedManifest(archive);
    } else {
      try {
        JarFile jf = new JarFile(archive, false);
        try {
          manifest = jf.getManifest();
          jf.close();
        } catch (Throwable throwable) {
          try {
            jf.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } catch (IOException ex) {
        throw new IOException("Failed to load " + archive, ex);
      } 
    } 
    return PluginWrapper.computeShortName(manifest, archive.getName());
  }
  
  private static boolean isLinked(File archive) { return (archive.getName().endsWith(".hpl") || archive.getName().endsWith(".jpl")); }
  
  private static Manifest loadLinkedManifest(File archive) throws IOException {
    try {
      String firstLine;
      try {
        InputStream manifestHeaderInput = Files.newInputStream(archive.toPath(), new java.nio.file.OpenOption[0]);
        try {
          firstLine = IOUtils.readFirstLine(manifestHeaderInput, "UTF-8");
          if (manifestHeaderInput != null)
            manifestHeaderInput.close(); 
        } catch (Throwable throwable) {
          if (manifestHeaderInput != null)
            try {
              manifestHeaderInput.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } catch (InvalidPathException e) {
        throw new IOException(e);
      } 
      if (!firstLine.startsWith("Manifest-Version:"))
        archive = resolve(archive, firstLine); 
      try {
        InputStream manifestInput = Files.newInputStream(archive.toPath(), new java.nio.file.OpenOption[0]);
        try {
          Manifest manifest = new Manifest(manifestInput);
          if (manifestInput != null)
            manifestInput.close(); 
          return manifest;
        } catch (Throwable throwable) {
          if (manifestInput != null)
            try {
              manifestInput.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } catch (InvalidPathException e) {
        throw new IOException(e);
      } 
    } catch (IOException e) {
      String firstLine;
      throw new IOException("Failed to load " + archive, firstLine);
    } 
  }
  
  public PluginWrapper createPluginWrapper(File archive) throws IOException {
    URL baseResourceURL;
    Manifest manifest;
    File expandDir = null;
    boolean isLinked = isLinked(archive);
    if (isLinked) {
      manifest = loadLinkedManifest(archive);
    } else {
      if (archive.isDirectory()) {
        expandDir = archive;
      } else {
        File f = this.pluginManager.getWorkDir();
        expandDir = new File((f == null) ? archive.getParentFile() : f, FilenameUtils.getBaseName(archive.getName()));
        explode(archive, expandDir);
      } 
      File manifestFile = new File(expandDir, "META-INF/MANIFEST.MF");
      if (!manifestFile.exists())
        throw new IOException("Plugin installation failed. No manifest at " + manifestFile); 
      try {
        InputStream fin = Files.newInputStream(manifestFile.toPath(), new java.nio.file.OpenOption[0]);
        try {
          manifest = new Manifest(fin);
          if (fin != null)
            fin.close(); 
        } catch (Throwable throwable) {
          if (fin != null)
            try {
              fin.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } catch (InvalidPathException e) {
        throw new IOException(e);
      } 
      String canonicalName = manifest.getMainAttributes().getValue("Short-Name") + ".jpi";
      if (!archive.getName().equals(canonicalName))
        LOGGER.warning(() -> "encountered " + archive + " under a nonstandard name; expected " + canonicalName); 
    } 
    Attributes atts = manifest.getMainAttributes();
    List<File> paths = new ArrayList<File>();
    if (isLinked) {
      parseClassPath(manifest, archive, paths, "Libraries", ",");
      parseClassPath(manifest, archive, paths, "Class-Path", " +");
      baseResourceURL = resolve(archive, atts.getValue("Resource-Path")).toURI().toURL();
    } else {
      File classes = new File(expandDir, "WEB-INF/classes");
      if (classes.exists()) {
        LOGGER.log(Level.WARNING, "Deprecated unpacked classes directory found in {0}", classes);
        paths.add(classes);
      } 
      File lib = new File(expandDir, "WEB-INF/lib");
      File[] libs = lib.listFiles(JAR_FILTER);
      if (libs != null)
        paths.addAll(Arrays.asList(libs)); 
      baseResourceURL = expandDir.toPath().toUri().toURL();
    } 
    File disableFile = new File(archive.getPath() + ".disabled");
    if (disableFile.exists())
      LOGGER.info("Plugin " + archive.getName() + " is disabled"); 
    if (paths.isEmpty())
      LOGGER.info("No classpaths found for plugin " + archive.getName()); 
    List<PluginWrapper.Dependency> dependencies = new ArrayList<PluginWrapper.Dependency>();
    List<PluginWrapper.Dependency> optionalDependencies = new ArrayList<PluginWrapper.Dependency>();
    String v = atts.getValue("Plugin-Dependencies");
    if (v != null)
      for (String s : v.split(",")) {
        PluginWrapper.Dependency d = new PluginWrapper.Dependency(s);
        if (d.optional) {
          optionalDependencies.add(d);
        } else {
          dependencies.add(d);
        } 
      }  
    fix(atts, optionalDependencies);
    String masked = atts.getValue("Global-Mask-Classes");
    if (masked != null)
      for (String pkg : masked.trim().split("[ \t\r\n]+"))
        this.coreClassLoader.add(pkg);  
    DependencyClassLoader dependencyClassLoader = new DependencyClassLoader(this.coreClassLoader, archive, Util.join(new Collection[] { dependencies, optionalDependencies }, ), this.pluginManager);
    ClassLoader classLoader = getBaseClassLoader(atts, dependencyClassLoader);
    return new PluginWrapper(this.pluginManager, archive, manifest, baseResourceURL, 
        createClassLoader(paths, classLoader, atts), disableFile, dependencies, optionalDependencies);
  }
  
  private void fix(Attributes atts, List<PluginWrapper.Dependency> optionalDependencies) {
    String pluginName = atts.getValue("Short-Name");
    String jenkinsVersion = atts.getValue("Jenkins-Version");
    if (jenkinsVersion == null)
      jenkinsVersion = atts.getValue("Hudson-Version"); 
    for (PluginWrapper.Dependency d : DetachedPluginsUtil.getImpliedDependencies(pluginName, jenkinsVersion)) {
      LOGGER.fine(() -> "implied dep " + pluginName + " → " + d.shortName);
      this.pluginManager.considerDetachedPlugin(d.shortName);
      optionalDependencies.add(d);
    } 
  }
  
  @Deprecated
  @NonNull
  public static List<PluginWrapper.Dependency> getImpliedDependencies(String pluginName, String jenkinsVersion) { return DetachedPluginsUtil.getImpliedDependencies(pluginName, jenkinsVersion); }
  
  @Deprecated
  protected ClassLoader createClassLoader(List<File> paths, ClassLoader parent) throws IOException { return createClassLoader(paths, parent, null); }
  
  protected ClassLoader createClassLoader(List<File> paths, ClassLoader parent, Attributes atts) throws IOException {
    URLClassLoader2 classLoader;
    boolean usePluginFirstClassLoader = (atts != null && Boolean.parseBoolean(atts.getValue("PluginFirstClassLoader")));
    List<URL> urls = new ArrayList<URL>();
    for (File path : paths)
      urls.add(path.toURI().toURL()); 
    if (usePluginFirstClassLoader) {
      classLoader = new PluginFirstClassLoader2((URL[])urls.toArray(new URL[0]), parent);
    } else {
      classLoader = new URLClassLoader2((URL[])urls.toArray(new URL[0]), parent);
    } 
    return classLoader;
  }
  
  private ClassLoader getBaseClassLoader(Attributes atts, ClassLoader base) {
    MaskingClassLoader maskingClassLoader;
    String masked = atts.getValue("Mask-Classes");
    if (masked != null)
      maskingClassLoader = new MaskingClassLoader(base, masked.trim().split("[ \t\r\n]+")); 
    return maskingClassLoader;
  }
  
  public void initializeComponents(PluginWrapper plugin) {}
  
  public <T> List<ExtensionComponent<T>> findComponents(Class<T> type, Hudson hudson) {
    ExtensionList extensionList;
    if (type == ExtensionFinder.class) {
      extensionList = List.of(new ExtensionFinder.Sezpoz());
    } else {
      extensionList = hudson.getExtensionList(ExtensionFinder.class);
    } 
    if (LOGGER.isLoggable(Level.FINER))
      LOGGER.log(Level.FINER, "Scout-loading ExtensionList: " + type, new Throwable()); 
    for (ExtensionFinder finder : extensionList)
      finder.scout(type, hudson); 
    List<ExtensionComponent<T>> r = new ArrayList<ExtensionComponent<T>>();
    for (ExtensionFinder finder : extensionList) {
      try {
        r.addAll(finder.find(type, hudson));
      } catch (AbstractMethodError e) {
        for (T t : finder.findExtensions(type, hudson))
          r.add(new ExtensionComponent(t)); 
      } 
    } 
    List<ExtensionComponent<T>> filtered = new ArrayList<ExtensionComponent<T>>();
    for (ExtensionComponent<T> e : r) {
      if (ExtensionFilter.isAllowed(type, e))
        filtered.add(e); 
    } 
    return filtered;
  }
  
  public void load(PluginWrapper wrapper) {
    old = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(wrapper.classLoader);
    try {
      String className = wrapper.getPluginClass();
      if (className == null) {
        wrapper.setPlugin(new Plugin.DummyImpl());
      } else {
        try {
          Class<?> clazz = wrapper.classLoader.loadClass(className);
          Object o = clazz.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
          if (!(o instanceof Plugin))
            throw new IOException(className + " doesn't extend from hudson.Plugin"); 
          wrapper.setPlugin((Plugin)o);
        } catch (LinkageError|ClassNotFoundException e) {
          throw new IOException("Unable to load " + className + " from " + wrapper.getShortName(), e);
        } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|java.lang.reflect.InvocationTargetException e) {
          throw new IOException("Unable to create instance of " + className + " from " + wrapper.getShortName(), e);
        } 
      } 
      try {
        Plugin plugin = wrapper.getPluginOrFail();
        plugin.setServletContext(this.pluginManager.context);
        startPlugin(wrapper);
      } catch (Throwable t) {
        throw new IOException("Failed to initialize", t);
      } 
    } finally {
      Thread.currentThread().setContextClassLoader(old);
    } 
  }
  
  public void startPlugin(PluginWrapper plugin) { plugin.getPluginOrFail().start(); }
  
  public void updateDependency(PluginWrapper depender, PluginWrapper dependee) {
    DependencyClassLoader classLoader = findAncestorDependencyClassLoader(depender.classLoader);
    if (classLoader != null) {
      classLoader.updateTransitiveDependencies();
      LOGGER.log(Level.INFO, "Updated dependency of {0}", depender.getShortName());
    } 
  }
  
  private DependencyClassLoader findAncestorDependencyClassLoader(ClassLoader classLoader) {
    for (; classLoader != null; classLoader = classLoader.getParent()) {
      if (classLoader instanceof DependencyClassLoader)
        return (DependencyClassLoader)classLoader; 
    } 
    return null;
  }
  
  @SuppressFBWarnings(value = {"PATH_TRAVERSAL_IN"}, justification = "Administrator action installing a plugin, which could do far worse.")
  private static File resolve(File base, String relative) {
    File rel = new File(relative);
    if (rel.isAbsolute())
      return rel; 
    return new File(base.getParentFile(), relative);
  }
  
  private static void parseClassPath(Manifest manifest, File archive, List<File> paths, String attributeName, String separator) throws IOException {
    String classPath = manifest.getMainAttributes().getValue(attributeName);
    if (classPath == null)
      return; 
    for (String s : classPath.split(separator)) {
      File file = resolve(archive, s);
      if (file.getName().contains("*")) {
        FileSet fs = new FileSet();
        File dir = file.getParentFile();
        fs.setDir(dir);
        fs.setIncludes(file.getName());
        for (String included : fs.getDirectoryScanner(new Project()).getIncludedFiles())
          paths.add(new File(dir, included)); 
      } else {
        if (!file.exists())
          throw new IOException("No such file: " + file); 
        paths.add(file);
      } 
    } 
  }
  
  private static void explode(File archive, File destDir) throws IOException {
    Util.createDirectories(Util.fileToPath(destDir), new java.nio.file.attribute.FileAttribute[0]);
    File explodeTime = new File(destDir, ".timestamp2");
    if (explodeTime.exists() && explodeTime.lastModified() == archive.lastModified())
      return; 
    Util.deleteRecursive(destDir);
    try {
      Project prj = new Project();
      unzipExceptClasses(archive, destDir, prj);
      createClassJarFromWebInfClasses(archive, destDir, prj);
    } catch (BuildException x) {
      throw new IOException("Failed to expand " + archive, x);
    } 
    try {
      (new FilePath(explodeTime)).touch(archive.lastModified());
    } catch (InterruptedException e) {
      throw new AssertionError(e);
    } 
  }
  
  private static void createClassJarFromWebInfClasses(File archive, File destDir, Project prj) throws IOException {
    File classesJar = new File(destDir, "WEB-INF/lib/classes.jar");
    ZipFileSet zfs = new ZipFileSet();
    zfs.setProject(prj);
    zfs.setSrc(archive);
    zfs.setIncludes("WEB-INF/classes/");
    MappedResourceCollection mapper = new MappedResourceCollection();
    mapper.add(zfs);
    GlobPatternMapper gm = new GlobPatternMapper();
    gm.setFrom("WEB-INF/classes/*");
    gm.setTo("*");
    mapper.add(gm);
    long dirTime = archive.lastModified();
    OutputStream nos = OutputStream.nullOutputStream();
    try {
      Object object = new Object(nos, dirTime);
      try {
        Object object1 = new Object(object);
        object1.setProject(prj);
        object1.setTaskType("zip");
        Util.createDirectories(Util.fileToPath(classesJar.getParentFile()), new java.nio.file.attribute.FileAttribute[0]);
        object1.setDestFile(classesJar);
        object1.add(mapper);
        object1.execute();
        object.close();
      } catch (Throwable throwable) {
        try {
          object.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
      if (nos != null)
        nos.close(); 
    } catch (Throwable throwable) {
      if (nos != null)
        try {
          nos.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    if (classesJar.isFile())
      LOGGER.log(Level.WARNING, "Created {0}; update plugin to a version created with a newer harness", classesJar); 
  }
  
  private static void unzipExceptClasses(File archive, File destDir, Project prj) throws IOException {
    Expand e = new Expand();
    e.setProject(prj);
    e.setTaskType("unzip");
    e.setSrc(archive);
    e.setDest(destDir);
    PatternSet p = new PatternSet();
    p.setExcludes("WEB-INF/classes/");
    e.addPatternset(p);
    e.execute();
  }
}
