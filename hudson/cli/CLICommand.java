package hudson.cli;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.ExtensionPoint.LegacyInstancesAreScopedToHudson;
import hudson.Functions;
import hudson.remoting.Channel;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.apache.commons.discovery.ResourceClassIterator;
import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.classes.DiscoverClasses;
import org.apache.commons.discovery.resource.names.DiscoverServiceNames;
import org.jvnet.hudson.annotation_indexer.Index;
import org.jvnet.tiger_types.Types;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@LegacyInstancesAreScopedToHudson
public abstract class CLICommand implements ExtensionPoint, Cloneable {
  public PrintStream stdout;
  
  public PrintStream stderr;
  
  static final String CLI_LISTPARAM_SUMMARY_ERROR_TEXT = "Error occurred while performing this command, see previous stderr output.";
  
  public InputStream stdin;
  
  @Deprecated
  public Channel channel;
  
  public Locale locale;
  
  @CheckForNull
  private Charset encoding;
  
  private Authentication transportAuth;
  
  public String getName() {
    String name = getClass().getName();
    name = name.substring(name.lastIndexOf('.') + 1);
    name = name.substring(name.lastIndexOf('$') + 1);
    if (name.endsWith("Command"))
      name = name.substring(0, name.length() - 7); 
    return name.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ENGLISH);
  }
  
  public abstract String getShortDescription();
  
  public int main(List<String> args, Locale locale, InputStream stdin, PrintStream stdout, PrintStream stderr) {
    this.stdin = new BufferedInputStream(stdin);
    this.stdout = stdout;
    this.stderr = stderr;
    this.locale = locale;
    registerOptionHandlers();
    CmdLineParser p = getCmdLineParser();
    sc = null;
    old = null;
    try {
      sc = SecurityContextHolder.getContext();
      old = sc.getAuthentication();
      Authentication auth;
      sc.setAuthentication(auth = getTransportAuthentication2());
      if (!(this instanceof HelpCommand) && !(this instanceof WhoAmICommand))
        Jenkins.get().checkPermission(Jenkins.READ); 
      p.parseArgument((String[])args.toArray(new String[0]));
      LOGGER.log(Level.FINE, "Invoking CLI command {0}, with {1} arguments, as user {2}.", new Object[] { getName(), Integer.valueOf(args.size()), auth.getName() });
      int res = run();
      LOGGER.log(Level.FINE, "Executed CLI command {0}, with {1} arguments, as user {2}, return code {3}", new Object[] { getName(), Integer.valueOf(args.size()), auth.getName(), Integer.valueOf(res) });
      return res;
    } catch (CmdLineException e) {
      logFailedCommandAndPrintExceptionErrorMessage(args, e);
      printUsage(stderr, p);
      return 2;
    } catch (IllegalStateException e) {
      logFailedCommandAndPrintExceptionErrorMessage(args, e);
      return 4;
    } catch (IllegalArgumentException e) {
      logFailedCommandAndPrintExceptionErrorMessage(args, e);
      return 3;
    } catch (AbortException e) {
      logFailedCommandAndPrintExceptionErrorMessage(args, e);
      return 5;
    } catch (AccessDeniedException e) {
      logFailedCommandAndPrintExceptionErrorMessage(args, e);
      return 6;
    } catch (BadCredentialsException e) {
      String id = UUID.randomUUID().toString();
      logAndPrintError(e, "Bad Credentials. Search the server log for " + id + " for more details.", "CLI login attempt failed: " + id, Level.INFO);
      return 7;
    } catch (Throwable e) {
      String errorMsg = "Unexpected exception occurred while performing " + getName() + " command.";
      logAndPrintError(e, errorMsg, errorMsg, Level.WARNING);
      Functions.printStackTrace(e, stderr);
      return 1;
    } finally {
      if (sc != null)
        sc.setAuthentication(old); 
    } 
  }
  
  private void logFailedCommandAndPrintExceptionErrorMessage(List<String> args, Throwable e) {
    Authentication auth = getTransportAuthentication2();
    String logMessage = String.format("Failed call to CLI command %s, with %d arguments, as user %s.", new Object[] { getName(), Integer.valueOf(args.size()), (auth != null) ? auth.getName() : "<unknown>" });
    logAndPrintError(e, e.getMessage(), logMessage, Level.FINE);
  }
  
  private void logAndPrintError(Throwable e, String errorMessage, String logMessage, Level logLevel) {
    LOGGER.log(logLevel, logMessage, e);
    this.stderr.println();
    this.stderr.println("ERROR: " + errorMessage);
  }
  
  protected CmdLineParser getCmdLineParser() { return new CmdLineParser(this); }
  
  @Deprecated
  public Channel checkChannel() throws AbortException { throw new AbortException("This command is requesting the -remoting mode which is no longer supported. See https://www.jenkins.io/redirect/cli-command-requires-channel"); }
  
  public Authentication getTransportAuthentication2() {
    Authentication a = this.transportAuth;
    if (a == null)
      a = Jenkins.ANONYMOUS2; 
    return a;
  }
  
  @Deprecated
  public Authentication getTransportAuthentication() { return Authentication.fromSpring(getTransportAuthentication2()); }
  
  public void setTransportAuth2(Authentication transportAuth) { this.transportAuth = transportAuth; }
  
  @Deprecated
  public void setTransportAuth(Authentication transportAuth) { setTransportAuth2(transportAuth.toSpring()); }
  
  protected abstract int run() throws Exception;
  
  protected void printUsage(PrintStream stderr, CmdLineParser p) {
    stderr.print("java -jar jenkins-cli.jar " + getName());
    p.printSingleLineUsage(stderr);
    stderr.println();
    printUsageSummary(stderr);
    p.printUsage(stderr);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public final String getSingleLineSummary() {
    Charset charset;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    getCmdLineParser().printSingleLineUsage(out);
    try {
      charset = getClientCharset();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } 
    return out.toString(charset);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public final String getUsage() {
    Charset charset;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    getCmdLineParser().printUsage(out);
    try {
      charset = getClientCharset();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } 
    return out.toString(charset);
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public final String getLongDescription() {
    Charset charset;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      charset = getClientCharset();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } 
    PrintStream ps = new PrintStream(out, false, charset);
    printUsageSummary(ps);
    ps.close();
    return out.toString(charset);
  }
  
  protected void printUsageSummary(PrintStream stderr) { stderr.println(getShortDescription()); }
  
  @Deprecated
  protected String getClientSystemProperty(String name) throws IOException, InterruptedException {
    checkChannel();
    return null;
  }
  
  public void setClientCharset(@NonNull Charset encoding) { this.encoding = encoding; }
  
  @NonNull
  protected Charset getClientCharset() throws IOException, InterruptedException {
    if (this.encoding != null)
      return this.encoding; 
    return Charset.defaultCharset();
  }
  
  @Deprecated
  protected String getClientEnvironmentVariable(String name) throws IOException, InterruptedException {
    checkChannel();
    return null;
  }
  
  protected CLICommand createClone() {
    try {
      return (CLICommand)getClass().getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
    } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|java.lang.reflect.InvocationTargetException e) {
      throw new LinkageError(e.getMessage(), e);
    } 
  }
  
  protected void registerOptionHandlers() {
    try {
      for (Class c : Index.list(hudson.cli.declarative.OptionHandlerExtension.class, (Jenkins.get()).pluginManager.uberClassLoader, Class.class)) {
        Type t = Types.getBaseClass(c, org.kohsuke.args4j.spi.OptionHandler.class);
        CmdLineParser.registerHandler(Types.erasure(Types.getTypeArgument(t, 0)), c);
      } 
    } catch (IOException e) {
      throw new Error(e);
    } 
  }
  
  public static ExtensionList<CLICommand> all() { return ExtensionList.lookup(CLICommand.class); }
  
  public static CLICommand clone(String name) {
    for (CLICommand cmd : all()) {
      if (name.equals(cmd.getName()))
        return cmd.createClone(); 
    } 
    return null;
  }
  
  private static final Logger LOGGER = Logger.getLogger(CLICommand.class.getName());
  
  private static final ThreadLocal<CLICommand> CURRENT_COMMAND = new ThreadLocal();
  
  static CLICommand setCurrent(CLICommand cmd) {
    CLICommand old = getCurrent();
    CURRENT_COMMAND.set(cmd);
    return old;
  }
  
  public static CLICommand getCurrent() { return (CLICommand)CURRENT_COMMAND.get(); }
  
  static  {
    cls = new ClassLoaders();
    Jenkins j = Jenkins.getInstanceOrNull();
    if (j != null) {
      cls.put((j.getPluginManager()).uberClassLoader);
      ResourceNameIterator servicesIter = (new DiscoverServiceNames(cls)).findResourceNames(org.kohsuke.args4j.spi.OptionHandler.class.getName());
      ResourceClassIterator itr = (new DiscoverClasses(cls)).findResourceClasses(servicesIter);
      while (itr.hasNext()) {
        Class h = itr.nextResourceClass().loadClass();
        Class c = Types.erasure(Types.getTypeArgument(Types.getBaseClass(h, org.kohsuke.args4j.spi.OptionHandler.class), 0));
        CmdLineParser.registerHandler(c, h);
      } 
    } 
  }
}
