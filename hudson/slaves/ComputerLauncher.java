package hudson.slaves;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.TaskListener;
import hudson.util.DescriptorList;
import hudson.util.StreamTaskListener;
import hudson.util.VersionNumber;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ComputerLauncher extends AbstractDescribableImpl<ComputerLauncher> implements ExtensionPoint {
  public boolean isLaunchSupported() { return true; }
  
  public void launch(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { launch(computer, cast(listener)); }
  
  @Deprecated
  public void launch(SlaveComputer computer, StreamTaskListener listener) throws IOException, InterruptedException {
    throw new UnsupportedOperationException("" + getClass() + " must implement the launch method");
  }
  
  public void afterDisconnect(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { afterDisconnect(computer, cast(listener)); }
  
  @Deprecated
  public void afterDisconnect(SlaveComputer computer, StreamTaskListener listener) throws IOException, InterruptedException {}
  
  public void beforeDisconnect(SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException { beforeDisconnect(computer, cast(listener)); }
  
  @Deprecated
  public void beforeDisconnect(SlaveComputer computer, StreamTaskListener listener) throws IOException, InterruptedException {}
  
  private StreamTaskListener cast(TaskListener listener) {
    if (listener instanceof StreamTaskListener)
      return (StreamTaskListener)listener; 
    return new StreamTaskListener(listener.getLogger());
  }
  
  @Deprecated
  public static final DescriptorList<ComputerLauncher> LIST = new DescriptorList(ComputerLauncher.class);
  
  protected static void checkJavaVersion(PrintStream logger, String javaCommand, BufferedReader r) throws IOException {
    Pattern p = Pattern.compile("(?i)(?:java|openjdk) version \"([0-9.]+).*\".*");
    String line;
    while (null != (line = r.readLine())) {
      Matcher m = p.matcher(line);
      if (m.matches()) {
        String versionStr = m.group(1);
        logger.println(Messages.ComputerLauncher_JavaVersionResult(javaCommand, versionStr));
        try {
          if ((new VersionNumber(versionStr)).isOlderThan(new VersionNumber("1.8")))
            throw new IOException(
                Messages.ComputerLauncher_NoJavaFound(line)); 
        } catch (NumberFormatException x) {
          throw new IOException(Messages.ComputerLauncher_NoJavaFound(line), x);
        } 
        return;
      } 
    } 
    logger.println(Messages.ComputerLauncher_UnknownJavaVersion(javaCommand));
    throw new IOException(Messages.ComputerLauncher_UnknownJavaVersion(javaCommand));
  }
}
