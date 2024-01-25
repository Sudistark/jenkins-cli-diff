package jenkins.diagnosis;

import hudson.Util;
import hudson.util.HttpResponses;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

public class HsErrPidFile {
  private final HsErrPidList owner;
  
  private final File file;
  
  public HsErrPidFile(HsErrPidList owner, File file) {
    this.owner = owner;
    this.file = file;
  }
  
  public String getName() { return this.file.getName(); }
  
  public String getPath() { return this.file.getPath(); }
  
  public long getLastModified() { return this.file.lastModified(); }
  
  public Date getLastModifiedDate() { return new Date(this.file.lastModified()); }
  
  public String getTimeSpanString() { return Util.getTimeSpanString(System.currentTimeMillis() - getLastModified()); }
  
  public HttpResponse doDownload() throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    return HttpResponses.staticResource(this.file);
  }
  
  @RequirePOST
  public HttpResponse doDelete() throws IOException {
    Jenkins.get().checkPermission(Jenkins.ADMINISTER);
    Files.deleteIfExists(Util.fileToPath(this.file));
    this.owner.files.remove(this);
    return HttpResponses.redirectTo("../..");
  }
}
