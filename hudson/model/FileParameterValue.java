package hudson.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.tasks.BuildWrapper;
import hudson.util.VariableResolver;
import java.io.File;
import java.util.regex.Pattern;
import jenkins.util.SystemProperties;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

public class FileParameterValue extends ParameterValue {
  private static final String FOLDER_NAME = "fileParameters";
  
  private static final Pattern PROHIBITED_DOUBLE_DOT = Pattern.compile(".*[\\\\/]\\.\\.[\\\\/].*");
  
  private static final long serialVersionUID = -143427023159076073L;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean ALLOW_FOLDER_TRAVERSAL_OUTSIDE_WORKSPACE = SystemProperties.getBoolean(FileParameterValue.class.getName() + ".allowFolderTraversalOutsideWorkspace");
  
  private final FileItem file;
  
  private final String originalFileName;
  
  private String location;
  
  @DataBoundConstructor
  public FileParameterValue(String name, FileItem file) { this(name, file, FilenameUtils.getName(file.getName())); }
  
  public FileParameterValue(String name, File file, String originalFileName) { this(name, new FileItemImpl(file), originalFileName); }
  
  protected FileParameterValue(String name, FileItem file, String originalFileName) {
    super(name);
    this.file = file;
    this.originalFileName = originalFileName;
    setLocation(name);
  }
  
  protected void setLocation(String location) { this.location = location; }
  
  public String getLocation() { return this.location; }
  
  public Object getValue() { return this.file; }
  
  public void buildEnvironment(Run<?, ?> build, EnvVars env) { env.put(this.name, this.originalFileName); }
  
  public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) { return name -> this.name.equals(name) ? this.originalFileName : null; }
  
  public String getOriginalFileName() { return this.originalFileName; }
  
  public FileItem getFile() { return this.file; }
  
  public BuildWrapper createBuildWrapper(AbstractBuild<?, ?> build) { return new Object(this); }
  
  public int hashCode() {
    int prime = 31;
    result = super.hashCode();
    return 31 * result + ((this.location == null) ? 0 : this.location.hashCode());
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (!super.equals(obj))
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    FileParameterValue other = (FileParameterValue)obj;
    if (this.location == null && other.location == null)
      return true; 
    return false;
  }
  
  public String toString() { return "(FileParameterValue) " + getName() + "='" + this.originalFileName + "'"; }
  
  public String getShortDescription() { return this.name + "=" + this.name; }
  
  public DirectoryBrowserSupport doDynamic(StaplerRequest request, StaplerResponse response) {
    AbstractBuild build = (AbstractBuild)request.findAncestor(AbstractBuild.class).getObject();
    File fileParameter = getFileParameterFolderUnderBuild(build);
    return new DirectoryBrowserSupport(build, new FilePath(fileParameter), Messages.FileParameterValue_IndexTitle(), "folder.png", false);
  }
  
  private File getLocationUnderBuild(AbstractBuild build) { return new File(getFileParameterFolderUnderBuild(build), this.location); }
  
  private File getFileParameterFolderUnderBuild(AbstractBuild<?, ?> build) { return new File(build.getRootDir(), "fileParameters"); }
}
