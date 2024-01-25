package jenkins.util;

import com.google.common.primitives.Ints;
import hudson.Functions;
import hudson.util.ProcessTree;
import java.lang.management.ManagementFactory;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class JavaVMArguments {
  public static List<String> current() {
    info = ProcessHandle.current().info();
    if (info.command().isPresent() && info.arguments().isPresent()) {
      List<String> args = new ArrayList<String>();
      args.add((String)info.command().get());
      Objects.requireNonNull(args);
      Stream.of((String[])info.arguments().get()).forEach(args::add);
      return args;
    } 
    if (Functions.isGlibcSupported()) {
      int pid = Ints.checkedCast(ProcessHandle.current().pid());
      ProcessTree.OSProcess process = ProcessTree.get().get(pid);
      if (process != null) {
        List<String> args = process.getArguments();
        if (!args.isEmpty())
          return args; 
      } 
    } 
    List<String> args = new ArrayList<String>();
    args.add(
        Paths.get(System.getProperty("java.home"), new String[0])
        .resolve("bin")
        .resolve("java")
        .toString());
    args.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
    return args;
  }
}
