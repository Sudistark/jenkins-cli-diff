package hudson.slaves;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.Computer;
import hudson.model.Executor;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.SocketChannelStream;
import hudson.util.ClasspathBuilder;
import hudson.util.JVMBuilder;
import hudson.util.StreamCopyThread;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import jenkins.security.ChannelConfigurator;

public class Channels {
  @Deprecated
  public static Channel forProcess(String name, ExecutorService execService, InputStream in, OutputStream out, Proc proc) throws IOException { return forProcess(name, execService, in, out, null, proc); }
  
  public static Channel forProcess(String name, ExecutorService execService, InputStream in, OutputStream out, OutputStream header, Proc proc) throws IOException {
    Object object = new Object(name, execService, proc);
    object.withHeaderStream(header);
    Executor executor = Executor.currentExecutor();
    Object context = (executor != null) ? executor.getOwner() : proc;
    for (ChannelConfigurator cc : ChannelConfigurator.all())
      cc.onChannelBuilding(object, context); 
    return object.build(in, out);
  }
  
  public static Channel forProcess(String name, ExecutorService execService, Process proc, OutputStream header) throws IOException {
    StreamCopyThread streamCopyThread = new StreamCopyThread(name + " stderr", proc.getErrorStream(), header);
    streamCopyThread.start();
    Object object = new Object(name, execService, proc, streamCopyThread);
    object.withHeaderStream(header);
    Executor executor = Executor.currentExecutor();
    Object context = (executor != null) ? executor.getOwner() : proc;
    for (ChannelConfigurator cc : ChannelConfigurator.all())
      cc.onChannelBuilding(object, context); 
    return object.build(proc.getInputStream(), proc.getOutputStream());
  }
  
  @Deprecated
  public static Channel newJVM(String displayName, TaskListener listener, FilePath workDir, ClasspathBuilder classpath, Map<String, String> systemProperties) throws IOException {
    JVMBuilder vmb = new JVMBuilder();
    vmb.systemProperties(systemProperties);
    return newJVM(displayName, listener, vmb, workDir, classpath);
  }
  
  @Deprecated
  public static Channel newJVM(String displayName, TaskListener listener, JVMBuilder vmb, FilePath workDir, ClasspathBuilder classpath) throws IOException {
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.bind(new InetSocketAddress("localhost", 0));
    serverSocket.setSoTimeout((int)TimeUnit.SECONDS.toMillis(10L));
    vmb.classpath().addJarOf(Channel.class);
    vmb.mainClass(hudson.remoting.Launcher.class);
    if (classpath != null)
      Arrays.stream(classpath.toString().split(File.pathSeparator)).forEach(arg -> vmb.classpath().add(arg)); 
    vmb.args().add(new String[] { "-connectTo", "localhost:" + serverSocket.getLocalPort() });
    listener.getLogger().println("Starting " + displayName);
    Proc p = vmb.launch(new Launcher.LocalLauncher(listener)).stdout(listener).pwd(workDir).start();
    Socket s = serverSocket.accept();
    serverSocket.close();
    return forProcess("Channel to " + displayName, Computer.threadPoolForRemoting, new BufferedInputStream(
          SocketChannelStream.in(s)), new BufferedOutputStream(
          SocketChannelStream.out(s)), null, p);
  }
  
  private static final Logger LOGGER = Logger.getLogger(Channels.class.getName());
}
