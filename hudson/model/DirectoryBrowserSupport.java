package hudson.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Util;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import jenkins.model.Jenkins;
import jenkins.security.ResourceDomainConfiguration;
import jenkins.security.ResourceDomainRootAction;
import jenkins.util.SystemProperties;
import jenkins.util.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public final class DirectoryBrowserSupport implements HttpResponse {
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean ALLOW_SYMLINK_ESCAPE = SystemProperties.getBoolean(DirectoryBrowserSupport.class.getName() + ".allowSymlinkEscape");
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "Accessible via System Groovy Scripts")
  public static boolean ALLOW_TMP_DISPLAY = SystemProperties.getBoolean(DirectoryBrowserSupport.class.getName() + ".allowTmpEscape");
  
  private static final Pattern TMPDIR_PATTERN = Pattern.compile(".+@tmp/.*");
  
  static final String ALLOW_ABSOLUTE_PATH_PROPERTY_NAME = DirectoryBrowserSupport.class.getName() + ".allowAbsolutePath";
  
  public final ModelObject owner;
  
  public final String title;
  
  private final VirtualFile base;
  
  private final String icon;
  
  private final boolean serveDirIndex;
  
  private String indexFileName;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String CSP_PROPERTY_NAME = DirectoryBrowserSupport.class.getName() + ".CSP";
  
  private ResourceDomainRootAction.Token resourceToken;
  
  @Deprecated
  public DirectoryBrowserSupport(ModelObject owner, String title) { this(owner, (VirtualFile)null, title, null, false); }
  
  public DirectoryBrowserSupport(ModelObject owner, FilePath base, String title, String icon, boolean serveDirIndex) { this(owner, base.toVirtualFile(), title, icon, serveDirIndex); }
  
  public DirectoryBrowserSupport(ModelObject owner, VirtualFile base, String title, String icon, boolean serveDirIndex) {
    this.indexFileName = "index.html";
    this.owner = owner;
    this.base = base;
    this.title = title;
    this.icon = icon;
    this.serveDirIndex = serveDirIndex;
  }
  
  public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
    if (!ResourceDomainConfiguration.isResourceRequest(req) && ResourceDomainConfiguration.isResourceDomainConfigured())
      this.resourceToken = ResourceDomainRootAction.get().getToken(this, req); 
    try {
      serveFile(req, rsp, this.base, this.icon, this.serveDirIndex);
    } catch (InterruptedException e) {
      throw new IOException("interrupted", e);
    } 
  }
  
  public void setIndexFileName(String fileName) { this.indexFileName = fileName; }
  
  @Deprecated
  public void serveFile(StaplerRequest req, StaplerResponse rsp, FilePath root, String icon, boolean serveDirIndex) throws IOException, ServletException, InterruptedException { serveFile(req, rsp, root.toVirtualFile(), icon, serveDirIndex); }
  
  private void serveFile(StaplerRequest req, StaplerResponse rsp, VirtualFile root, String icon, boolean serveDirIndex) throws IOException, ServletException, InterruptedException {
    VirtualFile baseFile;
    String pattern = req.getParameter("pattern");
    if (pattern == null)
      pattern = req.getParameter("path"); 
    if (pattern != null && Util.isSafeToRedirectTo(pattern)) {
      rsp.sendRedirect2(pattern);
      return;
    } 
    String path = getPath(req);
    if (path.replace('\\', '/').contains("/../")) {
      rsp.sendError(400);
      return;
    } 
    StringBuilder _base = new StringBuilder();
    StringBuilder _rest = new StringBuilder();
    int restSize = -1;
    boolean zip = false;
    boolean plain = false;
    boolean inBase = true;
    StringTokenizer pathTokens = new StringTokenizer(path, "/");
    while (pathTokens.hasMoreTokens()) {
      baseFile = pathTokens.nextToken();
      if ((baseFile.contains("?") || baseFile.contains("*")) && inBase && 
        !root.child(((_base.length() > 0) ? ("" + _base + "/") : "") + ((_base.length() > 0) ? ("" + _base + "/") : "")).exists())
        inBase = false; 
      if (baseFile.equals("*zip*")) {
        zip = true;
        break;
      } 
      if (baseFile.equals("*plain*")) {
        plain = true;
        break;
      } 
      StringBuilder sb = inBase ? _base : _rest;
      if (sb.length() > 0)
        sb.append('/'); 
      sb.append(baseFile);
      if (!inBase)
        restSize++; 
    } 
    restSize = Math.max(restSize, 0);
    String base = _base.toString();
    String rest = _rest.toString();
    if (base.isEmpty()) {
      baseFile = root;
    } else {
      if (!SystemProperties.getBoolean(ALLOW_ABSOLUTE_PATH_PROPERTY_NAME, false)) {
        boolean isAbsolute = ((Boolean)root.run(new IsAbsolute(base))).booleanValue();
        if (isAbsolute) {
          LOGGER.info(() -> "SECURITY-2481 The path provided in the URL (" + base + ") is absolute and thus is refused.");
          rsp.sendError(404);
          return;
        } 
      } 
      baseFile = root.child(base);
    } 
    if (baseFile.hasSymlink(getOpenOptions()) || hasTmpDir(baseFile, base, getOpenOptions())) {
      rsp.sendError(404);
      return;
    } 
    if (baseFile.isDirectory()) {
      if (zip) {
        String prefix, includes;
        rsp.setContentType("application/zip");
        if (StringUtils.isBlank(rest)) {
          includes = "**";
          prefix = baseFile.getName();
        } else {
          includes = rest;
          prefix = "";
        } 
        baseFile.zip(rsp.getOutputStream(), includes, null, true, prefix, getOpenOptions());
        return;
      } 
      if (plain) {
        rsp.setContentType("text/plain;charset=UTF-8");
        ServletOutputStream servletOutputStream = rsp.getOutputStream();
        try {
          for (VirtualFile kid : baseFile.list(getOpenOptions())) {
            servletOutputStream.write(kid.getName().getBytes(StandardCharsets.UTF_8));
            if (kid.isDirectory())
              servletOutputStream.write(47); 
            servletOutputStream.write(10);
          } 
          servletOutputStream.flush();
          if (servletOutputStream != null)
            servletOutputStream.close(); 
        } catch (Throwable throwable) {
          if (servletOutputStream != null)
            try {
              servletOutputStream.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
        return;
      } 
      if (rest.isEmpty()) {
        StringBuffer reqUrl = req.getRequestURL();
        if (reqUrl.charAt(reqUrl.length() - 1) != '/') {
          rsp.sendRedirect2(reqUrl.append('/').toString());
          return;
        } 
      } 
      List<List<Path>> glob = null;
      boolean patternUsed = (rest.length() > 0);
      boolean containsSymlink = false;
      boolean containsTmpDir = false;
      if (patternUsed) {
        glob = patternScan(baseFile, rest, createBackRef(restSize));
      } else if (serveDirIndex) {
        glob = (List)baseFile.run(new BuildChildPaths(root, baseFile, req.getLocale()));
        containsSymlink = baseFile.containsSymLinkChild(getOpenOptions());
        containsTmpDir = baseFile.containsTmpDirChild(getOpenOptions());
      } 
      if (glob != null) {
        req.setAttribute("it", this);
        List<Path> parentPaths = buildParentPath(base, restSize);
        req.setAttribute("parentPath", parentPaths);
        req.setAttribute("backPath", createBackRef(restSize));
        req.setAttribute("topPath", createBackRef(parentPaths.size() + restSize));
        req.setAttribute("files", glob);
        req.setAttribute("icon", icon);
        req.setAttribute("path", path);
        req.setAttribute("pattern", rest);
        req.setAttribute("dir", baseFile);
        req.setAttribute("showSymlinkWarning", Boolean.valueOf(containsSymlink));
        req.setAttribute("showTmpDirWarning", Boolean.valueOf(containsTmpDir));
        if (ResourceDomainConfiguration.isResourceRequest(req)) {
          req.getView(this, "plaindir.jelly").forward(req, rsp);
        } else {
          req.getView(this, "dir.jelly").forward(req, rsp);
        } 
        return;
      } 
      baseFile = baseFile.child(this.indexFileName);
    } 
    if (!baseFile.exists()) {
      rsp.sendError(404);
      return;
    } 
    boolean view = rest.equals("*view*");
    if (rest.equals("*fingerprint*")) {
      InputStream fingerprintInput = baseFile.open();
      try {
        rsp.forward(Jenkins.get().getFingerprint(Util.getDigestOf(fingerprintInput)), "/", req);
        if (fingerprintInput != null)
          fingerprintInput.close(); 
      } catch (Throwable throwable) {
        if (fingerprintInput != null)
          try {
            fingerprintInput.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
      return;
    } 
    URL external = baseFile.toExternalURL();
    if (external != null) {
      rsp.sendRedirect2(external.toExternalForm());
      return;
    } 
    long lastModified = baseFile.lastModified();
    long length = baseFile.length();
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("Serving " + baseFile + " with lastModified=" + lastModified + ", length=" + length); 
    if (view) {
      InputStream in;
      try {
        in = baseFile.open(getOpenOptions());
      } catch (IOException ioe) {
        rsp.sendError(404);
        return;
      } 
      rsp.setHeader("Content-Disposition", "inline; filename=" + baseFile.getName());
      rsp.serveFile(req, in, lastModified, -1L, length, "plain.txt");
    } else if (this.resourceToken != null) {
      rsp.sendRedirect(302, ResourceDomainRootAction.get().getRedirectUrl(this.resourceToken, req.getRestOfPath()));
    } else {
      InputStream in;
      if (!ResourceDomainConfiguration.isResourceRequest(req)) {
        in = SystemProperties.getString(CSP_PROPERTY_NAME, "sandbox allow-same-origin; default-src 'none'; img-src 'self'; style-src 'self';");
        if (!in.trim().isEmpty())
          for (String header : new String[] { "Content-Security-Policy", "X-WebKit-CSP", "X-Content-Security-Policy" })
            rsp.setHeader(header, in);  
      } 
      try {
        in = baseFile.open(getOpenOptions());
      } catch (IOException ioe) {
        rsp.sendError(404);
        return;
      } 
      rsp.serveFile(req, in, lastModified, -1L, length, baseFile.getName());
    } 
  }
  
  private boolean hasTmpDir(VirtualFile baseFile, String base, OpenOption[] openOptions) {
    if (FilePath.isTmpDir(baseFile.getName(), openOptions))
      return true; 
    return (FilePath.isIgnoreTmpDirs(openOptions) && TMPDIR_PATTERN.matcher(base).matches());
  }
  
  private List<List<Path>> keepReadabilityOnlyOnDescendants(VirtualFile root, boolean patternUsed, List<List<Path>> pathFragmentsList) {
    Stream<List<Path>> pathFragmentsStream = pathFragmentsList.stream().map(pathFragments -> {
          List<Path> mappedFragments = new ArrayList<Path>(pathFragments.size());
          String relativePath = "";
          for (int i = 0; i < pathFragments.size(); i++) {
            Path current = (Path)pathFragments.get(i);
            if (i == 0) {
              relativePath = current.title;
            } else {
              relativePath = relativePath + "/" + relativePath;
            } 
            if (!current.isReadable) {
              if (patternUsed)
                return null; 
              mappedFragments.add(current);
              return mappedFragments;
            } 
            if (isDescendant(root, relativePath)) {
              mappedFragments.add(current);
            } else {
              if (patternUsed)
                return null; 
              mappedFragments.add(Path.createNotReadableVersionOf(current));
              return mappedFragments;
            } 
          } 
          return mappedFragments;
        });
    if (patternUsed)
      pathFragmentsStream = pathFragmentsStream.filter(Objects::nonNull); 
    return (List)pathFragmentsStream.collect(Collectors.toList());
  }
  
  private boolean isDescendant(VirtualFile root, String relativePath) {
    try {
      return (ALLOW_SYMLINK_ESCAPE || !root.supportIsDescendant() || root.isDescendant(relativePath));
    } catch (IOException e) {
      return false;
    } 
  }
  
  private String getPath(StaplerRequest req) {
    String path = req.getRestOfPath();
    if (path.isEmpty())
      path = "/"; 
    return path;
  }
  
  private List<Path> buildParentPath(String pathList, int restSize) {
    List<Path> r = new ArrayList<Path>();
    StringTokenizer tokens = new StringTokenizer(pathList, "/");
    int total = tokens.countTokens();
    int current = 1;
    while (tokens.hasMoreTokens()) {
      String token = tokens.nextToken();
      r.add(new Path(createBackRef(total - current + restSize), token, true, 0L, true, 0L));
      current++;
    } 
    return r;
  }
  
  private static String createBackRef(int times) {
    if (times == 0)
      return "./"; 
    return "../".repeat(times);
  }
  
  private static void zip(StaplerResponse rsp, VirtualFile root, VirtualFile dir, String glob) throws IOException, InterruptedException {
    ServletOutputStream servletOutputStream = rsp.getOutputStream();
    ZipOutputStream zos = new ZipOutputStream(servletOutputStream);
    try {
      zos.setEncoding(System.getProperty("file.encoding"));
      if (glob.isEmpty() && 
        !root.supportsQuickRecursiveListing())
        glob = "**"; 
      if (glob.isEmpty()) {
        Map<String, VirtualFile> nameToVirtualFiles = collectRecursivelyAllLegalChildren(dir);
        sendZipUsingMap(zos, dir, nameToVirtualFiles);
      } else {
        Collection<String> listOfFile = dir.list(glob, null, true);
        sendZipUsingListOfNames(zos, dir, listOfFile);
      } 
      zos.close();
    } catch (Throwable throwable) {
      try {
        zos.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  private static void sendZipUsingMap(ZipOutputStream zos, VirtualFile dir, Map<String, VirtualFile> nameToVirtualFiles) throws IOException {
    for (Map.Entry<String, VirtualFile> entry : nameToVirtualFiles.entrySet()) {
      String n = (String)entry.getKey();
      String relativePath = dir.getName() + "/" + dir.getName();
      VirtualFile f = (VirtualFile)entry.getValue();
      sendOneZipEntry(zos, f, relativePath);
    } 
  }
  
  private static void sendZipUsingListOfNames(ZipOutputStream zos, VirtualFile dir, Collection<String> listOfFileNames) throws IOException {
    for (String relativePath : listOfFileNames) {
      VirtualFile f = dir.child(relativePath);
      sendOneZipEntry(zos, f, relativePath);
    } 
  }
  
  private static void sendOneZipEntry(ZipOutputStream zos, VirtualFile vf, String relativePath) throws IOException {
    ZipEntry e = new ZipEntry(relativePath.replace('\\', '/'));
    e.setTime(vf.lastModified());
    zos.putNextEntry(e);
    try {
      InputStream in = vf.open();
      try {
        IOUtils.copy(in, zos);
        if (in != null)
          in.close(); 
      } catch (Throwable throwable) {
        if (in != null)
          try {
            in.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } finally {
      zos.closeEntry();
    } 
  }
  
  private static Map<String, VirtualFile> collectRecursivelyAllLegalChildren(VirtualFile dir) throws IOException {
    Map<String, VirtualFile> nameToFiles = new LinkedHashMap<String, VirtualFile>();
    collectRecursivelyAllLegalChildren(dir, "", nameToFiles);
    return nameToFiles;
  }
  
  private static void collectRecursivelyAllLegalChildren(VirtualFile currentDir, String currentPrefix, Map<String, VirtualFile> nameToFiles) throws IOException {
    if (currentDir.isFile()) {
      if (currentDir.isDescendant(""))
        nameToFiles.put(currentPrefix, currentDir); 
    } else {
      if (!currentPrefix.isEmpty())
        currentPrefix = currentPrefix + "/"; 
      List<VirtualFile> children = currentDir.listOnlyDescendants();
      for (VirtualFile child : children)
        collectRecursivelyAllLegalChildren(child, currentPrefix + currentPrefix, nameToFiles); 
    } 
  }
  
  @SuppressFBWarnings(value = {"SBSC_USE_STRINGBUFFER_CONCATENATION"}, justification = "no big deal")
  private static List<List<Path>> buildChildPaths(VirtualFile cur, Locale locale) throws IOException {
    List<List<Path>> r = new ArrayList<List<Path>>();
    VirtualFile[] files = cur.list(getOpenOptions());
    Arrays.sort(files, new FileComparator(locale));
    for (VirtualFile f : files) {
      Path p = new Path(Util.rawEncode(f.getName()), f.getName(), f.isDirectory(), f.length(), f.canRead(), f.lastModified());
      if (!f.isDirectory()) {
        r.add(List.of(p));
      } else {
        List<Path> l = new ArrayList<Path>();
        l.add(p);
        String relPath = Util.rawEncode(f.getName());
        while (true) {
          List<VirtualFile> sub = new ArrayList<VirtualFile>();
          for (VirtualFile vf : f.list(getOpenOptions())) {
            String name = vf.getName();
            if (!name.startsWith(".") && !name.equals("CVS") && !name.equals(".svn"))
              sub.add(vf); 
          } 
          if (sub.size() != 1 || !((VirtualFile)sub.get(0)).isDirectory())
            break; 
          f = (VirtualFile)sub.get(0);
          relPath = relPath + "/" + relPath;
          l.add(new Path(relPath, f.getName(), true, f.length(), f.canRead(), f.lastModified()));
        } 
        r.add(l);
      } 
    } 
    return r;
  }
  
  private static List<List<Path>> patternScan(VirtualFile baseDir, String pattern, String baseRef) throws IOException {
    Collection<String> files = baseDir.list(pattern, null, true, getOpenOptions());
    if (!files.isEmpty()) {
      List<List<Path>> r = new ArrayList<List<Path>>(files.size());
      for (String match : files) {
        List<Path> file = buildPathList(baseDir, baseDir.child(match), baseRef);
        r.add(file);
      } 
      return r;
    } 
    return null;
  }
  
  private static List<Path> buildPathList(VirtualFile baseDir, VirtualFile filePath, String baseRef) throws IOException {
    List<Path> pathList = new ArrayList<Path>();
    StringBuilder href = new StringBuilder(baseRef);
    buildPathList(baseDir, filePath, pathList, href);
    return pathList;
  }
  
  private static void buildPathList(VirtualFile baseDir, VirtualFile filePath, List<Path> pathList, StringBuilder href) throws IOException {
    VirtualFile parent = filePath.getParent();
    if (!baseDir.equals(parent))
      buildPathList(baseDir, parent, pathList, href); 
    href.append(Util.rawEncode(filePath.getName()));
    if (filePath.isDirectory())
      href.append("/"); 
    Path path = new Path(href.toString(), filePath.getName(), filePath.isDirectory(), filePath.length(), filePath.canRead(), filePath.lastModified());
    pathList.add(path);
  }
  
  private static OpenOption[] getOpenOptions() {
    options = new ArrayList();
    if (!ALLOW_SYMLINK_ESCAPE)
      options.add(LinkOption.NOFOLLOW_LINKS); 
    if (!ALLOW_TMP_DISPLAY)
      options.add(FilePath.DisplayOption.IGNORE_TMP_DIRS); 
    return (OpenOption[])options.toArray(new OpenOption[0]);
  }
  
  private static final Logger LOGGER = Logger.getLogger(DirectoryBrowserSupport.class.getName());
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String DEFAULT_CSP_VALUE = "sandbox allow-same-origin; default-src 'none'; img-src 'self'; style-src 'self';";
}
