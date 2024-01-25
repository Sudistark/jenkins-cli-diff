package hudson.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class MyView extends View {
  @DataBoundConstructor
  public MyView(String name) { super(name); }
  
  public MyView(String name, ViewGroup owner) {
    this(name);
    this.owner = owner;
  }
  
  public boolean contains(TopLevelItem item) { return item.hasPermission(Item.CONFIGURE); }
  
  @RequirePOST
  public TopLevelItem doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
    ItemGroup<? extends TopLevelItem> ig = getOwner().getItemGroup();
    if (ig instanceof ModifiableItemGroup)
      return (TopLevelItem)((ModifiableItemGroup)ig).doCreateItem(req, rsp); 
    return null;
  }
  
  public Collection<TopLevelItem> getItems() {
    List<TopLevelItem> items = new ArrayList<TopLevelItem>(getOwner().getItemGroup().getItems(item -> item.hasPermission(Item.CONFIGURE)));
    return Collections.unmodifiableList(items);
  }
  
  public String getPostConstructLandingPage() { return ""; }
  
  protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {}
}
