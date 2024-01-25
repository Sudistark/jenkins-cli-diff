package hudson.os;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Functions;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;

public class WindowsUtil {
  private static final Pattern NEEDS_QUOTING = Pattern.compile("[\\s\"]");
  
  @NonNull
  public static String quoteArgument(@NonNull String argument) {
    if (!NEEDS_QUOTING.matcher(argument).find())
      return argument; 
    StringBuilder sb = new StringBuilder();
    sb.append('"');
    int end = argument.length();
    for (int i = 0; i < end; i++) {
      int nrBackslashes = 0;
      while (i < end && argument.charAt(i) == '\\') {
        i++;
        nrBackslashes++;
      } 
      if (i == end) {
        nrBackslashes *= 2;
      } else if (argument.charAt(i) == '"') {
        nrBackslashes = nrBackslashes * 2 + 1;
      } 
      sb.append("\\".repeat(Math.max(0, nrBackslashes)));
      if (i < end)
        sb.append(argument.charAt(i)); 
    } 
    return sb.append('"').toString();
  }
  
  private static final Pattern CMD_METACHARS = Pattern.compile("[()%!^\"<>&|]");
  
  @NonNull
  public static String quoteArgumentForCmd(@NonNull String argument) { return CMD_METACHARS.matcher(quoteArgument(argument)).replaceAll("^$0"); }
  
  @NonNull
  public static Process execCmd(String... argv) throws IOException {
    String command = (String)Arrays.stream(argv).map(WindowsUtil::quoteArgumentForCmd).collect(Collectors.joining(" "));
    return Runtime.getRuntime().exec(new String[] { "cmd.exe", "/C", command });
  }
  
  @NonNull
  public static File createJunction(@NonNull File junction, @NonNull File target) throws IOException, InterruptedException {
    if (!Functions.isWindows())
      throw new UnsupportedOperationException("Can only be called on windows platform"); 
    Process mklink = execCmd(new String[] { "mklink", "/J", junction.getAbsolutePath(), target.getAbsolutePath() });
    int result = mklink.waitFor();
    if (result != 0) {
      String stderr = IOUtils.toString(mklink.getErrorStream());
      String stdout = IOUtils.toString(mklink.getInputStream());
      throw new IOException("Process exited with " + result + "\nStandard Output:\n" + stdout + "\nError Output:\n" + stderr);
    } 
    return junction;
  }
}
