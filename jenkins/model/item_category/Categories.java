package jenkins.model.item_category;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;

@ExportedBean
@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class Categories implements HttpResponse, Serializable {
  private List<Category> items = new ArrayList();
  
  @Exported(name = "categories")
  public List<Category> getItems() { return this.items; }
  
  public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException { rsp.serveExposedBean(req, this, Flavor.JSON); }
  
  @CheckForNull
  public Category getItem(@NonNull String id) {
    for (Category category : this.items) {
      if (category.getId().equals(id))
        return category; 
    } 
    return null;
  }
}
