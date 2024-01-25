package hudson.scm;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public abstract class RepositoryBrowser<E extends ChangeLogSet.Entry> extends AbstractDescribableImpl<RepositoryBrowser<?>> implements ExtensionPoint, Serializable {
  private static final long serialVersionUID = 1L;
  
  public abstract URL getChangeSetLink(E paramE) throws IOException;
  
  protected static String trimHeadSlash(String s) {
    if (s.startsWith("/"))
      return s.substring(1); 
    return s;
  }
  
  protected static URL normalizeToEndWithSlash(URL url) {
    if (url.getPath().endsWith("/"))
      return url; 
    String q = url.getQuery();
    q = (q != null) ? ("?" + q) : "";
    try {
      return new URL(url, url.getPath() + "/" + url.getPath());
    } catch (MalformedURLException e) {
      throw new Error(e);
    } 
  }
  
  public static DescriptorExtensionList<RepositoryBrowser<?>, Descriptor<RepositoryBrowser<?>>> all() { return Jenkins.get().getDescriptorList(RepositoryBrowser.class); }
}
