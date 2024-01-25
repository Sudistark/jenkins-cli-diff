package jenkins.model.item_category;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Category implements Serializable {
  private String id;
  
  private String name;
  
  private String description;
  
  private int order;
  
  private int minToShow;
  
  private List<Map<String, Serializable>> items;
  
  public Category(String id, String name, String description, int order, int minToShow, List<Map<String, Serializable>> items) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.order = order;
    this.minToShow = minToShow;
    this.items = items;
  }
  
  @Exported
  public String getId() { return this.id; }
  
  @Exported
  public String getName() { return this.name; }
  
  @Exported
  public String getDescription() { return this.description; }
  
  @Exported
  public int getOrder() { return this.order; }
  
  @Exported
  public int getMinToShow() { return this.minToShow; }
  
  @Exported
  public List<Map<String, Serializable>> getItems() { return this.items; }
}
