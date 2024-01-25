package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import hudson.XmlFile;
import hudson.model.listeners.ItemListener;
import hudson.security.AccessControlled;
import hudson.util.CopyOnWriteMap;
import hudson.util.Function1;
import hudson.util.Secret;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jenkins.model.Jenkins;
import jenkins.util.xml.XMLUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.security.access.AccessDeniedException;

public abstract class ItemGroupMixIn {
  private final ItemGroup parent;
  
  private final AccessControlled acl;
  
  protected ItemGroupMixIn(ItemGroup parent, AccessControlled acl) {
    this.parent = parent;
    this.acl = acl;
  }
  
  protected abstract void add(TopLevelItem paramTopLevelItem);
  
  protected abstract File getRootDirFor(String paramString);
  
  public static <K, V extends Item> Map<K, V> loadChildren(ItemGroup parent, File modulesDir, Function1<? extends K, ? super V> key) {
    try {
      Util.createDirectories(modulesDir.toPath(), new java.nio.file.attribute.FileAttribute[0]);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } 
    File[] subdirs = modulesDir.listFiles(File::isDirectory);
    CopyOnWriteMap.Tree<K, V> configurations = new CopyOnWriteMap.Tree<K, V>();
    File[] arrayOfFile = subdirs;
    int i = arrayOfFile.length;
    byte b = 0;
    while (true) {
      if (b < i) {
        File subdir = arrayOfFile[b];
        try {
          V item = (V)parent.getItem(subdir.getName());
          if (item == null) {
            XmlFile xmlFile = Items.getConfigFile(subdir);
            if (xmlFile.exists()) {
              item = (V)Items.load(parent, subdir);
            } else {
              Logger.getLogger(ItemGroupMixIn.class.getName()).log(Level.WARNING, "could not find file " + xmlFile.getFile());
              b++;
            } 
          } else {
            item.onLoad(parent, subdir.getName());
          } 
          configurations.put(key.call(item), item);
        } catch (Exception e) {
          Logger.getLogger(ItemGroupMixIn.class.getName()).log(Level.WARNING, "could not load " + subdir, e);
        } 
      } else {
        break;
      } 
      b++;
    } 
    return configurations;
  }
  
  public static final Function1<String, Item> KEYED_BY_NAME = Item::getName;
  
  public TopLevelItem createTopLevelItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    TopLevelItem result;
    this.acl.checkPermission(Item.CREATE);
    String requestContentType = req.getContentType();
    String mode = req.getParameter("mode");
    if (requestContentType == null && (mode == null || 
      !mode.equals("copy")))
      throw new Failure("No Content-Type header set"); 
    boolean isXmlSubmission = (requestContentType != null && (requestContentType.startsWith("application/xml") || requestContentType.startsWith("text/xml")));
    String name = req.getParameter("name");
    if (name == null)
      throw new Failure("Query parameter 'name' is required"); 
    Jenkins.checkGoodName(name);
    name = name.trim();
    if (this.parent.getItem(name) != null)
      throw new Failure(Messages.Hudson_JobAlreadyExists(name)); 
    if (mode != null && mode.equals("copy")) {
      String from = req.getParameter("from");
      Item src = Jenkins.get().getItem(from, this.parent);
      if (src == null) {
        if (Util.fixEmpty(from) == null)
          throw new Failure("Specify which job to copy"); 
        throw new Failure("No such job: " + from);
      } 
      if (!(src instanceof TopLevelItem))
        throw new Failure(from + " cannot be copied"); 
      result = copy((TopLevelItem)src, name);
    } else {
      if (isXmlSubmission) {
        TopLevelItem result = createProjectFromXML(name, req.getInputStream());
        rsp.setStatus(200);
        return result;
      } 
      if (mode == null)
        throw new Failure("No mode given"); 
      TopLevelItemDescriptor descriptor = (TopLevelItemDescriptor)Items.all().findByName(mode);
      if (descriptor == null)
        throw new Failure("No item type ‘" + mode + "’ is known"); 
      descriptor.checkApplicableIn(this.parent);
      this.acl.getACL().checkCreatePermission(this.parent, descriptor);
      result = createProject(descriptor, name, true);
    } 
    rsp.sendRedirect2(redirectAfterCreateItem(req, result));
    return result;
  }
  
  protected String redirectAfterCreateItem(StaplerRequest req, TopLevelItem result) throws IOException {
    return req.getContextPath() + "/" + req.getContextPath() + "configure";
  }
  
  public <T extends TopLevelItem> T copy(T src, String name) throws IOException {
    this.acl.checkPermission(Item.CREATE);
    src.checkPermission(Item.EXTENDED_READ);
    XmlFile srcConfigFile = Items.getConfigFile(src);
    if (!src.hasPermission(Item.CONFIGURE)) {
      Matcher matcher = AbstractItem.SECRET_PATTERN.matcher(srcConfigFile.asString());
      while (matcher.find()) {
        if (Secret.decrypt(matcher.group(true)) != null)
          throw new AccessDeniedException(
              Messages.ItemGroupMixIn_may_not_copy_as_it_contains_secrets_and_(src
                .getFullName(), 
                Jenkins.getAuthentication2().getName(), Item.PERMISSIONS.title, Item.EXTENDED_READ.name, Item.CONFIGURE.name)); 
      } 
    } 
    src.getDescriptor().checkApplicableIn(this.parent);
    this.acl.getACL().checkCreatePermission(this.parent, src.getDescriptor());
    Jenkins.checkGoodName(name);
    ItemListener.checkBeforeCopy(src, this.parent);
    T result = (T)createProject(src.getDescriptor(), name, false);
    Files.copy(Util.fileToPath(srcConfigFile.getFile()), Util.fileToPath(Items.getConfigFile(result).getFile()), new CopyOption[] { StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING });
    File rootDir = result.getRootDir();
    result = (T)(TopLevelItem)Items.whileUpdatingByXml(new Object(this, rootDir));
    result.onCopiedFrom(src);
    add(result);
    ItemListener.fireOnCopied(src, result);
    Jenkins.get().rebuildDependencyGraphAsync();
    return result;
  }
  
  public TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException {
    this.acl.checkPermission(Item.CREATE);
    Jenkins.get().getProjectNamingStrategy().checkName(this.parent.getFullName(), name);
    Items.verifyItemDoesNotAlreadyExist(this.parent, name, null);
    Jenkins.checkGoodName(name);
    File configXml = Items.getConfigFile(getRootDirFor(name)).getFile();
    dir = configXml.getParentFile();
    success = false;
    try {
      Util.createDirectories(dir.toPath(), new java.nio.file.attribute.FileAttribute[0]);
      XMLUtils.safeTransform(new StreamSource(xml), new StreamResult(configXml));
      TopLevelItem result = (TopLevelItem)Items.whileUpdatingByXml(new Object(this, dir));
      success = (this.acl.getACL().hasCreatePermission2(Jenkins.getAuthentication2(), this.parent, result.getDescriptor()) && result.getDescriptor().isApplicableIn(this.parent));
      add(result);
      ItemListener.fireOnCreated(result);
      Jenkins.get().rebuildDependencyGraphAsync();
      return result;
    } catch (TransformerException|org.xml.sax.SAXException e) {
      success = false;
      throw new IOException("Failed to persist config.xml", e);
    } catch (IOException|RuntimeException e) {
      success = false;
      throw e;
    } finally {
      if (!success)
        Util.deleteRecursive(dir); 
    } 
  }
  
  @NonNull
  public TopLevelItem createProject(@NonNull TopLevelItemDescriptor type, @NonNull String name, boolean notify) throws IOException {
    this.acl.checkPermission(Item.CREATE);
    type.checkApplicableIn(this.parent);
    this.acl.getACL().checkCreatePermission(this.parent, type);
    Jenkins.get().getProjectNamingStrategy().checkName(this.parent.getFullName(), name);
    Items.verifyItemDoesNotAlreadyExist(this.parent, name, null);
    Jenkins.checkGoodName(name);
    TopLevelItem item = type.newInstance(this.parent, name);
    item.onCreatedFromScratch();
    item.save();
    add(item);
    Jenkins.get().rebuildDependencyGraphAsync();
    if (notify)
      ItemListener.fireOnCreated(item); 
    return item;
  }
}
