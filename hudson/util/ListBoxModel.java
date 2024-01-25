package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.ModelObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.ServletException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;

@ExportedBean
public class ListBoxModel extends ArrayList<ListBoxModel.Option> implements HttpResponse {
  public ListBoxModel(int initialCapacity) { super(initialCapacity); }
  
  public ListBoxModel() {}
  
  public ListBoxModel(Collection<Option> c) { super(c); }
  
  public ListBoxModel(Option... data) { super(Arrays.asList(data)); }
  
  public void add(@NonNull String displayName, @NonNull String value) { add(new Option(displayName, value)); }
  
  public void add(ModelObject usedForDisplayName, @NonNull String value) { add(usedForDisplayName.getDisplayName(), value); }
  
  public ListBoxModel add(@NonNull String nameAndValue) {
    add(nameAndValue, nameAndValue);
    return this;
  }
  
  public void writeTo(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { rsp.serveExposedBean(req, this, Flavor.JSON); }
  
  public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException { writeTo(req, rsp); }
  
  @Exported
  @Deprecated
  public Option[] values() { return (Option[])toArray(new Option[size()]); }
}
