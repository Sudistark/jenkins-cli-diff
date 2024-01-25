package hudson.scm;

import java.util.List;
import org.kohsuke.stapler.export.CustomExportedBean;

public final class EditType implements CustomExportedBean {
  private String name;
  
  private String description;
  
  public EditType(String name, String description) {
    this.name = name;
    this.description = description;
  }
  
  public String getName() { return this.name; }
  
  public String getDescription() { return this.description; }
  
  public String toExportedObject() { return this.name; }
  
  public static final EditType ADD = new EditType("add", "The file was added");
  
  public static final EditType EDIT = new EditType("edit", "The file was modified");
  
  public static final EditType DELETE = new EditType("delete", "The file was removed");
  
  public static final List<EditType> ALL = List.of(ADD, EDIT, DELETE);
}
