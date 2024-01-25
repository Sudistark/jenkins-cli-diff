package hudson.views;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.View;
import hudson.util.DescriptorList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.export.Exported;

public abstract class ListViewColumn extends Object implements ExtensionPoint, Describable<ListViewColumn> {
  @Exported
  public String getColumnCaption() { return getDescriptor().getDisplayName(); }
  
  public static DescriptorExtensionList<ListViewColumn, Descriptor<ListViewColumn>> all() { return Jenkins.get().getDescriptorList(ListViewColumn.class); }
  
  @Deprecated
  public static final DescriptorList<ListViewColumn> LIST = new DescriptorList(ListViewColumn.class);
  
  @Deprecated
  public boolean shownByDefault() { return true; }
  
  public Descriptor<ListViewColumn> getDescriptor() { return Jenkins.get().getDescriptorOrDie(getClass()); }
  
  @Deprecated
  public static List<ListViewColumn> createDefaultInitialColumnList() { return createDefaultInitialColumnList(all()); }
  
  public static List<ListViewColumn> createDefaultInitialColumnList(Class<? extends View> context) { return createDefaultInitialColumnList(DescriptorVisibilityFilter.applyType(context, all())); }
  
  public static List<ListViewColumn> createDefaultInitialColumnList(View view) { return createDefaultInitialColumnList(DescriptorVisibilityFilter.apply(view, all())); }
  
  private static List<ListViewColumn> createDefaultInitialColumnList(List<Descriptor<ListViewColumn>> descriptors) {
    ArrayList<ListViewColumn> r = new ArrayList<ListViewColumn>();
    JSONObject emptyJSON = new JSONObject();
    for (Descriptor<ListViewColumn> d : descriptors) {
      try {
        if (d instanceof ListViewColumnDescriptor) {
          ListViewColumnDescriptor ld = (ListViewColumnDescriptor)d;
          if (!ld.shownByDefault())
            continue; 
        } 
        ListViewColumn lvc = (ListViewColumn)d.newInstance(null, emptyJSON);
        if (!lvc.shownByDefault())
          continue; 
        r.add(lvc);
      } catch (hudson.model.Descriptor.FormException e) {
        LOGGER.log(Level.WARNING, "Failed to instantiate " + d.clazz, e);
      } 
    } 
    return r;
  }
  
  private static final Logger LOGGER = Logger.getLogger(ListViewColumn.class.getName());
  
  public static final double DEFAULT_COLUMNS_ORDINAL_ICON_START = 60.0D;
  
  public static final double DEFAULT_COLUMNS_ORDINAL_ICON_END = 50.0D;
  
  public static final double DEFAULT_COLUMNS_ORDINAL_PROPERTIES_START = 40.0D;
  
  public static final double DEFAULT_COLUMNS_ORDINAL_PROPERTIES_END = 30.0D;
  
  public static final double DEFAULT_COLUMNS_ORDINAL_ACTIONS_START = 20.0D;
  
  public static final double DEFAULT_COLUMNS_ORDINAL_ACTIONS_END = 10.0D;
}
