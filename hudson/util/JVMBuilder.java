package hudson.util;

import hudson.FilePath;
import hudson.Launcher;
import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class JVMBuilder implements Serializable {
  private final ClasspathBuilder classpath = new ClasspathBuilder();
  
  private final Map<String, String> systemProperties = new TreeMap();
  
  private final ArgumentListBuilder args = new ArgumentListBuilder();
  
  private final ArgumentListBuilder vmopts = new ArgumentListBuilder();
  
  private FilePath pwd;
  
  private String mainClass;
  
  private static final long serialVersionUID = 1L;
  
  public ClasspathBuilder classpath() { return this.classpath; }
  
  public JVMBuilder systemProperty(String key, String value) {
    this.systemProperties.put(key, value);
    return this;
  }
  
  public Map<String, String> systemProperties() { return this.systemProperties; }
  
  public JVMBuilder systemProperties(Map<String, String> props) {
    if (props != null)
      this.systemProperties.putAll(props); 
    return this;
  }
  
  public ArgumentListBuilder args() { return this.args; }
  
  public ArgumentListBuilder vmopts() { return this.vmopts; }
  
  public JVMBuilder pwd(FilePath pwd) {
    this.pwd = pwd;
    return this;
  }
  
  public JVMBuilder debug(int port) {
    this.vmopts.add("-Xrunjdwp:transport=dt_socket,server=y,address=" + port);
    return this;
  }
  
  public JVMBuilder pwd(File pwd) { return pwd(new FilePath(pwd)); }
  
  public JVMBuilder mainClass(String fullyQualifiedClassName) {
    this.mainClass = fullyQualifiedClassName;
    return this;
  }
  
  public JVMBuilder mainClass(Class mainClass) { return mainClass(mainClass.getName()); }
  
  public ArgumentListBuilder toFullArguments() {
    ArgumentListBuilder args = new ArgumentListBuilder();
    args.add(new File(System.getProperty("java.home"), "bin/java"));
    args.addKeyValuePairs("-D", this.systemProperties);
    args.add("-cp").add(this.classpath.toString());
    args.add(this.vmopts.toCommandArray());
    args.add(this.mainClass);
    args.add(this.args.toCommandArray());
    return args;
  }
  
  public Launcher.ProcStarter launch(Launcher launcher) { return launcher.launch().cmds(toFullArguments()).pwd(this.pwd); }
}
