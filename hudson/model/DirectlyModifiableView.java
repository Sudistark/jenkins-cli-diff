package hudson.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;

public interface DirectlyModifiableView {
  boolean remove(@NonNull TopLevelItem paramTopLevelItem) throws IOException, IllegalArgumentException;
  
  void add(@NonNull TopLevelItem paramTopLevelItem) throws IOException, IllegalArgumentException;
  
  HttpResponse doAddJobToView(@QueryParameter String paramString) throws IOException, ServletException;
  
  HttpResponse doRemoveJobFromView(@QueryParameter String paramString) throws IOException, ServletException;
}
