package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.cli.CLICommand;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class FileParameterDefinition extends ParameterDefinition {
  @DataBoundConstructor
  public FileParameterDefinition(@NonNull String name) { super(name); }
  
  public FileParameterDefinition(@NonNull String name, @CheckForNull String description) {
    this(name);
    setDescription(description);
  }
  
  public FileParameterValue createValue(StaplerRequest req, JSONObject jo) {
    FileParameterValue p = (FileParameterValue)req.bindJSON(FileParameterValue.class, jo);
    p.setLocation(getName());
    p.setDescription(getDescription());
    return p;
  }
  
  public ParameterValue createValue(StaplerRequest req) {
    FileItem src;
    try {
      src = req.getFileItem(getName());
    } catch (ServletException|IOException e) {
      return null;
    } 
    if (src == null)
      return null; 
    FileParameterValue p = new FileParameterValue(getName(), src, getFileName(src.getName()));
    p.setDescription(getDescription());
    p.setLocation(getName());
    return p;
  }
  
  private String getFileName(String possiblyPathName) {
    possiblyPathName = possiblyPathName.substring(possiblyPathName.lastIndexOf('/') + 1);
    return possiblyPathName.substring(possiblyPathName.lastIndexOf('\\') + 1);
  }
  
  public ParameterValue createValue(CLICommand command, String value) throws IOException, InterruptedException {
    String name;
    File local = Files.createTempFile("jenkins", "parameter", new java.nio.file.attribute.FileAttribute[0]).toFile();
    if (value.isEmpty()) {
      FileUtils.copyInputStreamToFile(command.stdin, local);
      name = "stdin";
    } else {
      command.checkChannel();
      return null;
    } 
    FileParameterValue p = new FileParameterValue(getName(), local, name);
    p.setDescription(getDescription());
    p.setLocation(getName());
    return p;
  }
  
  public int hashCode() {
    if (FileParameterDefinition.class != getClass())
      return super.hashCode(); 
    return Objects.hash(new Object[] { getName(), getDescription() });
  }
  
  @SuppressFBWarnings(value = {"EQ_GETCLASS_AND_CLASS_CONSTANT"}, justification = "ParameterDefinitionTest tests that subclasses are not equal to their parent classes, so the behavior appears to be intentional")
  public boolean equals(Object obj) {
    if (FileParameterDefinition.class != getClass())
      return super.equals(obj); 
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    FileParameterDefinition other = (FileParameterDefinition)obj;
    if (!Objects.equals(getName(), other.getName()))
      return false; 
    return Objects.equals(getDescription(), other.getDescription());
  }
}
