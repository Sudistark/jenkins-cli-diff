package hudson.model;

import com.thoughtworks.xstream.XStream;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.DescriptorExtensionList;
import hudson.XmlFile;
import hudson.model.listeners.ItemListener;
import hudson.remoting.Callable;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.AccessControlled;
import hudson.util.DescriptorList;
import hudson.util.EditDistance;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import jenkins.model.DirectlyModifiableTopLevelItemGroup;
import jenkins.model.Jenkins;
import jenkins.util.MemoryReductionUtil;
import org.acegisecurity.Authentication;
import org.apache.commons.io.FileUtils;
import org.springframework.security.core.Authentication;

public class Items {
  @Deprecated
  public static final List<TopLevelItemDescriptor> LIST = new DescriptorList(TopLevelItem.class);
  
  private static final ThreadLocal<Boolean> updatingByXml = ThreadLocal.withInitial(() -> Boolean.valueOf(false));
  
  public static final Comparator<Item> BY_NAME = new Object();
  
  public static final Comparator<Item> BY_FULL_NAME = new Object();
  
  public static <V, T extends Throwable> V whileUpdatingByXml(Callable<V, T> callable) throws T {
    updatingByXml.set(Boolean.valueOf(true));
    try {
      object = callable.call();
      return (V)object;
    } finally {
      updatingByXml.set(Boolean.valueOf(false));
    } 
  }
  
  public static boolean currentlyUpdatingByXml() { return ((Boolean)updatingByXml.get()).booleanValue(); }
  
  public static DescriptorExtensionList<TopLevelItem, TopLevelItemDescriptor> all() { return Jenkins.get().getDescriptorList(TopLevelItem.class); }
  
  public static List<TopLevelItemDescriptor> all(ItemGroup c) { return all2(Jenkins.getAuthentication2(), c); }
  
  public static List<TopLevelItemDescriptor> all2(Authentication a, ItemGroup c) {
    ACL acl;
    List<TopLevelItemDescriptor> result = new ArrayList<TopLevelItemDescriptor>();
    if (c instanceof AccessControlled) {
      acl = ((AccessControlled)c).getACL();
    } else {
      acl = Jenkins.get().getACL();
    } 
    for (TopLevelItemDescriptor d : all()) {
      if (acl.hasCreatePermission2(a, c, d) && d.isApplicableIn(c))
        result.add(d); 
    } 
    return result;
  }
  
  @Deprecated
  public static List<TopLevelItemDescriptor> all(Authentication a, ItemGroup c) { return all2(a.toSpring(), c); }
  
  @Deprecated
  public static TopLevelItemDescriptor getDescriptor(String fqcn) { return (TopLevelItemDescriptor)Descriptor.find(all(), fqcn); }
  
  public static String toNameList(Collection<? extends Item> items) {
    StringBuilder buf = new StringBuilder();
    for (Item item : items) {
      if (buf.length() > 0)
        buf.append(", "); 
      buf.append(item.getFullName());
    } 
    return buf.toString();
  }
  
  @Deprecated
  public static <T extends Item> List<T> fromNameList(String list, Class<T> type) { return fromNameList(null, list, type); }
  
  public static <T extends Item> List<T> fromNameList(ItemGroup context, @NonNull String list, @NonNull Class<T> type) {
    Jenkins jenkins = Jenkins.get();
    List<T> r = new ArrayList<T>();
    StringTokenizer tokens = new StringTokenizer(list, ",");
    while (tokens.hasMoreTokens()) {
      String fullName = tokens.nextToken().trim();
      if (fullName != null && !fullName.isEmpty()) {
        T item = (T)jenkins.getItem(fullName, context, type);
        if (item != null)
          r.add(item); 
      } 
    } 
    return r;
  }
  
  public static String getCanonicalName(ItemGroup context, String path) {
    String[] c = context.getFullName().split("/");
    String[] p = path.split("/");
    Stack<String> name = new Stack<String>();
    for (int i = 0; i < c.length; i++) {
      if (i != 0 || !c[i].isEmpty())
        name.push(c[i]); 
    } 
    for (int i = 0; i < p.length; i++) {
      if (i == 0 && p[i].isEmpty()) {
        name.clear();
      } else if (p[i].equals("..")) {
        if (name.isEmpty())
          throw new IllegalArgumentException(String.format("Illegal relative path '%s' within context '%s'", new Object[] { path, context
                  .getFullName() })); 
        name.pop();
      } else if (!p[i].equals(".")) {
        name.push(p[i]);
      } 
    } 
    return String.join("/", name);
  }
  
  public static String computeRelativeNamesAfterRenaming(String oldFullName, String newFullName, String relativeNames, ItemGroup context) {
    StringTokenizer tokens = new StringTokenizer(relativeNames, ",");
    List<String> newValue = new ArrayList<String>();
    while (tokens.hasMoreTokens()) {
      String relativeName = tokens.nextToken().trim();
      String canonicalName = getCanonicalName(context, relativeName);
      if (canonicalName.equals(oldFullName) || canonicalName.startsWith(oldFullName + "/")) {
        String newCanonicalName = newFullName + newFullName;
        if (relativeName.startsWith("/")) {
          newValue.add("/" + newCanonicalName);
          continue;
        } 
        newValue.add(getRelativeNameFrom(newCanonicalName, context.getFullName()));
        continue;
      } 
      newValue.add(relativeName);
    } 
    return String.join(",", newValue);
  }
  
  static String getRelativeNameFrom(String itemFullName, String groupFullName) {
    String[] itemFullNameA = itemFullName.isEmpty() ? MemoryReductionUtil.EMPTY_STRING_ARRAY : itemFullName.split("/");
    String[] groupFullNameA = groupFullName.isEmpty() ? MemoryReductionUtil.EMPTY_STRING_ARRAY : groupFullName.split("/");
    int i = 0;
    while (true) {
      if (i == itemFullNameA.length) {
        if (i == groupFullNameA.length)
          return "."; 
        StringBuilder b = new StringBuilder();
        for (int j = 0; j < groupFullNameA.length - itemFullNameA.length; j++) {
          if (j > 0)
            b.append('/'); 
          b.append("..");
        } 
        return b.toString();
      } 
      if (i == groupFullNameA.length) {
        StringBuilder b = new StringBuilder();
        for (int j = i; j < itemFullNameA.length; j++) {
          if (j > i)
            b.append('/'); 
          b.append(itemFullNameA[j]);
        } 
        return b.toString();
      } 
      if (itemFullNameA[i].equals(groupFullNameA[i])) {
        i++;
        continue;
      } 
      StringBuilder b = new StringBuilder();
      for (int j = i; j < groupFullNameA.length; j++) {
        if (j > i)
          b.append('/'); 
        b.append("..");
      } 
      for (int j = i; j < itemFullNameA.length; j++)
        b.append('/').append(itemFullNameA[j]); 
      return b.toString();
    } 
  }
  
  public static Item load(ItemGroup parent, File dir) throws IOException {
    Item item = (Item)getConfigFile(dir).read();
    item.onLoad(parent, dir.getName());
    return item;
  }
  
  public static XmlFile getConfigFile(File dir) { return new XmlFile(XSTREAM, new File(dir, "config.xml")); }
  
  public static XmlFile getConfigFile(Item item) { return getConfigFile(item.getRootDir()); }
  
  public static <T extends Item> List<T> getAllItems(ItemGroup root, Class<T> type) { return getAllItems(root, type, t -> true); }
  
  public static <T extends Item> List<T> getAllItems(ItemGroup root, Class<T> type, Predicate<T> pred) {
    List<T> r = new ArrayList<T>();
    getAllItems(root, type, r, pred);
    return r;
  }
  
  private static <T extends Item> void getAllItems(ItemGroup root, Class<T> type, List<T> r, Predicate<T> pred) {
    List<Item> items = new ArrayList<Item>(root.getItems(t -> (t instanceof ItemGroup || (type.isInstance(t) && pred.test((Item)type.cast(t))))));
    items.sort(BY_NAME);
    for (Item i : items) {
      if (type.isInstance(i) && pred.test((Item)type.cast(i)) && 
        i.hasPermission(Item.READ))
        r.add((Item)type.cast(i)); 
      if (i instanceof ItemGroup)
        getAllItems((ItemGroup)i, type, r, pred); 
    } 
  }
  
  public static <T extends Item> Iterable<T> allItems(ItemGroup root, Class<T> type) { return allItems2(Jenkins.getAuthentication2(), root, type); }
  
  public static <T extends Item> Iterable<T> allItems(ItemGroup root, Class<T> type, Predicate<T> pred) { return allItems2(Jenkins.getAuthentication2(), root, type, pred); }
  
  public static <T extends Item> Iterable<T> allItems2(Authentication authentication, ItemGroup root, Class<T> type) { return allItems2(authentication, root, type, t -> true); }
  
  @Deprecated
  public static <T extends Item> Iterable<T> allItems(Authentication authentication, ItemGroup root, Class<T> type) { return allItems2(authentication.toSpring(), root, type); }
  
  public static <T extends Item> Iterable<T> allItems2(Authentication authentication, ItemGroup root, Class<T> type, Predicate<T> pred) { return new AllItemsIterable(root, authentication, type, pred); }
  
  @Deprecated
  public static <T extends Item> Iterable<T> allItems(Authentication authentication, ItemGroup root, Class<T> type, Predicate<T> pred) { return allItems2(authentication.toSpring(), root, type, pred); }
  
  @CheckForNull
  public static <T extends Item> T findNearest(Class<T> type, String name, ItemGroup context) {
    List<String> names = new ArrayList<String>();
    for (Iterator iterator = Jenkins.get().allItems(type).iterator(); iterator.hasNext(); ) {
      T item = (T)(Item)iterator.next();
      names.add(item.getRelativeNameFrom(context));
    } 
    String nearest = EditDistance.findNearest(name, names);
    return (T)Jenkins.get().getItem(nearest, context, type);
  }
  
  public static <I extends AbstractItem & TopLevelItem> I move(I item, DirectlyModifiableTopLevelItemGroup destination) throws IOException, IllegalArgumentException {
    DirectlyModifiableTopLevelItemGroup oldParent = (DirectlyModifiableTopLevelItemGroup)item.getParent();
    if (oldParent == destination)
      throw new IllegalArgumentException(); 
    if (!destination.canAdd((TopLevelItem)item))
      throw new IllegalArgumentException(); 
    String name = item.getName();
    verifyItemDoesNotAlreadyExist(destination, name, null);
    String oldFullName = item.getFullName();
    File destDir = destination.getRootDirFor((TopLevelItem)item);
    FileUtils.forceMkdir(destDir.getParentFile());
    FileUtils.moveDirectory(item.getRootDir(), destDir);
    oldParent.remove((TopLevelItem)item);
    I newItem = (I)(AbstractItem)destination.add(item, name);
    item.movedTo(destination, newItem, destDir);
    ItemListener.fireLocationChange(newItem, oldFullName);
    return newItem;
  }
  
  static void verifyItemDoesNotAlreadyExist(@NonNull ItemGroup<?> parent, @NonNull String newName, @CheckForNull Item variant) throws IllegalArgumentException, Failure {
    Item existing;
    ACLContext ctxt = ACL.as2(ACL.SYSTEM2);
    try {
      existing = parent.getItem(newName);
      if (ctxt != null)
        ctxt.close(); 
    } catch (Throwable throwable) {
      if (ctxt != null)
        try {
          ctxt.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
    if (existing != null && existing != variant) {
      if (existing.hasPermission(Item.DISCOVER)) {
        String prefix = parent.getFullName();
        throw new IllegalArgumentException((prefix.isEmpty() ? "" : (prefix + "/")) + (prefix.isEmpty() ? "" : (prefix + "/")) + " already exists");
      } 
      throw new Failure("");
    } 
  }
  
  public static final XStream XSTREAM = new XStream2();
  
  public static final XStream2 XSTREAM2 = (XStream2)XSTREAM;
  
  static  {
    XSTREAM.alias("project", FreeStyleProject.class);
  }
}
