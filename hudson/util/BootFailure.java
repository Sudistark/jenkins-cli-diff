package hudson.util;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import jenkins.util.groovy.GroovyHookScript;
import org.kohsuke.stapler.WebApp;

public abstract class BootFailure extends ErrorObject {
  protected BootFailure() {}
  
  protected BootFailure(Throwable cause) { super(cause); }
  
  public void publish(ServletContext context, @CheckForNull File home) {
    LOGGER.log(Level.SEVERE, "Failed to initialize Jenkins", this);
    WebApp.get(context).setApp(this);
    if (home == null)
      return; 
    (new GroovyHookScript("boot-failure", context, home, BootFailure.class.getClassLoader()))
      .bind("exception", this)
      .bind("home", home)
      .bind("servletContext", context)
      .bind("attempts", loadAttempts(home))
      .run();
  }
  
  protected List<Date> loadAttempts(File home) {
    List<Date> dates = new ArrayList<Date>();
    if (home != null) {
      File f = getBootFailureFile(home);
      try {
        if (f.exists()) {
          BufferedReader failureFileReader = Files.newBufferedReader(f.toPath(), Charset.defaultCharset());
          try {
            SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
            String line;
            while ((line = failureFileReader.readLine()) != null) {
              try {
                dates.add(df.parse(line));
              } catch (Exception exception) {}
            } 
            if (failureFileReader != null)
              failureFileReader.close(); 
          } catch (Throwable throwable) {
            if (failureFileReader != null)
              try {
                failureFileReader.close();
              } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
              }  
            throw throwable;
          } 
        } 
      } catch (IOException|java.nio.file.InvalidPathException e) {
        LOGGER.log(Level.WARNING, "Failed to parse " + f, e);
      } 
    } 
    return dates;
  }
  
  private static final Logger LOGGER = Logger.getLogger(BootFailure.class.getName());
  
  public static File getBootFailureFile(File home) { return new File(home, "failed-boot-attempts.txt"); }
}
