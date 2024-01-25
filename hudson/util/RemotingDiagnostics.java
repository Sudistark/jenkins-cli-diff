package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.FilePath;
import hudson.remoting.AsyncFutureImpl;
import hudson.remoting.Future;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.util.Map;

public final class RemotingDiagnostics {
  public static Map<Object, Object> getSystemProperties(VirtualChannel channel) throws IOException, InterruptedException {
    if (channel == null)
      return Map.of("N/A", "N/A"); 
    return (Map)channel.call(new GetSystemProperties());
  }
  
  public static Map<String, String> getThreadDump(VirtualChannel channel) throws IOException, InterruptedException {
    if (channel == null)
      return Map.of("N/A", "N/A"); 
    return (Map)channel.call(new GetThreadDump());
  }
  
  public static Future<Map<String, String>> getThreadDumpAsync(VirtualChannel channel) throws IOException, InterruptedException {
    if (channel == null)
      return new AsyncFutureImpl(Map.of("N/A", "offline")); 
    return channel.callAsync(new GetThreadDump());
  }
  
  public static String executeGroovy(String script, @NonNull VirtualChannel channel) throws IOException, InterruptedException { return (String)channel.call(new Script(script)); }
  
  public static FilePath getHeapDump(VirtualChannel channel) throws IOException, InterruptedException { return (FilePath)channel.call(new GetHeapDump()); }
}
