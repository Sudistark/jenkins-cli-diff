package hudson.util;

import hudson.FilePath;
import hudson.remoting.Which;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ClasspathBuilder implements Serializable {
  private final List<String> args = new ArrayList();
  
  public ClasspathBuilder add(File f) { return add(f.getAbsolutePath()); }
  
  public ClasspathBuilder add(FilePath f) { return add(f.getRemote()); }
  
  public ClasspathBuilder add(String path) {
    this.args.add(path);
    return this;
  }
  
  public ClasspathBuilder addJarOf(Class c) throws IOException { return add(Which.jarFile(c)); }
  
  public ClasspathBuilder addAll(FilePath base, String glob) throws IOException, InterruptedException {
    for (FilePath item : base.list(glob))
      add(item); 
    return this;
  }
  
  public String toString() { return String.join(File.pathSeparator, this.args); }
}
