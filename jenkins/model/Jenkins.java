package jenkins.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Injector;
import com.thoughtworks.xstream.XStream;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.BulkChange;
import hudson.DescriptorExtensionList;
import hudson.ExtensionComponent;
import hudson.ExtensionFinder;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.Lookup;
import hudson.Main;
import hudson.Plugin;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.ProxyConfiguration;
import hudson.RestrictedSince;
import hudson.TcpSlaveAgentListener;
import hudson.Util;
import hudson.XmlFile;
import hudson.cli.declarative.CLIMethod;
import hudson.cli.declarative.CLIResolver;
import hudson.init.InitMilestone;
import hudson.init.InitStrategy;
import hudson.init.TerminatorFinder;
import hudson.lifecycle.Lifecycle;
import hudson.lifecycle.RestartNotSupportedException;
import hudson.logging.LogRecorderManager;
import hudson.markup.EscapedMarkupFormatter;
import hudson.markup.MarkupFormatter;
import hudson.model.AbstractCIBase;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.AdministrativeMonitor;
import hudson.model.AllView;
import hudson.model.Api;
import hudson.model.Computer;
import hudson.model.ComputerSet;
import hudson.model.DependencyGraph;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.DescriptorByNameOwner;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.Failure;
import hudson.model.Fingerprint;
import hudson.model.FingerprintCleanupThread;
import hudson.model.FingerprintMap;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ItemGroupMixIn;
import hudson.model.JDK;
import hudson.model.Job;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Label;
import hudson.model.LoadBalancer;
import hudson.model.LoadStatistics;
import hudson.model.ManagementLink;
import hudson.model.Messages;
import hudson.model.ModifiableViewGroup;
import hudson.model.NoFingerprintMatch;
import hudson.model.Node;
import hudson.model.OverallLoadStatistics;
import hudson.model.PaneStatusProperties;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.RestartListener;
import hudson.model.RootAction;
import hudson.model.Slave;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.TopLevelItemDescriptor;
import hudson.model.UpdateCenter;
import hudson.model.User;
import hudson.model.View;
import hudson.model.ViewGroupMixIn;
import hudson.model.WorkspaceCleanupThread;
import hudson.model.labels.LabelAtom;
import hudson.model.listeners.ItemListener;
import hudson.model.listeners.SCMListener;
import hudson.model.listeners.SaveableListener;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import hudson.search.SearchIndexBuilder;
import hudson.security.ACL;
import hudson.security.ACLContext;
import hudson.security.AccessControlled;
import hudson.security.AuthorizationStrategy;
import hudson.security.FederatedLoginService;
import hudson.security.HudsonFilter;
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import hudson.security.PermissionScope;
import hudson.security.SecurityMode;
import hudson.security.SecurityRealm;
import hudson.security.csrf.CrumbIssuer;
import hudson.security.csrf.GlobalCrumbIssuerConfiguration;
import hudson.slaves.Cloud;
import hudson.slaves.ComputerListener;
import hudson.slaves.NodeDescriptor;
import hudson.slaves.NodeList;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.NodeProvisioner;
import hudson.slaves.RetentionStrategy;
import hudson.tasks.BuildWrapper;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.AdministrativeError;
import hudson.util.ClockDifference;
import hudson.util.CopyOnWriteList;
import hudson.util.CopyOnWriteMap;
import hudson.util.DaemonThreadFactory;
import hudson.util.DescribableList;
import hudson.util.FormApply;
import hudson.util.FormValidation;
import hudson.util.HudsonIsLoading;
import hudson.util.HudsonIsRestarting;
import hudson.util.Iterators;
import hudson.util.LogTaskListener;
import hudson.util.MultipartFormDataParser;
import hudson.util.NamingThreadFactory;
import hudson.util.PluginServletFilter;
import hudson.util.QuotedStringTokenizer;
import hudson.util.RemotingDiagnostics;
import hudson.util.TextFile;
import hudson.util.VersionNumber;
import hudson.util.XStream2;
import hudson.views.DefaultMyViewsTabBar;
import hudson.views.DefaultViewsTabBar;
import hudson.views.MyViewsTabBar;
import hudson.views.ViewsTabBar;
import hudson.widgets.Widget;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.URL;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import jenkins.AgentProtocol;
import jenkins.ExtensionComponentSet;
import jenkins.ExtensionRefreshException;
import jenkins.agents.CloudSet;
import jenkins.diagnostics.URICheckEncodingMonitor;
import jenkins.install.InstallState;
import jenkins.install.SetupWizard;
import jenkins.security.ClassFilterImpl;
import jenkins.security.RedactSecretJsonInErrorMessageSanitizer;
import jenkins.security.ResourceDomainConfiguration;
import jenkins.security.SecurityListener;
import jenkins.security.stapler.DoActionFilter;
import jenkins.security.stapler.StaplerDispatchValidator;
import jenkins.security.stapler.StaplerDispatchable;
import jenkins.security.stapler.StaplerFilteredActionListener;
import jenkins.security.stapler.TypedFilter;
import jenkins.slaves.WorkspaceLocator;
import jenkins.util.JenkinsJVM;
import jenkins.util.SystemProperties;
import jenkins.util.Timer;
import jenkins.util.io.FileBoolean;
import jenkins.util.io.OnMaster;
import jenkins.util.xml.XMLUtils;
import net.jcip.annotations.GuardedBy;
import net.sf.json.JSONObject;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationToken;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.logging.LogFactory;
import org.jvnet.hudson.reactor.Milestone;
import org.jvnet.hudson.reactor.Reactor;
import org.jvnet.hudson.reactor.ReactorException;
import org.jvnet.hudson.reactor.TaskBuilder;
import org.jvnet.hudson.reactor.TaskGraphBuilder;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.args4j.Argument;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.JellyRequestDispatcher;
import org.kohsuke.stapler.verb.POST;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.xml.sax.InputSource;

@ExportedBean
public class Jenkins extends AbstractCIBase implements DirectlyModifiableTopLevelItemGroup, StaplerProxy, StaplerFallback, ModifiableViewGroup, AccessControlled, DescriptorByNameOwner, ModelObjectWithContextMenu, ModelObjectWithChildren, OnMaster {
  private final Queue queue;
  
  public final Lookup lookup;
  
  private String version;
  
  private String installStateName;
  
  @Deprecated
  private InstallState installState;
  
  private SetupWizard setupWizard;
  
  private int numExecutors;
  
  private Node.Mode mode;
  
  private Boolean useSecurity;
  
  private ProjectNamingStrategy projectNamingStrategy;
  
  private String workspaceDir;
  
  private String buildsDir;
  
  private String systemMessage;
  
  private MarkupFormatter markupFormatter;
  
  public final File root;
  
  final Map<String, TopLevelItem> items;
  
  private static Jenkins theInstance;
  
  @GuardedBy("Jenkins.class")
  private boolean cleanUpStarted;
  
  private static FileBoolean STARTUP_MARKER_FILE;
  
  private Future<DependencyGraph> scheduledFutureDependencyGraph;
  
  private Future<DependencyGraph> calculatingFutureDependencyGraph;
  
  private Object dependencyGraphLock;
  
  private final Map<Class, ExtensionList> extensionLists;
  
  private final Map<Class, DescriptorExtensionList> descriptorLists;
  
  protected final ConcurrentMap<Node, Computer> computers;
  
  public final Hudson.CloudList clouds;
  
  private final Nodes nodes;
  
  Integer quietPeriod;
  
  int scmCheckoutRetryCount;
  
  private final CopyOnWriteArrayList<View> views;
  
  private final ViewGroupMixIn viewGroupMixIn;
  
  private final FingerprintMap fingerprintMap;
  
  public final PluginManager pluginManager;
  
  private final Object tcpSlaveAgentListenerLock;
  
  private final CopyOnWriteList<SCMListener> scmListeners;
  
  private int slaveAgentPort;
  
  private static int getSlaveAgentPortInitialValue(int def) { return SystemProperties.getInteger(Jenkins.class.getName() + ".slaveAgentPort", Integer.valueOf(def)).intValue(); }
  
  private static final boolean SLAVE_AGENT_PORT_ENFORCE = SystemProperties.getBoolean(Jenkins.class.getName() + ".slaveAgentPortEnforce", false);
  
  @GuardedBy("this")
  @CheckForNull
  private List<String> disabledAgentProtocols;
  
  @Deprecated
  private String[] _disabledAgentProtocols;
  
  @GuardedBy("this")
  @CheckForNull
  private List<String> enabledAgentProtocols;
  
  @Deprecated
  private String[] _enabledAgentProtocols;
  
  @GuardedBy("this")
  private Set<String> agentProtocols;
  
  private String label;
  
  private static String nodeNameAndSelfLabelOverride = SystemProperties.getString(Jenkins.class.getName() + ".nodeNameAndSelfLabelOverride");
  
  private final ConcurrentHashMap<String, Label> labels;
  
  @Exported
  public final OverallLoadStatistics overallLoad;
  
  @Exported
  public final LoadStatistics unlabeledLoad;
  
  public final NodeProvisioner unlabeledNodeProvisioner;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public final NodeProvisioner overallNodeProvisioner;
  
  public final ServletContext servletContext;
  
  private final List<Action> actions;
  
  private DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties;
  
  private DescribableList<NodeProperty<?>, NodePropertyDescriptor> globalNodeProperties;
  
  public final List<AdministrativeMonitor> administrativeMonitors;
  
  private final List<Widget> widgets;
  
  private final AdjunctManager adjuncts;
  
  private final ItemGroupMixIn itemGroupMixIn;
  
  static JenkinsHolder HOLDER = new Object();
  
  private final String secretKey;
  
  private final UpdateCenter updateCenter;
  
  private Boolean noUsageStatistics;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  Boolean nodeRenameMigrationNeeded;
  
  private LogRecorderManager log;
  
  private final boolean oldJenkinsJVM;
  
  @NonNull
  private Set<LabelAtom> labelAtomSet;
  
  @NonNull
  public static Jenkins get() throws IllegalStateException {
    instance = getInstanceOrNull();
    if (instance == null)
      throw new IllegalStateException("Jenkins.instance is missing. Read the documentation of Jenkins.getInstanceOrNull to see what you are doing wrong."); 
    return instance;
  }
  
  @Deprecated
  @NonNull
  public static Jenkins getActiveInstance() throws IllegalStateException { return get(); }
  
  @CLIResolver
  @CheckForNull
  public static Jenkins getInstanceOrNull() throws IllegalStateException { return HOLDER.getInstance(); }
  
  @Deprecated
  @Nullable
  public static Jenkins getInstance() throws IllegalStateException { return getInstanceOrNull(); }
  
  protected Jenkins(File root, ServletContext context) throws IOException, InterruptedException, ReactorException { this(root, context, null); }
  
  @SuppressFBWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "DM_EXIT"})
  protected Jenkins(File root, ServletContext context, PluginManager pluginManager) throws IOException, InterruptedException, ReactorException {
    this.configLoaded = false;
    this.lookup = new Lookup();
    this.version = "1.0";
    this.numExecutors = 2;
    this.mode = Node.Mode.NORMAL;
    this.authorizationStrategy = AuthorizationStrategy.UNSECURED;
    this.securityRealm = SecurityRealm.NO_AUTHENTICATION;
    this.projectNamingStrategy = ProjectNamingStrategy.DefaultProjectNamingStrategy.DEFAULT_NAMING_STRATEGY;
    this.workspaceDir = OLD_DEFAULT_WORKSPACES_DIR;
    this.buildsDir = "${ITEM_ROOTDIR}/builds";
    this.initLevel = InitMilestone.STARTED;
    this.items = new CopyOnWriteMap.Tree(String.CASE_INSENSITIVE_ORDER);
    this.jdks = new ArrayList();
    this.dependencyGraphLock = new Object();
    this.viewsTabBar = new DefaultViewsTabBar();
    this.myViewsTabBar = new DefaultMyViewsTabBar();
    this.extensionLists = new ConcurrentHashMap();
    this.descriptorLists = new ConcurrentHashMap();
    this.computers = new ConcurrentHashMap();
    this.clouds = new Hudson.CloudList(this);
    this.nodes = new Nodes(this);
    this.views = new CopyOnWriteArrayList();
    this.viewGroupMixIn = new Object(this, this);
    this.fingerprintMap = new FingerprintMap();
    this.tcpSlaveAgentListenerLock = new Object();
    this.scmListeners = new CopyOnWriteList();
    this.slaveAgentPort = getSlaveAgentPortInitialValue(0);
    this.label = "";
    this.crumbIssuer = GlobalCrumbIssuerConfiguration.createDefaultCrumbIssuer();
    this.labels = new ConcurrentHashMap();
    this.overallLoad = new OverallLoadStatistics();
    this.unlabeledLoad = new UnlabeledLoadStatistics();
    this.unlabeledNodeProvisioner = new NodeProvisioner(null, this.unlabeledLoad);
    this.overallNodeProvisioner = this.unlabeledNodeProvisioner;
    this.actions = new CopyOnWriteArrayList();
    this.nodeProperties = new DescribableList(this);
    this.globalNodeProperties = new DescribableList(this);
    this.administrativeMonitors = getExtensionList(AdministrativeMonitor.class);
    this.widgets = getExtensionList(Widget.class);
    this.itemGroupMixIn = new Object(this, this, this);
    this.updateCenter = UpdateCenter.createUpdateCenter(null);
    this.log = new LogRecorderManager();
    this.threadPoolForLoad = new ThreadPoolExecutor(TWICE_CPU_NUM, TWICE_CPU_NUM, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue(), new NamingThreadFactory(new DaemonThreadFactory(), "Jenkins load"));
    this.oldJenkinsJVM = JenkinsJVM.isJenkinsJVM();
    JenkinsJVMAccess._setJenkinsJVM(true);
    long start = System.currentTimeMillis();
    STARTUP_MARKER_FILE = new FileBoolean(new File(root, ".lastStarted"));
    ACLContext ctx = ACL.as2(ACL.SYSTEM2);
    try {
      this.root = root;
      this.servletContext = context;
      computeVersion(context);
      if (theInstance != null)
        throw new IllegalStateException("second instance"); 
      theInstance = this;
      if (!(new File(root, "jobs")).exists())
        this.workspaceDir = "${JENKINS_HOME}/workspace/${ITEM_FULL_NAME}"; 
      InitStrategy is = InitStrategy.get(Thread.currentThread().getContextClassLoader());
      Trigger.timer = new Timer("Jenkins cron thread");
      this.queue = new Queue(LoadBalancer.CONSISTENT_HASH);
      this.labelAtomSet = Collections.unmodifiableSet(Label.parse(this.label));
      try {
        this.dependencyGraph = DependencyGraph.EMPTY;
      } catch (InternalError e) {
        if (e.getMessage().contains("window server"))
          throw new Error("Looks like the server runs without X. Please specify -Djava.awt.headless=true as JVM option", e); 
        throw e;
      } 
      TextFile secretFile = new TextFile(new File(getRootDir(), "secret.key"));
      if (secretFile.exists()) {
        this.secretKey = secretFile.readTrim();
      } else {
        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        this.secretKey = Util.toHexString(random);
        secretFile.write(this.secretKey);
        (new FileBoolean(new File(root, "secret.key.not-so-secret"))).on();
      } 
      try {
        this.proxy = ProxyConfiguration.load();
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Failed to load proxy configuration", e);
      } 
      if (pluginManager == null)
        pluginManager = PluginManager.createDefault(this); 
      this.pluginManager = pluginManager;
      WebApp webApp = WebApp.get(this.servletContext);
      webApp.setClassLoader(pluginManager.uberClassLoader);
      webApp.setJsonInErrorMessageSanitizer(RedactSecretJsonInErrorMessageSanitizer.INSTANCE);
      TypedFilter typedFilter = new TypedFilter();
      webApp.setFilterForGetMethods(typedFilter);
      webApp.setFilterForFields(typedFilter);
      webApp.setFilterForDoActions(new DoActionFilter());
      StaplerFilteredActionListener actionListener = new StaplerFilteredActionListener();
      webApp.setFilteredGetterTriggerListener(actionListener);
      webApp.setFilteredDoActionTriggerListener(actionListener);
      webApp.setFilteredFieldTriggerListener(actionListener);
      webApp.setDispatchValidator(new StaplerDispatchValidator());
      webApp.setFilteredDispatchTriggerListener(actionListener);
      this.adjuncts = new AdjunctManager(this.servletContext, pluginManager.uberClassLoader, "adjuncts/" + SESSION_HASH, TimeUnit.DAYS.toMillis(365L));
      ClassFilterImpl.register();
      executeReactor(is, new TaskBuilder[] { pluginManager.initTasks(is), loadTasks(), InitMilestone.ordering() });
      if (this.initLevel != InitMilestone.COMPLETED)
        LOGGER.log(Level.SEVERE, "Jenkins initialization has not reached the COMPLETED initialization milestone after the startup. Current state: {0}. It may cause undefined incorrect behavior in Jenkins plugin relying on this state. It is likely an issue with the Initialization task graph. Example: usage of @Initializer(after = InitMilestone.COMPLETED) in a plugin (JENKINS-37759). Please create a bug in Jenkins bugtracker. ", this.initLevel); 
      if (KILL_AFTER_LOAD)
        System.exit(0); 
      save();
      launchTcpSlaveAgentListener();
      Timer.get().scheduleAtFixedRate(new Object(this), TimeUnit.MINUTES.toMillis(5L), TimeUnit.MINUTES.toMillis(5L), TimeUnit.MILLISECONDS);
      updateComputerList();
      Computer c = toComputer();
      if (c != null)
        for (ComputerListener cl : ComputerListener.all()) {
          try {
            cl.onOnline(c, new LogTaskListener(LOGGER, Level.INFO));
          } catch (Exception e) {
            LOGGER.log(Level.WARNING, String.format("Exception in onOnline() for the computer listener %s on the built-in node", new Object[] { cl.getClass() }), e);
          } 
        }  
      for (ItemListener l : ItemListener.all()) {
        long itemListenerStart = System.currentTimeMillis();
        try {
          l.onLoaded();
        } catch (RuntimeException x) {
          LOGGER.log(Level.WARNING, null, x);
        } 
        if (LOG_STARTUP_PERFORMANCE)
          LOGGER.info(String.format("Took %dms for item listener %s startup", new Object[] { Long.valueOf(System.currentTimeMillis() - itemListenerStart), l.getClass().getName() })); 
      } 
      if (LOG_STARTUP_PERFORMANCE)
        LOGGER.info(String.format("Took %dms for complete Jenkins startup", new Object[] { Long.valueOf(System.currentTimeMillis() - start) })); 
      STARTUP_MARKER_FILE.on();
      if (ctx != null)
        ctx.close(); 
    } catch (Throwable throwable) {
      if (ctx != null)
        try {
          ctx.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  protected Object readResolve() {
    if (this.jdks == null)
      this.jdks = new ArrayList(); 
    if (SLAVE_AGENT_PORT_ENFORCE)
      this.slaveAgentPort = getSlaveAgentPortInitialValue(this.slaveAgentPort); 
    synchronized (this) {
      if (this.disabledAgentProtocols == null && this._disabledAgentProtocols != null) {
        this.disabledAgentProtocols = Arrays.asList(this._disabledAgentProtocols);
        this._disabledAgentProtocols = null;
      } 
      if (this.enabledAgentProtocols == null && this._enabledAgentProtocols != null) {
        this.enabledAgentProtocols = Arrays.asList(this._enabledAgentProtocols);
        this._enabledAgentProtocols = null;
      } 
      this.agentProtocols = null;
    } 
    this.installStateName = null;
    if (this.nodeRenameMigrationNeeded == null)
      this.nodeRenameMigrationNeeded = Boolean.valueOf(true); 
    _setLabelString(this.label);
    return this;
  }
  
  @CheckForNull
  public ProxyConfiguration getProxy() { return this.proxy; }
  
  public void setProxy(@CheckForNull ProxyConfiguration proxy) { this.proxy = proxy; }
  
  @NonNull
  public InstallState getInstallState() {
    if (this.installState != null) {
      this.installStateName = this.installState.name();
      this.installState = null;
    } 
    InstallState is = (this.installStateName != null) ? InstallState.valueOf(this.installStateName) : InstallState.UNKNOWN;
    return (is != null) ? is : InstallState.UNKNOWN;
  }
  
  public void setInstallState(@NonNull InstallState newState) {
    String prior = this.installStateName;
    this.installStateName = newState.name();
    LOGGER.log(Main.isDevelopmentMode ? Level.INFO : Level.FINE, "Install state transitioning from: {0} to: {1}", new Object[] { prior, this.installStateName });
    if (!this.installStateName.equals(prior)) {
      getSetupWizard().onInstallStateUpdate(newState);
      newState.initializeState();
    } 
  }
  
  private void executeReactor(InitStrategy is, TaskBuilder... builders) throws IOException, InterruptedException, ReactorException {
    Object object = new Object(this, builders, is);
    (new Object(this)).run(object);
  }
  
  public TcpSlaveAgentListener getTcpSlaveAgentListener() { return this.tcpSlaveAgentListener; }
  
  public AdjunctManager getAdjuncts(String dummy) { return this.adjuncts; }
  
  @Exported
  public int getSlaveAgentPort() { return this.slaveAgentPort; }
  
  public boolean isSlaveAgentPortEnforced() { return SLAVE_AGENT_PORT_ENFORCE; }
  
  public void setSlaveAgentPort(int port) throws IOException {
    if (SLAVE_AGENT_PORT_ENFORCE) {
      LOGGER.log(Level.WARNING, "setSlaveAgentPort({0}) call ignored because system property {1} is true", new String[] { Integer.toString(port), Jenkins.class.getName() + ".slaveAgentPortEnforce" });
    } else {
      forceSetSlaveAgentPort(port);
    } 
  }
  
  private void forceSetSlaveAgentPort(int port) throws IOException {
    this.slaveAgentPort = port;
    launchTcpSlaveAgentListener();
  }
  
  @NonNull
  public Set<String> getAgentProtocols() {
    if (this.agentProtocols == null) {
      Set<String> result = new TreeSet<String>();
      Set<String> disabled = new TreeSet<String>();
      for (String p : Util.fixNull(this.disabledAgentProtocols))
        disabled.add(p.trim()); 
      Set<String> enabled = new TreeSet<String>();
      for (String p : Util.fixNull(this.enabledAgentProtocols))
        enabled.add(p.trim()); 
      for (AgentProtocol p : AgentProtocol.all()) {
        String name = p.getName();
        if (name != null && (p.isRequired() || (!disabled.contains(name) && (!p.isOptIn() || enabled.contains(name)))))
          result.add(name); 
      } 
      if (!result.isEmpty())
        this.agentProtocols = result; 
      return result;
    } 
    return this.agentProtocols;
  }
  
  public void setAgentProtocols(@NonNull Set<String> protocols) {
    Set<String> disabled = new TreeSet<String>();
    Set<String> enabled = new TreeSet<String>();
    for (AgentProtocol p : AgentProtocol.all()) {
      String name = p.getName();
      if (name != null && !p.isRequired()) {
        if (p.isOptIn()) {
          if (protocols.contains(name))
            enabled.add(name); 
          continue;
        } 
        if (!protocols.contains(name))
          disabled.add(name); 
      } 
    } 
    this.disabledAgentProtocols = disabled.isEmpty() ? null : new ArrayList(disabled);
    this.enabledAgentProtocols = enabled.isEmpty() ? null : new ArrayList(enabled);
    this.agentProtocols = null;
  }
  
  private void launchTcpSlaveAgentListener() throws IOException {
    synchronized (this.tcpSlaveAgentListenerLock) {
      if (this.tcpSlaveAgentListener != null && this.tcpSlaveAgentListener.configuredPort != this.slaveAgentPort) {
        this.tcpSlaveAgentListener.shutdown();
        this.tcpSlaveAgentListener = null;
      } 
      if (this.slaveAgentPort != -1 && this.tcpSlaveAgentListener == null) {
        String administrativeMonitorId = getClass().getName() + ".tcpBind";
        try {
          this.tcpSlaveAgentListener = new TcpSlaveAgentListener(this.slaveAgentPort);
          AdministrativeMonitor toBeRemoved = null;
          ExtensionList<AdministrativeMonitor> all = AdministrativeMonitor.all();
          for (AdministrativeMonitor am : all) {
            if (administrativeMonitorId.equals(am.id)) {
              toBeRemoved = am;
              break;
            } 
          } 
          all.remove(toBeRemoved);
        } catch (BindException e) {
          LOGGER.log(Level.WARNING, String.format("Failed to listen to incoming agent connections through port %s. Change the port number", new Object[] { Integer.valueOf(this.slaveAgentPort) }), e);
          new AdministrativeError(administrativeMonitorId, "Failed to listen to incoming agent connections", "Failed to listen to incoming agent connections. <a href='configureSecurity'>Change the inbound TCP port number</a> to solve the problem.", e);
        } 
      } 
    } 
  }
  
  public void setNodeName(String name) { throw new UnsupportedOperationException(); }
  
  public String getNodeDescription() { return Messages.Hudson_NodeDescription(); }
  
  @Exported
  public String getDescription() { return this.systemMessage; }
  
  @NonNull
  public PluginManager getPluginManager() { return this.pluginManager; }
  
  public UpdateCenter getUpdateCenter() { return this.updateCenter; }
  
  @CheckForNull
  public Boolean isNoUsageStatistics() { return this.noUsageStatistics; }
  
  public boolean isUsageStatisticsCollected() { return (this.noUsageStatistics == null || !this.noUsageStatistics.booleanValue()); }
  
  public void setNoUsageStatistics(Boolean noUsageStatistics) throws IOException {
    this.noUsageStatistics = noUsageStatistics;
    save();
  }
  
  public View.People getPeople() { return new View.People(this); }
  
  public View.AsynchPeople getAsynchPeople() { return new View.AsynchPeople(this); }
  
  @Deprecated
  public boolean hasPeople() { return View.People.isApplicable(this.items.values()); }
  
  public Api getApi() {
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req != null) {
      Object attribute = req.getAttribute("javax.servlet.error.message");
      if (attribute != null)
        return null; 
    } 
    return new Api(this);
  }
  
  @Deprecated
  public String getSecretKey() { return this.secretKey; }
  
  @Deprecated
  public SecretKey getSecretKeyAsAES128() { return Util.toAes128Key(this.secretKey); }
  
  public String getLegacyInstanceId() { return Util.getDigestOf(getSecretKey()); }
  
  public Descriptor<SCM> getScm(String shortClassName) { return findDescriptor(shortClassName, SCM.all()); }
  
  public Descriptor<RepositoryBrowser<?>> getRepositoryBrowser(String shortClassName) { return findDescriptor(shortClassName, RepositoryBrowser.all()); }
  
  public Descriptor<Builder> getBuilder(String shortClassName) { return findDescriptor(shortClassName, Builder.all()); }
  
  public Descriptor<BuildWrapper> getBuildWrapper(String shortClassName) { return findDescriptor(shortClassName, BuildWrapper.all()); }
  
  public Descriptor<Publisher> getPublisher(String shortClassName) { return findDescriptor(shortClassName, Publisher.all()); }
  
  public TriggerDescriptor getTrigger(String shortClassName) { return (TriggerDescriptor)findDescriptor(shortClassName, Trigger.all()); }
  
  public Descriptor<RetentionStrategy<?>> getRetentionStrategy(String shortClassName) { return findDescriptor(shortClassName, RetentionStrategy.all()); }
  
  public JobPropertyDescriptor getJobProperty(String shortClassName) {
    Descriptor d = findDescriptor(shortClassName, JobPropertyDescriptor.all());
    return (JobPropertyDescriptor)d;
  }
  
  @Deprecated
  public ComputerSet getComputer() { return new ComputerSet(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  public CloudSet getCloud() { return new CloudSet(); }
  
  public Descriptor getDescriptor(String id) {
    Iterable<Descriptor> descriptors = Iterators.sequence(new Iterable[] { getExtensionList(Descriptor.class), DescriptorExtensionList.listLegacyInstances() });
    for (Descriptor d : descriptors) {
      if (d.getId().equals(id))
        return d; 
    } 
    Descriptor candidate = null;
    for (Descriptor d : descriptors) {
      String name = d.getId();
      if (name.substring(name.lastIndexOf('.') + 1).equals(id)) {
        if (candidate == null) {
          candidate = d;
          continue;
        } 
        throw new IllegalArgumentException(id + " is ambiguous; matches both " + id + " and " + name);
      } 
    } 
    return candidate;
  }
  
  public Descriptor getDescriptorByName(String id) { return getDescriptor(id); }
  
  @CheckForNull
  public Descriptor getDescriptor(Class<? extends Describable> type) {
    for (Descriptor d : getExtensionList(Descriptor.class)) {
      if (d.clazz == type)
        return d; 
    } 
    return null;
  }
  
  @NonNull
  public Descriptor getDescriptorOrDie(Class<? extends Describable> type) {
    Descriptor d = getDescriptor(type);
    if (d == null)
      throw new AssertionError("" + type + " is missing its descriptor"); 
    return d;
  }
  
  public <T extends Descriptor> T getDescriptorByType(Class<T> type) {
    for (Descriptor d : getExtensionList(Descriptor.class)) {
      if (d.getClass() == type)
        return (T)(Descriptor)type.cast(d); 
    } 
    return null;
  }
  
  public Descriptor<SecurityRealm> getSecurityRealms(String shortClassName) { return findDescriptor(shortClassName, SecurityRealm.all()); }
  
  private <T extends Describable<T>> Descriptor<T> findDescriptor(String shortClassName, Collection<? extends Descriptor<T>> descriptors) {
    String name = "." + shortClassName;
    for (Descriptor<T> d : descriptors) {
      if (d.clazz.getName().endsWith(name))
        return d; 
    } 
    return null;
  }
  
  protected void updateNewComputer(Node n) { updateNewComputer(n, AUTOMATIC_AGENT_LAUNCH); }
  
  protected void updateComputerList() throws IOException { updateComputerList(AUTOMATIC_AGENT_LAUNCH); }
  
  @Deprecated
  public CopyOnWriteList<SCMListener> getSCMListeners() { return this.scmListeners; }
  
  @CheckForNull
  public Plugin getPlugin(String shortName) {
    PluginWrapper p = this.pluginManager.getPlugin(shortName);
    if (p == null)
      return null; 
    return p.getPlugin();
  }
  
  @CheckForNull
  public <P extends Plugin> P getPlugin(Class<P> clazz) {
    PluginWrapper p = this.pluginManager.getPlugin(clazz);
    if (p == null)
      return null; 
    return (P)p.getPlugin();
  }
  
  public <P extends Plugin> List<P> getPlugins(Class<P> clazz) {
    List<P> result = new ArrayList<P>();
    for (PluginWrapper w : this.pluginManager.getPlugins(clazz))
      result.add(w.getPlugin()); 
    return Collections.unmodifiableList(result);
  }
  
  public String getSystemMessage() { return this.systemMessage; }
  
  @NonNull
  public MarkupFormatter getMarkupFormatter() {
    MarkupFormatter f = this.markupFormatter;
    return (f != null) ? f : new EscapedMarkupFormatter();
  }
  
  public void setMarkupFormatter(MarkupFormatter f) { this.markupFormatter = f; }
  
  public void setSystemMessage(String message) {
    this.systemMessage = message;
    save();
  }
  
  @StaplerDispatchable
  public FederatedLoginService getFederatedLoginService(String name) {
    for (FederatedLoginService fls : FederatedLoginService.all()) {
      if (fls.getUrlName().equals(name))
        return fls; 
    } 
    return null;
  }
  
  public List<FederatedLoginService> getFederatedLoginServices() { return FederatedLoginService.all(); }
  
  public Launcher createLauncher(TaskListener listener) { return (new Launcher.LocalLauncher(listener)).decorateFor(this); }
  
  public String getFullName() { return ""; }
  
  public String getFullDisplayName() { return ""; }
  
  public List<Action> getActions() { return this.actions; }
  
  @Exported(name = "jobs")
  public List<TopLevelItem> getItems() { return getItems(t -> true); }
  
  public List<TopLevelItem> getItems(Predicate<TopLevelItem> pred) {
    List<TopLevelItem> viewableItems = new ArrayList<TopLevelItem>();
    for (TopLevelItem item : this.items.values()) {
      if (pred.test(item) && item.hasPermission(Item.READ))
        viewableItems.add(item); 
    } 
    return viewableItems;
  }
  
  public Map<String, TopLevelItem> getItemMap() { return Collections.unmodifiableMap(this.items); }
  
  public <T> List<T> getItems(Class<T> type) {
    List<T> r = new ArrayList<T>();
    Objects.requireNonNull(type);
    for (TopLevelItem i : getItems(type::isInstance))
      r.add(type.cast(i)); 
    return r;
  }
  
  @Deprecated
  public List<Project> getProjects() { return Util.createSubList(this.items.values(), Project.class); }
  
  public Collection<String> getJobNames() {
    List<String> names = new ArrayList<String>();
    for (Job j : allItems(Job.class))
      names.add(j.getFullName()); 
    names.sort(String.CASE_INSENSITIVE_ORDER);
    return names;
  }
  
  public List<Action> getViewActions() { return getActions(); }
  
  public Collection<String> getTopLevelItemNames() {
    List<String> names = new ArrayList<String>();
    for (TopLevelItem j : this.items.values())
      names.add(j.getName()); 
    return names;
  }
  
  @CheckForNull
  public View getView(@CheckForNull String name) { return this.viewGroupMixIn.getView(name); }
  
  @Exported
  public Collection<View> getViews() { return this.viewGroupMixIn.getViews(); }
  
  public void addView(@NonNull View v) throws IOException { this.viewGroupMixIn.addView(v); }
  
  public void setViews(Collection<View> views) throws IOException {
    BulkChange bc = new BulkChange(this);
    try {
      this.views.clear();
      for (View v : views)
        addView(v); 
      bc.commit();
      bc.close();
    } catch (Throwable throwable) {
      try {
        bc.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  public boolean canDelete(View view) { return this.viewGroupMixIn.canDelete(view); }
  
  public void deleteView(View view) throws IOException { this.viewGroupMixIn.deleteView(view); }
  
  public void onViewRenamed(View view, String oldName, String newName) { this.viewGroupMixIn.onViewRenamed(view, oldName, newName); }
  
  @Exported
  public View getPrimaryView() { return this.viewGroupMixIn.getPrimaryView(); }
  
  public void setPrimaryView(@NonNull View v) throws IOException { this.primaryView = v.getViewName(); }
  
  public ViewsTabBar getViewsTabBar() { return this.viewsTabBar; }
  
  public void setViewsTabBar(ViewsTabBar viewsTabBar) { this.viewsTabBar = viewsTabBar; }
  
  public Jenkins getItemGroup() throws IllegalStateException { return this; }
  
  public MyViewsTabBar getMyViewsTabBar() { return this.myViewsTabBar; }
  
  public void setMyViewsTabBar(MyViewsTabBar myViewsTabBar) { this.myViewsTabBar = myViewsTabBar; }
  
  public boolean isUpgradedFromBefore(VersionNumber v) {
    try {
      return (new VersionNumber(this.version)).isOlderThan(v);
    } catch (IllegalArgumentException e) {
      return false;
    } 
  }
  
  public Computer[] getComputers() { return (Computer[])this.computers.values().stream().sorted(Comparator.comparing(Computer::getName)).toArray(x$0 -> new Computer[x$0]); }
  
  @CLIResolver
  @CheckForNull
  public Computer getComputer(@Argument(required = true, metaVar = "NAME", usage = "Node name") @NonNull String name) {
    if (name.equals("(built-in)") || name.equals("(master)"))
      name = ""; 
    for (Computer c : this.computers.values()) {
      if (c.getName().equals(name))
        return c; 
    } 
    return null;
  }
  
  @CheckForNull
  public Label getLabel(String expr) {
    if (expr == null)
      return null; 
    expr = QuotedStringTokenizer.unquote(expr);
    while (true) {
      Label l = (Label)this.labels.get(expr);
      if (l != null)
        return l; 
      try {
        this.labels.putIfAbsent(expr, Label.parseExpression(expr));
      } catch (IllegalArgumentException e) {
        return getLabelAtom(expr);
      } 
    } 
  }
  
  @Nullable
  public LabelAtom getLabelAtom(@CheckForNull String name) {
    if (name == null)
      return null; 
    while (true) {
      Label l = (Label)this.labels.get(name);
      if (l != null)
        return (LabelAtom)l; 
      LabelAtom la = new LabelAtom(name);
      if (this.labels.putIfAbsent(name, la) == null)
        la.load(); 
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Nullable
  public LabelAtom tryGetLabelAtom(@NonNull String name) {
    Label label = (Label)this.labels.get(name);
    if (label instanceof LabelAtom)
      return (LabelAtom)label; 
    return null;
  }
  
  public Set<Label> getLabels() {
    Set<Label> r = new TreeSet<Label>();
    for (Label l : this.labels.values()) {
      if (!l.isEmpty())
        r.add(l); 
    } 
    return r;
  }
  
  protected Set<LabelAtom> getLabelAtomSet() { return this.labelAtomSet; }
  
  public Set<LabelAtom> getLabelAtoms() {
    Set<LabelAtom> r = new TreeSet<LabelAtom>();
    for (Label l : this.labels.values()) {
      if (!l.isEmpty() && l instanceof LabelAtom)
        r.add((LabelAtom)l); 
    } 
    return r;
  }
  
  public Queue getQueue() { return this.queue; }
  
  public String getDisplayName() { return Messages.Hudson_DisplayName(); }
  
  public List<JDK> getJDKs() { return this.jdks; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void setJDKs(Collection<? extends JDK> jdks) { this.jdks = new ArrayList(jdks); }
  
  public JDK getJDK(String name) {
    if (name == null) {
      List<JDK> jdks = getJDKs();
      if (jdks.size() == 1)
        return (JDK)jdks.get(0); 
      return null;
    } 
    for (JDK j : getJDKs()) {
      if (j.getName().equals(name))
        return j; 
    } 
    return null;
  }
  
  @CheckForNull
  public Node getNode(String name) { return this.nodes.getNode(name); }
  
  public Cloud getCloud(String name) { return this.clouds.getByName(name); }
  
  protected ConcurrentMap<Node, Computer> getComputerMap() { return this.computers; }
  
  @NonNull
  public List<Node> getNodes() { return this.nodes.getNodes(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Nodes getNodesObject() { return this.nodes; }
  
  public void addNode(Node n) { this.nodes.addNode(n); }
  
  public void removeNode(@NonNull Node n) { this.nodes.removeNode(n); }
  
  public boolean updateNode(Node n) throws IOException { return this.nodes.updateNode(n); }
  
  public void setNodes(List<? extends Node> n) throws IOException { this.nodes.setNodes(n); }
  
  public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() { return this.nodeProperties; }
  
  public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getGlobalNodeProperties() { return this.globalNodeProperties; }
  
  void trimLabels() throws IOException { trimLabels((Set)null); }
  
  void trimLabels(Node... nodes) {
    Set<LabelAtom> includedLabels = new HashSet<LabelAtom>();
    Arrays.stream(nodes).filter(Objects::nonNull).forEach(n -> includedLabels.addAll(n.getAssignedLabels()));
    trimLabels(includedLabels);
  }
  
  private void trimLabels(@CheckForNull Set<LabelAtom> includedLabels) {
    Set<Set<LabelAtom>> nodeLabels = new HashSet<Set<LabelAtom>>();
    nodeLabels.add(getAssignedLabels());
    getNodes().forEach(n -> nodeLabels.add(n.getAssignedLabels()));
    for (Iterator<Label> itr = this.labels.values().iterator(); itr.hasNext(); ) {
      Label l = (Label)itr.next();
      if (includedLabels == null || includedLabels.contains(l) || l.matches(includedLabels)) {
        Objects.requireNonNull(l);
        if (nodeLabels.stream().anyMatch(l::matches) || !l.getClouds().isEmpty()) {
          resetLabel(l);
          continue;
        } 
        itr.remove();
      } 
    } 
  }
  
  @CheckForNull
  public AdministrativeMonitor getAdministrativeMonitor(String id) {
    for (AdministrativeMonitor m : this.administrativeMonitors) {
      if (m.id.equals(id))
        return m; 
    } 
    return null;
  }
  
  public List<AdministrativeMonitor> getActiveAdministrativeMonitors() {
    if (!get().hasPermission(SYSTEM_READ))
      return Collections.emptyList(); 
    return (List)this.administrativeMonitors.stream().filter(m -> {
          try {
            return (get().hasPermission(m.getRequiredPermission()) && m.isEnabled() && m.isActivated());
          } catch (Throwable x) {
            LOGGER.log(Level.WARNING, null, x);
            return false;
          } 
        }).collect(Collectors.toList());
  }
  
  public NodeDescriptor getDescriptor() { return DescriptorImpl.INSTANCE; }
  
  public int getQuietPeriod() { return (this.quietPeriod != null) ? this.quietPeriod.intValue() : 5; }
  
  public void setQuietPeriod(Integer quietPeriod) throws IOException {
    this.quietPeriod = quietPeriod;
    save();
  }
  
  public int getScmCheckoutRetryCount() { return this.scmCheckoutRetryCount; }
  
  public void setScmCheckoutRetryCount(int scmCheckoutRetryCount) throws IOException {
    this.scmCheckoutRetryCount = scmCheckoutRetryCount;
    save();
  }
  
  public String getSearchUrl() { return ""; }
  
  public SearchIndexBuilder makeSearchIndex() {
    SearchIndexBuilder builder = super.makeSearchIndex();
    if (hasPermission(ADMINISTER))
      builder.add("configure", new String[] { "config", "configure" }).add("manage").add("log"); 
    builder.add(new Object(this)).add(getPrimaryView().makeSearchIndex()).add(new Object(this)).add(new Object(this)).add(new Object(this));
    return builder;
  }
  
  public String getUrlChildPrefix() { return "job"; }
  
  @Nullable
  public String getRootUrl() {
    JenkinsLocationConfiguration config = JenkinsLocationConfiguration.get();
    String url = config.getUrl();
    if (url != null)
      return Util.ensureEndsWith(url, "/"); 
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req != null)
      return getRootUrlFromRequest(); 
    return null;
  }
  
  @Exported(name = "url")
  @Restricted({org.kohsuke.accmod.restrictions.DoNotUse.class})
  @CheckForNull
  public String getConfiguredRootUrl() {
    JenkinsLocationConfiguration config = JenkinsLocationConfiguration.get();
    return config.getUrl();
  }
  
  public boolean isRootUrlSecure() {
    String url = getRootUrl();
    return (url != null && url.startsWith("https"));
  }
  
  @NonNull
  public String getRootUrlFromRequest() {
    StaplerRequest req = Stapler.getCurrentRequest();
    if (req == null)
      throw new IllegalStateException("cannot call getRootUrlFromRequest from outside a request handling thread"); 
    StringBuilder buf = new StringBuilder();
    String scheme = getXForwardedHeader(req, "X-Forwarded-Proto", req.getScheme());
    buf.append(scheme).append("://");
    String host = getXForwardedHeader(req, "X-Forwarded-Host", req.getServerName());
    int index = host.lastIndexOf(':');
    int port = req.getServerPort();
    if (index == -1) {
      buf.append(host);
    } else if (host.startsWith("[") && host.endsWith("]")) {
      buf.append(host);
    } else {
      buf.append(host, 0, index);
      if (index + 1 < host.length())
        try {
          port = Integer.parseInt(host.substring(index + 1));
        } catch (NumberFormatException numberFormatException) {} 
    } 
    String forwardedPort = getXForwardedHeader(req, "X-Forwarded-Port", null);
    if (forwardedPort != null)
      try {
        port = Integer.parseInt(forwardedPort);
      } catch (NumberFormatException numberFormatException) {} 
    if (port != ("https".equals(scheme) ? 443 : 80))
      buf.append(':').append(port); 
    buf.append(req.getContextPath()).append('/');
    return buf.toString();
  }
  
  private static String getXForwardedHeader(StaplerRequest req, String header, String defaultValue) {
    String value = req.getHeader(header);
    if (value != null) {
      int index = value.indexOf(',');
      return (index == -1) ? value.trim() : value.substring(0, index).trim();
    } 
    return defaultValue;
  }
  
  public File getRootDir() { return this.root; }
  
  public FilePath getWorkspaceFor(TopLevelItem item) {
    for (WorkspaceLocator l : WorkspaceLocator.all()) {
      FilePath workspace = l.locate(item, this);
      if (workspace != null)
        return workspace; 
    } 
    return new FilePath(expandVariablesForDirectory(this.workspaceDir, item));
  }
  
  public File getBuildDirFor(Job job) { return expandVariablesForDirectory(this.buildsDir, job); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean isDefaultBuildDir() { return "${ITEM_ROOTDIR}/builds".equals(this.buildsDir); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  boolean isDefaultWorkspaceDir() { return (OLD_DEFAULT_WORKSPACES_DIR.equals(this.workspaceDir) || "${JENKINS_HOME}/workspace/${ITEM_FULL_NAME}".equals(this.workspaceDir)); }
  
  private File expandVariablesForDirectory(String base, Item item) { return new File(expandVariablesForDirectory(base, item.getFullName(), item.getRootDir().getPath())); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String expandVariablesForDirectory(String base, String itemFullName, String itemRootDir) {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("JENKINS_HOME", get().getRootDir().getPath());
    properties.put("ITEM_ROOTDIR", itemRootDir);
    properties.put("ITEM_FULLNAME", itemFullName);
    properties.put("ITEM_FULL_NAME", itemFullName.replace(':', '$'));
    return Util.replaceMacro(base, Collections.unmodifiableMap(properties));
  }
  
  public String getRawWorkspaceDir() { return this.workspaceDir; }
  
  public String getRawBuildsDir() { return this.buildsDir; }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void setRawBuildsDir(String buildsDir) { this.buildsDir = buildsDir; }
  
  @NonNull
  public FilePath getRootPath() { return new FilePath(getRootDir()); }
  
  public FilePath createPath(String absolutePath) { return new FilePath((VirtualChannel)null, absolutePath); }
  
  public ClockDifference getClockDifference() { return ClockDifference.ZERO; }
  
  public Callable<ClockDifference, IOException> getClockDifferenceCallable() { return new ClockDifferenceCallable(); }
  
  public LogRecorderManager getLog() {
    checkPermission(SYSTEM_READ);
    return this.log;
  }
  
  public void setLog(LogRecorderManager log) {
    checkPermission(ADMINISTER);
    this.log = log;
  }
  
  @Exported
  public boolean isUseSecurity() { return (this.securityRealm != SecurityRealm.NO_AUTHENTICATION || this.authorizationStrategy != AuthorizationStrategy.UNSECURED); }
  
  public boolean isUseProjectNamingStrategy() { return (this.projectNamingStrategy != ProjectNamingStrategy.DEFAULT_NAMING_STRATEGY); }
  
  @Exported
  public boolean isUseCrumbs() { return (this.crumbIssuer != null); }
  
  public SecurityMode getSecurity() {
    SecurityRealm realm = this.securityRealm;
    if (realm == SecurityRealm.NO_AUTHENTICATION)
      return SecurityMode.UNSECURED; 
    if (realm instanceof hudson.security.LegacySecurityRealm)
      return SecurityMode.LEGACY; 
    return SecurityMode.SECURED;
  }
  
  public SecurityRealm getSecurityRealm() { return this.securityRealm; }
  
  public void setSecurityRealm(@CheckForNull SecurityRealm securityRealm) {
    if (securityRealm == null)
      securityRealm = SecurityRealm.NO_AUTHENTICATION; 
    this.useSecurity = Boolean.valueOf(true);
    IdStrategy oldUserIdStrategy = (this.securityRealm == null) ? securityRealm.getUserIdStrategy() : this.securityRealm.getUserIdStrategy();
    this.securityRealm = securityRealm;
    resetFilter(securityRealm, oldUserIdStrategy);
    saveQuietly();
  }
  
  private void resetFilter(@CheckForNull SecurityRealm securityRealm, @CheckForNull IdStrategy oldUserIdStrategy) {
    try {
      HudsonFilter filter = HudsonFilter.get(this.servletContext);
      if (filter == null) {
        LOGGER.fine("HudsonFilter has not yet been initialized: Can't perform security setup for now");
      } else {
        LOGGER.fine("HudsonFilter has been previously initialized: Setting security up");
        filter.reset(securityRealm);
        LOGGER.fine("Security is now fully set up");
      } 
      if (oldUserIdStrategy != null && this.securityRealm != null && !oldUserIdStrategy.equals(this.securityRealm.getUserIdStrategy()))
        User.rekey(); 
    } catch (ServletException e) {
      throw new Object(this, "Failed to configure filter", e);
    } 
  }
  
  public void setAuthorizationStrategy(@CheckForNull AuthorizationStrategy a) {
    if (a == null)
      a = AuthorizationStrategy.UNSECURED; 
    this.useSecurity = Boolean.valueOf(true);
    this.authorizationStrategy = a;
    saveQuietly();
  }
  
  public boolean isDisableRememberMe() { return this.disableRememberMe; }
  
  public void setDisableRememberMe(boolean disableRememberMe) { this.disableRememberMe = disableRememberMe; }
  
  public void disableSecurity() throws IOException {
    this.useSecurity = null;
    setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
    this.authorizationStrategy = AuthorizationStrategy.UNSECURED;
  }
  
  public void setProjectNamingStrategy(ProjectNamingStrategy ns) {
    if (ns == null)
      ns = ProjectNamingStrategy.DEFAULT_NAMING_STRATEGY; 
    this.projectNamingStrategy = ns;
  }
  
  public Lifecycle getLifecycle() { return Lifecycle.get(); }
  
  @CheckForNull
  public Injector getInjector() { return (Injector)lookup(Injector.class); }
  
  public <T> ExtensionList<T> getExtensionList(Class<T> extensionType) {
    ExtensionList<T> extensionList = (ExtensionList)this.extensionLists.get(extensionType);
    return (extensionList != null) ? extensionList : (ExtensionList)this.extensionLists.computeIfAbsent(extensionType, key -> ExtensionList.create(this, key));
  }
  
  @StaplerDispatchable
  public ExtensionList getExtensionList(String extensionType) throws ClassNotFoundException { return getExtensionList(this.pluginManager.uberClassLoader.loadClass(extensionType)); }
  
  @NonNull
  public <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> getDescriptorList(Class<T> type) { return (DescriptorExtensionList)this.descriptorLists.computeIfAbsent(type, key -> DescriptorExtensionList.createDescriptorList(this, key)); }
  
  public void refreshExtensions() throws IOException {
    ExtensionList<ExtensionFinder> finders = getExtensionList(ExtensionFinder.class);
    for (ExtensionFinder ef : finders) {
      if (!ef.isRefreshable())
        throw new ExtensionRefreshException("" + ef + " doesn't support refresh"); 
    } 
    List<ExtensionComponentSet> fragments = new ArrayList<ExtensionComponentSet>();
    for (ExtensionFinder ef : finders)
      fragments.add(ef.refresh()); 
    ExtensionComponentSet delta = ExtensionComponentSet.union(fragments).filtered();
    List<ExtensionComponent<ExtensionFinder>> newFinders = new ArrayList<ExtensionComponent<ExtensionFinder>>(delta.find(ExtensionFinder.class));
    while (!newFinders.isEmpty()) {
      ExtensionFinder f = (ExtensionFinder)((ExtensionComponent)newFinders.remove(newFinders.size() - 1)).getInstance();
      ExtensionComponentSet ecs = ExtensionComponentSet.allOf(f).filtered();
      newFinders.addAll(ecs.find(ExtensionFinder.class));
      delta = ExtensionComponentSet.union(new ExtensionComponentSet[] { delta, ecs });
    } 
    for (ExtensionList el : this.extensionLists.values())
      el.refresh(delta); 
    for (ExtensionList el : this.descriptorLists.values())
      el.refresh(delta); 
    for (ExtensionComponent<RootAction> ea : delta.find(RootAction.class)) {
      Action a = (Action)ea.getInstance();
      if (!this.actions.contains(a))
        this.actions.add(a); 
    } 
  }
  
  @NonNull
  public ACL getACL() { return this.authorizationStrategy.getRootACL(); }
  
  public AuthorizationStrategy getAuthorizationStrategy() { return this.authorizationStrategy; }
  
  public ProjectNamingStrategy getProjectNamingStrategy() { return (this.projectNamingStrategy == null) ? ProjectNamingStrategy.DEFAULT_NAMING_STRATEGY : this.projectNamingStrategy; }
  
  @Exported
  public boolean isQuietingDown() { return (this.quietDownInfo != null); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @NonNull
  public boolean isPreparingSafeRestart() {
    QuietDownInfo quietDownInfo = this.quietDownInfo;
    if (quietDownInfo != null)
      return quietDownInfo.isSafeRestart(); 
    return false;
  }
  
  @Exported
  @CheckForNull
  public String getQuietDownReason() {
    QuietDownInfo info = this.quietDownInfo;
    return (info != null) ? info.message : null;
  }
  
  public boolean isTerminating() { return this.terminating; }
  
  public InitMilestone getInitLevel() { return this.initLevel; }
  
  public void setNumExecutors(int n) throws IOException {
    if (n < 0)
      throw new IllegalArgumentException("Incorrect field \"# of executors\": " + n + ". It should be a non-negative number."); 
    if (this.numExecutors != n) {
      this.numExecutors = n;
      updateComputerList();
      save();
    } 
  }
  
  public TopLevelItem getItem(String name) throws AccessDeniedException {
    if (name == null)
      return null; 
    TopLevelItem item = (TopLevelItem)this.items.get(name);
    if (item == null)
      return null; 
    if (!item.hasPermission(Item.READ)) {
      if (item.hasPermission(Item.DISCOVER))
        throw new AccessDeniedException("Please login to access job " + name); 
      return null;
    } 
    return item;
  }
  
  public Item getItem(String pathName, ItemGroup context) {
    Jenkins jenkins;
    if (context == null)
      jenkins = this; 
    if (pathName == null)
      return null; 
    if (pathName.startsWith("/"))
      return getItemByFullName(pathName); 
    Object ctx = jenkins;
    StringTokenizer tokens = new StringTokenizer(pathName, "/");
    while (tokens.hasMoreTokens()) {
      String s = tokens.nextToken();
      if (s.equals("..")) {
        if (ctx instanceof Item) {
          ctx = ((Item)ctx).getParent();
          continue;
        } 
        ctx = null;
        break;
      } 
      if (s.equals("."))
        continue; 
      if (ctx instanceof ItemGroup) {
        ItemGroup g = (ItemGroup)ctx;
        Item i = g.getItem(s);
        if (i == null || !i.hasPermission(Item.READ)) {
          ctx = null;
          break;
        } 
        ctx = i;
        continue;
      } 
      return null;
    } 
    if (ctx instanceof Item)
      return (Item)ctx; 
    return getItemByFullName(pathName);
  }
  
  public final Item getItem(String pathName, Item context) { return getItem(pathName, (context != null) ? context.getParent() : null); }
  
  public final <T extends Item> T getItem(String pathName, ItemGroup context, @NonNull Class<T> type) {
    Item r = getItem(pathName, context);
    if (type.isInstance(r))
      return (T)(Item)type.cast(r); 
    return null;
  }
  
  public final <T extends Item> T getItem(String pathName, Item context, Class<T> type) { return (T)getItem(pathName, (context != null) ? context.getParent() : null, type); }
  
  public File getRootDirFor(TopLevelItem child) { return getRootDirFor(child.getName()); }
  
  private File getRootDirFor(String name) { return new File(new File(getRootDir(), "jobs"), name); }
  
  @CheckForNull
  public <T extends Item> T getItemByFullName(@NonNull String fullName, Class<T> type) throws AccessDeniedException {
    StringTokenizer tokens = new StringTokenizer(fullName, "/");
    Jenkins jenkins = this;
    if (!tokens.hasMoreTokens())
      return null; 
    while (true) {
      Item item = jenkins.getItem(tokens.nextToken());
      if (!tokens.hasMoreTokens()) {
        if (type.isInstance(item))
          return (T)(Item)type.cast(item); 
        return null;
      } 
      if (!(item instanceof ItemGroup))
        return null; 
      if (!item.hasPermission(Item.READ))
        return null; 
      ItemGroup itemGroup = (ItemGroup)item;
    } 
  }
  
  @CheckForNull
  public Item getItemByFullName(String fullName) { return getItemByFullName(fullName, Item.class); }
  
  @CheckForNull
  public User getUser(String name) { return User.get(name, (User.ALLOW_USER_CREATION_VIA_URL && hasPermission(ADMINISTER))); }
  
  @NonNull
  public TopLevelItem createProject(@NonNull TopLevelItemDescriptor type, @NonNull String name) throws IOException { return createProject(type, name, true); }
  
  @NonNull
  public TopLevelItem createProject(@NonNull TopLevelItemDescriptor type, @NonNull String name, boolean notify) throws IOException { return this.itemGroupMixIn.createProject(type, name, notify); }
  
  public void putItem(TopLevelItem item) throws IOException, InterruptedException {
    String name = item.getName();
    TopLevelItem old = (TopLevelItem)this.items.get(name);
    if (old == item)
      return; 
    checkPermission(Item.CREATE);
    if (old != null)
      old.delete(); 
    this.items.put(name, item);
    ItemListener.fireOnCreated(item);
  }
  
  @NonNull
  public <T extends TopLevelItem> T createProject(@NonNull Class<T> type, @NonNull String name) throws IOException { return (T)(TopLevelItem)type.cast(createProject((TopLevelItemDescriptor)getDescriptorOrDie(type), name)); }
  
  public void onRenamed(TopLevelItem job, String oldName, String newName) throws IOException {
    this.items.remove(oldName);
    this.items.put(newName, job);
    for (View v : this.views)
      v.onJobRenamed(job, oldName, newName); 
  }
  
  public void onDeleted(TopLevelItem item) throws IOException, InterruptedException {
    ItemListener.fireOnDeleted(item);
    this.items.remove(item.getName());
    for (View v : this.views)
      v.onJobRenamed(item, item.getName(), null); 
  }
  
  public boolean canAdd(TopLevelItem item) { return true; }
  
  public <I extends TopLevelItem> I add(I item, String name) throws IOException, IllegalArgumentException {
    if (this.items.containsKey(name))
      throw new IllegalArgumentException("already an item '" + name + "'"); 
    this.items.put(name, item);
    return item;
  }
  
  public void remove(TopLevelItem item) throws IOException, InterruptedException { this.items.remove(item.getName()); }
  
  public FingerprintMap getFingerprintMap() { return this.fingerprintMap; }
  
  @StaplerDispatchable
  public Object getFingerprint(String md5sum) throws IOException {
    Fingerprint r = (Fingerprint)this.fingerprintMap.get(md5sum);
    if (r == null)
      return new NoFingerprintMatch(md5sum); 
    return r;
  }
  
  public Fingerprint _getFingerprint(String md5sum) throws IOException { return (Fingerprint)this.fingerprintMap.get(md5sum); }
  
  private XmlFile getConfigFile() { return new XmlFile(XSTREAM, new File(this.root, "config.xml")); }
  
  public int getNumExecutors() { return this.numExecutors; }
  
  public Node.Mode getMode() { return this.mode; }
  
  public void setMode(Node.Mode m) throws IOException {
    this.mode = m;
    save();
  }
  
  public String getLabelString() { return Util.fixNull(this.label).trim(); }
  
  public void setLabelString(String label) {
    _setLabelString(label);
    save();
  }
  
  private void _setLabelString(String label) {
    this.label = label;
    if (getInstanceOrNull() != null)
      this.labelAtomSet = Collections.unmodifiableSet(Label.parse(label)); 
  }
  
  @NonNull
  public LabelAtom getSelfLabel() {
    if (nodeNameAndSelfLabelOverride != null)
      return getLabelAtom(nodeNameAndSelfLabelOverride); 
    if (getRenameMigrationDone())
      return getLabelAtom("built-in"); 
    return getLabelAtom("master");
  }
  
  boolean getRenameMigrationDone() {
    if (this.nodeRenameMigrationNeeded == null)
      return true; 
    return !this.nodeRenameMigrationNeeded.booleanValue();
  }
  
  void performRenameMigration() throws IOException {
    this.nodeRenameMigrationNeeded = Boolean.valueOf(false);
    save();
    trimLabels();
  }
  
  @NonNull
  public Computer createComputer() { return new Hudson.MasterComputer(); }
  
  private void loadConfig() throws IOException {
    XmlFile cfg = getConfigFile();
    if (cfg.exists()) {
      this.primaryView = null;
      this.views.clear();
      cfg.unmarshal(this);
    } 
    if (this.views.isEmpty() || this.primaryView == null) {
      AllView allView = new AllView("all");
      setViewOwner(allView);
      this.views.add(0, allView);
      this.primaryView = allView.getViewName();
    } 
    this.primaryView = AllView.migrateLegacyPrimaryAllViewLocalizedName(this.views, this.primaryView);
    this.configLoaded = true;
    try {
      checkRawBuildsDir(this.buildsDir);
      setBuildsAndWorkspacesDir();
      resetFilter(this.securityRealm, null);
    } catch (InvalidBuildsDir invalidBuildsDir) {
      throw new IOException(invalidBuildsDir);
    } 
  }
  
  private void setBuildsAndWorkspacesDir() throws IOException {
    boolean mustSave = false;
    String newBuildsDir = SystemProperties.getString(BUILDS_DIR_PROP);
    boolean freshStartup = STARTUP_MARKER_FILE.isOff();
    if (newBuildsDir != null && !this.buildsDir.equals(newBuildsDir)) {
      checkRawBuildsDir(newBuildsDir);
      Level level = freshStartup ? Level.INFO : Level.WARNING;
      LOGGER.log(level, "Changing builds directories from {0} to {1}. Beware that no automated data migration will occur.", new String[] { this.buildsDir, newBuildsDir });
      this.buildsDir = newBuildsDir;
      mustSave = true;
    } else if (!isDefaultBuildDir()) {
      LOGGER.log(Level.INFO, "Using non default builds directories: {0}.", this.buildsDir);
    } 
    String newWorkspacesDir = SystemProperties.getString(WORKSPACES_DIR_PROP);
    if (newWorkspacesDir != null && !this.workspaceDir.equals(newWorkspacesDir)) {
      Level level = freshStartup ? Level.INFO : Level.WARNING;
      LOGGER.log(level, "Changing workspaces directories from {0} to {1}. Beware that no automated data migration will occur.", new String[] { this.workspaceDir, newWorkspacesDir });
      this.workspaceDir = newWorkspacesDir;
      mustSave = true;
    } else if (!isDefaultWorkspaceDir()) {
      LOGGER.log(Level.INFO, "Using non default workspaces directories: {0}.", this.workspaceDir);
    } 
    if (mustSave)
      save(); 
  }
  
  @VisibleForTesting
  static void checkRawBuildsDir(String newBuildsDirValue) {
    String replacedValue = expandVariablesForDirectory(newBuildsDirValue, "doCheckRawBuildsDir-Marker:foo", get().getRootDir().getPath() + "/jobs/doCheckRawBuildsDir-Marker$foo");
    File replacedFile = new File(replacedValue);
    if (!replacedFile.isAbsolute())
      throw new InvalidBuildsDir(newBuildsDirValue + " does not resolve to an absolute path"); 
    if (!replacedValue.contains("doCheckRawBuildsDir-Marker"))
      throw new InvalidBuildsDir(newBuildsDirValue + " does not contain ${ITEM_FULL_NAME} or ${ITEM_ROOTDIR}, cannot distinguish between projects"); 
    if (replacedValue.contains("doCheckRawBuildsDir-Marker:foo"))
      try {
        File tmp = File.createTempFile("Jenkins-doCheckRawBuildsDir", "foo:bar");
        Files.delete(tmp.toPath());
      } catch (IOException|java.nio.file.InvalidPathException e) {
        throw (InvalidBuildsDir)(new InvalidBuildsDir(newBuildsDirValue + " contains ${ITEM_FULLNAME} but your system does not support it (JENKINS-12251). Use ${ITEM_FULL_NAME} instead")).initCause(e);
      }  
    File d = new File(replacedValue);
    if (!d.isDirectory()) {
      d = d.getParentFile();
      while (!d.exists())
        d = d.getParentFile(); 
      if (!d.canWrite())
        throw new InvalidBuildsDir(newBuildsDirValue + " does not exist and probably cannot be created"); 
    } 
  }
  
  private TaskBuilder loadTasks() throws IOException {
    File projectsDir = new File(this.root, "jobs");
    if (!projectsDir.getCanonicalFile().isDirectory() && !projectsDir.mkdirs()) {
      if (projectsDir.exists())
        throw new IOException("" + projectsDir + " is not a directory"); 
      throw new IOException("Unable to create " + projectsDir + "\nPermission issue? Please create this directory manually.");
    } 
    File[] subdirs = projectsDir.listFiles();
    Set<String> loadedNames = Collections.synchronizedSet(new HashSet());
    TaskGraphBuilder g = new TaskGraphBuilder();
    TaskGraphBuilder.Handle loadJenkins = g.requires(new Milestone[] { InitMilestone.EXTENSIONS_AUGMENTED }).attains(new Milestone[] { InitMilestone.SYSTEM_CONFIG_LOADED }).add("Loading global config", new Object(this));
    List<TaskGraphBuilder.Handle> loadJobs = new ArrayList<TaskGraphBuilder.Handle>();
    for (File subdir : subdirs) {
      loadJobs.add(g.requires(new Milestone[] { loadJenkins }).attains(new Milestone[] { InitMilestone.JOB_LOADED }).notFatal().add("Loading item " + subdir.getName(), new Object(this, subdir, loadedNames)));
    } 
    g.requires((Milestone[])loadJobs.toArray(new TaskGraphBuilder.Handle[0])).attains(new Milestone[] { InitMilestone.JOB_LOADED }).add("Cleaning up obsolete items deleted from the disk", new Object(this, loadedNames));
    g.requires(new Milestone[] { InitMilestone.JOB_CONFIG_ADAPTED }).attains(new Milestone[] { InitMilestone.COMPLETED }).add("Finalizing set up", new Object(this));
    return g;
  }
  
  public void save() throws IOException {
    InitMilestone currentMilestone = this.initLevel;
    if (!this.configLoaded) {
      LOGGER.log(Level.SEVERE, "An attempt to save Jenkins'' global configuration before it has been loaded has been made during milestone " + currentMilestone + ".  This is indicative of a bug in the caller and may lead to full or partial loss of configuration.", new IllegalStateException("call trace"));
      throw new IllegalStateException("An attempt to save the global configuration was made before it was loaded");
    } 
    if (BulkChange.contains(this))
      return; 
    if (currentMilestone == InitMilestone.COMPLETED) {
      LOGGER.log(Level.FINE, "setting version {0} to {1}", new Object[] { this.version, VERSION });
      this.version = VERSION;
    } else {
      LOGGER.log(Level.FINE, "refusing to set version {0} to {1} during {2}", new Object[] { this.version, VERSION, currentMilestone });
    } 
    if (this.nodeRenameMigrationNeeded == null)
      this.nodeRenameMigrationNeeded = Boolean.valueOf(false); 
    getConfigFile().write(this);
    SaveableListener.fireOnChange(this, getConfigFile());
  }
  
  private void saveQuietly() throws IOException {
    try {
      save();
    } catch (IOException x) {
      LOGGER.log(Level.WARNING, null, x);
    } 
  }
  
  public void cleanUp() throws IOException {
    if (theInstance != this && theInstance != null) {
      LOGGER.log(Level.WARNING, "This instance is no longer the singleton, ignoring cleanUp()");
      return;
    } 
    synchronized (Jenkins.class) {
      if (this.cleanUpStarted) {
        LOGGER.log(Level.WARNING, "Jenkins.cleanUp() already started, ignoring repeated cleanUp()");
        return;
      } 
      this.cleanUpStarted = true;
    } 
    try {
      getLifecycle().onStatusUpdate("Stopping Jenkins");
      List<Throwable> errors = new ArrayList<Throwable>();
      fireBeforeShutdown(errors);
      _cleanUpRunTerminators(errors);
      this.terminating = true;
      Set<Future<?>> pending = _cleanUpDisconnectComputers(errors);
      _cleanUpCancelDependencyGraphCalculation();
      _cleanUpInterruptReloadThread(errors);
      _cleanUpShutdownTriggers(errors);
      _cleanUpShutdownTimer(errors);
      _cleanUpShutdownTcpSlaveAgent(errors);
      _cleanUpShutdownPluginManager(errors);
      _cleanUpPersistQueue(errors);
      _cleanUpShutdownThreadPoolForLoad(errors);
      _cleanUpAwaitDisconnects(errors, pending);
      _cleanUpPluginServletFilters(errors);
      _cleanUpReleaseAllLoggers(errors);
      getLifecycle().onStatusUpdate("Jenkins stopped");
      if (!errors.isEmpty()) {
        StringBuilder message = new StringBuilder("Unexpected issues encountered during cleanUp: ");
        Iterator<Throwable> iterator = errors.iterator();
        message.append(((Throwable)iterator.next()).getMessage());
        while (iterator.hasNext()) {
          message.append("; ");
          message.append(((Throwable)iterator.next()).getMessage());
        } 
        iterator = errors.iterator();
        RuntimeException exception = new RuntimeException(message.toString(), (Throwable)iterator.next());
        while (iterator.hasNext())
          exception.addSuppressed((Throwable)iterator.next()); 
        throw exception;
      } 
    } finally {
      theInstance = null;
      if (JenkinsJVM.isJenkinsJVM())
        JenkinsJVMAccess._setJenkinsJVM(this.oldJenkinsJVM); 
      ClassFilterImpl.unregister();
    } 
  }
  
  private void fireBeforeShutdown(List<Throwable> errors) {
    LOGGER.log(Level.FINE, "Notifying termination");
    for (ItemListener l : ItemListener.all()) {
      try {
        l.onBeforeShutdown();
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (LinkageError e) {
        LOGGER.log(Level.WARNING, e, () -> "ItemListener " + l + ": " + e.getMessage());
      } catch (Throwable e) {
        LOGGER.log(Level.WARNING, e, () -> "ItemListener " + l + ": " + e.getMessage());
        errors.add(e);
      } 
    } 
  }
  
  private void _cleanUpRunTerminators(List<Throwable> errors) {
    try {
      TerminatorFinder tf = new TerminatorFinder((this.pluginManager != null) ? this.pluginManager.uberClassLoader : Thread.currentThread().getContextClassLoader());
      (new Reactor(new TaskBuilder[] { tf })).execute(Runnable::run, new Object(this));
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (LinkageError e) {
      LOGGER.log(Level.SEVERE, "Failed to execute termination", e);
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to execute termination", e);
      errors.add(e);
    } 
  }
  
  private Set<Future<?>> _cleanUpDisconnectComputers(List<Throwable> errors) {
    LOGGER.log(Main.isUnitTest ? Level.FINE : Level.INFO, "Starting node disconnection");
    Set<Future<?>> pending = new HashSet<Future<?>>();
    Queue.withLock(() -> {
          for (Computer c : this.computers.values()) {
            try {
              c.interrupt();
              killComputer(c);
              pending.add(c.disconnect(null));
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (LinkageError e) {
              LOGGER.log(Level.WARNING, e, ());
            } catch (Throwable e) {
              LOGGER.log(Level.WARNING, e, ());
              errors.add(e);
            } 
          } 
        });
    return pending;
  }
  
  private void _cleanUpInterruptReloadThread(List<Throwable> errors) {
    LOGGER.log(Level.FINE, "Interrupting reload thread");
    try {
      interruptReloadThread();
    } catch (SecurityException e) {
      LOGGER.log(Level.WARNING, "Not permitted to interrupt reload thread", e);
      errors.add(e);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (LinkageError e) {
      LOGGER.log(Level.SEVERE, "Failed to interrupt reload thread", e);
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to interrupt reload thread", e);
      errors.add(e);
    } 
  }
  
  private void _cleanUpShutdownTriggers(List<Throwable> errors) {
    LOGGER.log(Level.FINE, "Shutting down triggers");
    try {
      Timer timer = Trigger.timer;
      if (timer != null) {
        CountDownLatch latch = new CountDownLatch(1);
        timer.schedule(new Object(this, timer, latch), 0L);
        if (latch.await(10L, TimeUnit.SECONDS)) {
          LOGGER.log(Level.FINE, "Triggers shut down successfully");
        } else {
          timer.cancel();
          LOGGER.log(Level.INFO, "Gave up waiting for triggers to finish running");
        } 
      } 
      Trigger.timer = null;
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (LinkageError e) {
      LOGGER.log(Level.SEVERE, "Failed to shut down triggers", e);
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to shut down triggers", e);
      errors.add(e);
    } 
  }
  
  private void _cleanUpShutdownTimer(List<Throwable> errors) {
    LOGGER.log(Level.FINE, "Shutting down timer");
    try {
      Timer.shutdown();
    } catch (SecurityException e) {
      LOGGER.log(Level.WARNING, "Not permitted to shut down Timer", e);
      errors.add(e);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (LinkageError e) {
      LOGGER.log(Level.SEVERE, "Failed to shut down Timer", e);
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to shut down Timer", e);
      errors.add(e);
    } 
  }
  
  private void _cleanUpShutdownTcpSlaveAgent(List<Throwable> errors) {
    if (this.tcpSlaveAgentListener != null) {
      LOGGER.log(Level.FINE, "Shutting down TCP/IP agent listener");
      try {
        this.tcpSlaveAgentListener.shutdown();
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (LinkageError e) {
        LOGGER.log(Level.SEVERE, "Failed to shut down TCP/IP agent listener", e);
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Failed to shut down TCP/IP agent listener", e);
        errors.add(e);
      } 
    } 
  }
  
  private void _cleanUpShutdownPluginManager(List<Throwable> errors) {
    if (this.pluginManager != null) {
      LOGGER.log(Main.isUnitTest ? Level.FINE : Level.INFO, "Stopping plugin manager");
      try {
        this.pluginManager.stop();
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (LinkageError e) {
        LOGGER.log(Level.SEVERE, "Failed to stop plugin manager", e);
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Failed to stop plugin manager", e);
        errors.add(e);
      } 
    } 
  }
  
  private void _cleanUpPersistQueue(List<Throwable> errors) {
    if (getRootDir().exists()) {
      LOGGER.log(Main.isUnitTest ? Level.FINE : Level.INFO, "Persisting build queue");
      try {
        getQueue().save();
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (LinkageError e) {
        LOGGER.log(Level.SEVERE, "Failed to persist build queue", e);
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Failed to persist build queue", e);
        errors.add(e);
      } 
    } 
  }
  
  private void _cleanUpShutdownThreadPoolForLoad(List<Throwable> errors) {
    LOGGER.log(Level.FINE, "Shutting down Jenkins load thread pool");
    try {
      this.threadPoolForLoad.shutdown();
    } catch (SecurityException e) {
      LOGGER.log(Level.WARNING, "Not permitted to shut down Jenkins load thread pool", e);
      errors.add(e);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (LinkageError e) {
      LOGGER.log(Level.SEVERE, "Failed to shut down Jenkins load thread pool", e);
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to shut down Jenkins load thread pool", e);
      errors.add(e);
    } 
  }
  
  private void _cleanUpAwaitDisconnects(List<Throwable> errors, Set<Future<?>> pending) {
    if (!pending.isEmpty())
      LOGGER.log(Main.isUnitTest ? Level.FINE : Level.INFO, "Waiting for node disconnection completion"); 
    for (Future<?> f : pending) {
      try {
        f.get(10L, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (ExecutionException e) {
        LOGGER.log(Level.WARNING, "Failed to shut down remote computer connection cleanly", e);
      } catch (TimeoutException e) {
        LOGGER.log(Level.WARNING, "Failed to shut down remote computer connection within 10 seconds", e);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (LinkageError e) {
        LOGGER.log(Level.WARNING, "Failed to shut down remote computer connection", e);
      } catch (Throwable e) {
        LOGGER.log(Level.SEVERE, "Unexpected error while waiting for remote computer connection disconnect", e);
        errors.add(e);
      } 
    } 
  }
  
  private void _cleanUpPluginServletFilters(List<Throwable> errors) {
    LOGGER.log(Level.FINE, "Stopping filters");
    try {
      PluginServletFilter.cleanUp();
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (LinkageError e) {
      LOGGER.log(Level.SEVERE, "Failed to stop filters", e);
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to stop filters", e);
      errors.add(e);
    } 
  }
  
  private void _cleanUpReleaseAllLoggers(List<Throwable> errors) {
    LOGGER.log(Level.FINE, "Releasing all loggers");
    try {
      LogFactory.releaseAll();
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (LinkageError e) {
      LOGGER.log(Level.SEVERE, "Failed to release all loggers", e);
    } catch (Throwable e) {
      LOGGER.log(Level.SEVERE, "Failed to release all loggers", e);
      errors.add(e);
    } 
  }
  
  private void _cleanUpCancelDependencyGraphCalculation() throws IOException {
    synchronized (this.dependencyGraphLock) {
      LOGGER.log(Level.FINE, "Canceling internal dependency graph calculation");
      if (this.scheduledFutureDependencyGraph != null && !this.scheduledFutureDependencyGraph.isDone())
        this.scheduledFutureDependencyGraph.cancel(true); 
      if (this.calculatingFutureDependencyGraph != null && !this.calculatingFutureDependencyGraph.isDone())
        this.calculatingFutureDependencyGraph.cancel(true); 
    } 
  }
  
  public Object getDynamic(String token) throws IOException {
    for (Action a : getActions()) {
      String url = a.getUrlName();
      if (url != null && (url.equals(token) || url.equals("/" + token)))
        return a; 
    } 
    for (Action a : getManagementLinks()) {
      if (Objects.equals(a.getUrlName(), token))
        return a; 
    } 
    return null;
  }
  
  @POST
  public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    BulkChange bc = new BulkChange(this);
    try {
      checkPermission(MANAGE);
      JSONObject json = req.getSubmittedForm();
      this.systemMessage = Util.nullify(req.getParameter("system_message"));
      boolean result = true;
      for (Descriptor<?> d : Functions.getSortedDescriptorsForGlobalConfigUnclassified())
        result &= configureDescriptor(req, json, d); 
      save();
      updateComputerList();
      if (result) {
        FormApply.success(req.getContextPath() + "/").generateResponse(req, rsp, null);
      } else {
        FormApply.success("configure").generateResponse(req, rsp, null);
      } 
      bc.commit();
      bc.close();
    } catch (Throwable throwable) {
      try {
        bc.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  @CheckForNull
  public CrumbIssuer getCrumbIssuer() { return GlobalCrumbIssuerConfiguration.DISABLE_CSRF_PROTECTION ? null : this.crumbIssuer; }
  
  public void setCrumbIssuer(CrumbIssuer issuer) { this.crumbIssuer = issuer; }
  
  public void doTestPost(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException { rsp.sendRedirect("foo"); }
  
  private boolean configureDescriptor(StaplerRequest req, JSONObject json, Descriptor<?> d) throws Descriptor.FormException {
    String name = d.getJsonSafeClassName();
    JSONObject js = json.has(name) ? json.getJSONObject(name) : new JSONObject();
    json.putAll(js);
    return d.configure(req, js);
  }
  
  @POST
  public void doConfigExecutorsSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    checkPermission(ADMINISTER);
    BulkChange bc = new BulkChange(this);
    try {
      JSONObject json = req.getSubmittedForm();
      ((MasterBuildConfiguration)ExtensionList.lookupSingleton(MasterBuildConfiguration.class)).configure(req, json);
      getNodeProperties().rebuild(req, json.optJSONObject("nodeProperties"), NodeProperty.all());
      bc.commit();
      bc.close();
    } catch (Throwable throwable) {
      try {
        bc.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
    updateComputerList();
    rsp.sendRedirect(req.getContextPath() + "/" + req.getContextPath());
  }
  
  @RequirePOST
  public void doSubmitDescription(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException { getPrimaryView().doSubmitDescription(req, rsp); }
  
  @RequirePOST
  public HttpRedirect doQuietDown() {
    try {
      return doQuietDown(false, 0, null);
    } catch (IOException|InterruptedException e) {
      throw new AssertionError(e);
    } 
  }
  
  @Deprecated
  public HttpRedirect doQuietDown(boolean block, int timeout) {
    try {
      return doQuietDown(block, timeout, null);
    } catch (IOException|InterruptedException e) {
      throw new AssertionError(e);
    } 
  }
  
  @Deprecated(since = "2.414")
  public HttpRedirect doQuietDown(boolean block, int timeout, @CheckForNull String message) throws InterruptedException, IOException { return doQuietDown(block, timeout, message, false); }
  
  @RequirePOST
  public HttpRedirect doQuietDown(@QueryParameter boolean block, @QueryParameter int timeout, @QueryParameter @CheckForNull String message, @QueryParameter boolean safeRestart) throws InterruptedException, IOException {
    synchronized (this) {
      checkPermission(MANAGE);
      this.quietDownInfo = new QuietDownInfo(message, safeRestart);
    } 
    if (block) {
      long waitUntil = timeout;
      if (timeout > 0)
        waitUntil += System.currentTimeMillis(); 
      while (isQuietingDown() && (timeout <= 0 || System.currentTimeMillis() < waitUntil) && !RestartListener.isAllReady())
        TimeUnit.SECONDS.sleep(1L); 
    } 
    return new HttpRedirect(".");
  }
  
  @RequirePOST
  public HttpRedirect doCancelQuietDown() {
    checkPermission(MANAGE);
    this.quietDownInfo = null;
    getQueue().scheduleMaintenance();
    return new HttpRedirect(".");
  }
  
  public HttpResponse doToggleCollapse() throws ServletException, IOException {
    StaplerRequest request = Stapler.getCurrentRequest();
    String paneId = request.getParameter("paneId");
    PaneStatusProperties.forCurrentUser().toggleCollapsed(paneId);
    return HttpResponses.forwardToPreviousPage();
  }
  
  public void doClassicThreadDump(StaplerResponse rsp) throws IOException, ServletException { rsp.sendRedirect2("threadDump"); }
  
  public Map<String, Map<String, String>> getAllThreadDumps() throws IOException, InterruptedException {
    checkPermission(ADMINISTER);
    Map<String, Future<Map<String, String>>> future = new HashMap<String, Future<Map<String, String>>>();
    for (Computer c : getComputers()) {
      try {
        future.put(c.getName(), RemotingDiagnostics.getThreadDumpAsync(c.getChannel()));
      } catch (Exception e) {
        LOGGER.info("Failed to get thread dump for node " + c.getName() + ": " + e.getMessage());
      } 
    } 
    if (toComputer() == null)
      future.put("master", RemotingDiagnostics.getThreadDumpAsync(FilePath.localChannel)); 
    long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5L);
    Map<String, Map<String, String>> r = new HashMap<String, Map<String, String>>();
    for (Map.Entry<String, Future<Map<String, String>>> e : future.entrySet()) {
      try {
        r.put((String)e.getKey(), (Map)((Future)e.getValue()).get(endTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
      } catch (Exception x) {
        r.put((String)e.getKey(), Map.of("Failed to retrieve thread dump", Functions.printThrowable(x)));
      } 
    } 
    return Collections.unmodifiableSortedMap(new TreeMap(r));
  }
  
  @RequirePOST
  public TopLevelItem doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException { return this.itemGroupMixIn.createTopLevelItem(req, rsp); }
  
  public TopLevelItem createProjectFromXML(String name, InputStream xml) throws IOException { return this.itemGroupMixIn.createProjectFromXML(name, xml); }
  
  public <T extends TopLevelItem> T copy(T src, String name) throws IOException { return (T)this.itemGroupMixIn.copy(src, name); }
  
  public <T extends AbstractProject<?, ?>> T copy(T src, String name) throws IOException { return (T)(AbstractProject)copy((TopLevelItem)src, name); }
  
  @POST
  public void doCreateView(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    checkPermission(View.CREATE);
    addView(View.create(req, rsp, this));
  }
  
  public static void checkGoodName(String name) {
    if (name == null || name.isEmpty())
      throw new Failure(Messages.Hudson_NoName()); 
    if (".".equals(name.trim()))
      throw new Failure(Messages.Jenkins_NotAllowedName(".")); 
    if ("..".equals(name.trim()))
      throw new Failure(Messages.Jenkins_NotAllowedName("..")); 
    for (int i = 0; i < name.length(); i++) {
      char ch = name.charAt(i);
      if (Character.isISOControl(ch))
        throw new Failure(Messages.Hudson_ControlCodeNotAllowed(toPrintableName(name))); 
      if ("?*/\\%!@#$^&|<>[]:;".indexOf(ch) != -1)
        throw new Failure(Messages.Hudson_UnsafeChar(Character.valueOf(ch))); 
    } 
    if (SystemProperties.getBoolean(NAME_VALIDATION_REJECTS_TRAILING_DOT_PROP, true))
      if (name.trim().endsWith("."))
        throw new Failure(Messages.Hudson_TrailingDot());  
  }
  
  private static String toPrintableName(String name) {
    StringBuilder printableName = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char ch = name.charAt(i);
      if (Character.isISOControl(ch)) {
        printableName.append("\\u").append(ch).append(';');
      } else {
        printableName.append(ch);
      } 
    } 
    return printableName.toString();
  }
  
  public void doSecured(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    if (req.getUserPrincipal() == null) {
      rsp.setStatus(401);
      return;
    } 
    String path = req.getContextPath() + req.getContextPath();
    String q = req.getQueryString();
    if (q != null)
      path = path + "?" + path; 
    rsp.sendRedirect2(path);
  }
  
  public void doLoginEntry(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    if (req.getUserPrincipal() == null) {
      rsp.sendRedirect2("noPrincipal");
      return;
    } 
    String from = req.getParameter("from");
    if (from != null && from.startsWith("/") && !from.equals("/loginError")) {
      rsp.sendRedirect2(from);
      return;
    } 
    rsp.sendRedirect2(".");
  }
  
  public void doLogout(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    String user = getAuthentication2().getName();
    this.securityRealm.doLogout(req, rsp);
    SecurityListener.fireLoggedOut(user);
  }
  
  public Slave.JnlpJar getJnlpJars(String fileName) { return new Slave.JnlpJar(fileName); }
  
  public Slave.JnlpJar doJnlpJars(StaplerRequest req) { return new Slave.JnlpJar(req.getRestOfPath().substring(1)); }
  
  @RequirePOST
  public HttpResponse doReload() throws ServletException, IOException {
    checkPermission(MANAGE);
    getLifecycle().onReload(getAuthentication2().getName(), null);
    WebApp.get(this.servletContext).setApp(new HudsonIsLoading());
    (new Object(this, "Jenkins config reload thread")).start();
    return HttpResponses.redirectViaContextPath("/");
  }
  
  public void reload() throws IOException {
    this.queue.save();
    executeReactor(null, new TaskBuilder[] { loadTasks() });
    if (this.initLevel != InitMilestone.COMPLETED)
      LOGGER.log(Level.SEVERE, "Jenkins initialization has not reached the COMPLETED initialization milestone after the configuration reload. Current state: {0}. It may cause undefined incorrect behavior in Jenkins plugin relying on this state. It is likely an issue with the Initialization task graph. Example: usage of @Initializer(after = InitMilestone.COMPLETED) in a plugin (JENKINS-37759). Please create a bug in Jenkins bugtracker.", this.initLevel); 
    User.reload();
    this.queue.load();
    WebApp.get(this.servletContext).setApp(this);
  }
  
  @RequirePOST
  public void doDoFingerprintCheck(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    MultipartFormDataParser p = new MultipartFormDataParser(req, 10);
    try {
      if (isUseCrumbs() && !getCrumbIssuer().validateCrumb(req, p))
        rsp.sendError(403, "No crumb found"); 
      rsp.sendRedirect2(req.getContextPath() + "/fingerprint/" + req.getContextPath() + "/");
      p.close();
    } catch (Throwable throwable) {
      try {
        p.close();
      } catch (Throwable throwable1) {
        throwable.addSuppressed(throwable1);
      } 
      throw throwable;
    } 
  }
  
  @RequirePOST
  @SuppressFBWarnings(value = {"DM_GC"}, justification = "for debugging")
  public void doGc(StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(ADMINISTER);
    System.gc();
    rsp.setStatus(200);
    rsp.setContentType("text/plain");
    rsp.getWriter().println("GCed");
  }
  
  @StaplerDispatchable
  public void doException() throws IOException { throw new RuntimeException(); }
  
  public ModelObjectWithContextMenu.ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws IOException, JellyException {
    ModelObjectWithContextMenu.ContextMenu menu = (new ModelObjectWithContextMenu.ContextMenu()).from(this, request, response);
    for (ModelObjectWithContextMenu.MenuItem i : menu.items) {
      if (i.url.equals(request.getContextPath() + "/manage"))
        i.subMenu = (new ModelObjectWithContextMenu.ContextMenu()).from((ModelObjectWithContextMenu)ExtensionList.lookupSingleton(hudson.model.ManageJenkinsAction.class), request, response, "index"); 
    } 
    return menu;
  }
  
  public ModelObjectWithContextMenu.ContextMenu doChildrenContextMenu(StaplerRequest request, StaplerResponse response) throws IOException, JellyException {
    ModelObjectWithContextMenu.ContextMenu menu = new ModelObjectWithContextMenu.ContextMenu();
    for (View view : getViews())
      menu.add(view.getViewUrl(), view.getDisplayName()); 
    return menu;
  }
  
  public RemotingDiagnostics.HeapDump getHeapDump() throws IOException { return new RemotingDiagnostics.HeapDump(this, FilePath.localChannel); }
  
  @RequirePOST
  public void doSimulateOutOfMemory() throws IOException {
    checkPermission(ADMINISTER);
    System.out.println("Creating artificial OutOfMemoryError situation");
    List<Object> args = new ArrayList<Object>();
    while (true)
      args.add(new byte[1048576]); 
  }
  
  public DirectoryBrowserSupport doUserContent() { return new DirectoryBrowserSupport(this, getRootPath().child("userContent"), "User content", "folder.png", true); }
  
  @CLIMethod(name = "restart")
  public void doRestart(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    checkPermission(MANAGE);
    if (req != null && req.getMethod().equals("GET")) {
      req.getView(this, "_restart.jelly").forward(req, rsp);
      return;
    } 
    if (req == null || req.getMethod().equals("POST"))
      restart(); 
    if (rsp != null)
      rsp.sendRedirect2("."); 
  }
  
  @WebMethod(name = {"404"})
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public void generateNotFoundResponse(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    if (ResourceDomainConfiguration.isResourceRequest(req)) {
      rsp.forward(this, "_404_simple", req);
    } else {
      Object attribute = req.getAttribute("jenkins.ErrorAttributeFilter.user");
      if (attribute instanceof Authentication) {
        ACLContext unused = ACL.as2((Authentication)attribute);
        try {
          rsp.forward(this, "_404", req);
          if (unused != null)
            unused.close(); 
        } catch (Throwable throwable) {
          if (unused != null)
            try {
              unused.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } else {
        rsp.forward(this, "_404", req);
      } 
    } 
  }
  
  @Deprecated(since = "2.414")
  public HttpResponse doSafeRestart(StaplerRequest req) throws IOException, ServletException, RestartNotSupportedException { return doSafeRestart(req, null); }
  
  public HttpResponse doSafeRestart(StaplerRequest req, @QueryParameter("message") String message) throws IOException, ServletException, RestartNotSupportedException {
    checkPermission(MANAGE);
    if (req != null && req.getMethod().equals("GET"))
      return HttpResponses.forwardToView(this, "_safeRestart.jelly"); 
    if (req != null && req.getParameter("cancel") != null)
      return doCancelQuietDown(); 
    if (req == null || req.getMethod().equals("POST"))
      safeRestart(message); 
    return HttpResponses.redirectToDot();
  }
  
  private static Lifecycle restartableLifecycle() {
    if (Main.isUnitTest)
      throw new RestartNotSupportedException("Restarting the controller JVM is not supported in JenkinsRule-based tests"); 
    lifecycle = Lifecycle.get();
    lifecycle.verifyRestartable();
    return lifecycle;
  }
  
  public void restart() throws IOException {
    Lifecycle lifecycle = restartableLifecycle();
    this.servletContext.setAttribute("app", new HudsonIsRestarting());
    (new Object(this, "restart thread", lifecycle)).start();
  }
  
  @Deprecated(since = "2.414")
  public void safeRestart() throws IOException { safeRestart(null); }
  
  public void safeRestart(String message) {
    Lifecycle lifecycle = restartableLifecycle();
    this.quietDownInfo = new QuietDownInfo(message, true);
    (new Object(this, "safe-restart thread", message, lifecycle)).start();
  }
  
  @CLIMethod(name = "shutdown")
  @RequirePOST
  public void doExit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    checkPermission(ADMINISTER);
    String exitUser = getAuthentication2().getName();
    String exitAddr = (req != null) ? req.getRemoteAddr() : null;
    if (rsp != null) {
      rsp.setStatus(200);
      rsp.setContentType("text/plain");
      PrintWriter w = rsp.getWriter();
      try {
        w.println("Shutting down");
        if (w != null)
          w.close(); 
      } catch (Throwable throwable) {
        if (w != null)
          try {
            w.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } 
    (new Object(this, "exit thread", exitUser, exitAddr)).start();
  }
  
  @CLIMethod(name = "safe-shutdown")
  @RequirePOST
  public HttpResponse doSafeExit(StaplerRequest req) throws IOException, ServletException, RestartNotSupportedException {
    checkPermission(ADMINISTER);
    this.quietDownInfo = new QuietDownInfo();
    String exitUser = getAuthentication2().getName();
    String exitAddr = (req != null) ? req.getRemoteAddr() : null;
    (new Object(this, "safe-exit thread", exitUser, exitAddr)).start();
    return HttpResponses.plainText("Shutting down as soon as all jobs are complete");
  }
  
  @NonNull
  public static Authentication getAuthentication2() {
    a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null)
      a = ANONYMOUS2; 
    return a;
  }
  
  @Deprecated
  @NonNull
  public static Authentication getAuthentication() { return Authentication.fromSpring(getAuthentication2()); }
  
  public void doScript(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException { _doScript(req, rsp, req.getView(this, "_script.jelly"), FilePath.localChannel, getACL()); }
  
  public void doScriptText(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException { _doScript(req, rsp, req.getView(this, "_scriptText.jelly"), FilePath.localChannel, getACL()); }
  
  public static void _doScript(StaplerRequest req, StaplerResponse rsp, RequestDispatcher view, VirtualChannel channel, ACL acl) throws IOException, ServletException {
    acl.checkPermission(ADMINISTER);
    String text = req.getParameter("script");
    if (text != null) {
      if (!"POST".equals(req.getMethod()))
        throw HttpResponses.error(405, "requires POST"); 
      if (channel == null)
        throw HttpResponses.error(404, "Node is offline"); 
      try {
        req.setAttribute("output", RemotingDiagnostics.executeGroovy(text, channel));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new ServletException(e);
      } 
    } 
    view.forward(req, rsp);
  }
  
  @RequirePOST
  public void doEval(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    checkPermission(ADMINISTER);
    req.getWebApp().getDispatchValidator().allowDispatch(req, rsp);
    try {
      MetaClass mc = req.getWebApp().getMetaClass(getClass());
      Script script = ((JellyClassLoaderTearOff)mc.classLoader.loadTearOff(JellyClassLoaderTearOff.class)).createContext().compileScript(new InputSource(req.getReader()));
      (new JellyRequestDispatcher(this, script)).forward(req, rsp);
    } catch (JellyException e) {
      throw new ServletException(e);
    } 
  }
  
  public void doSignup(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    if (getSecurityRealm().allowsSignup()) {
      req.getView(getSecurityRealm(), "signup.jelly").forward(req, rsp);
      return;
    } 
    req.getView(SecurityRealm.class, "signup.jelly").forward(req, rsp);
  }
  
  public void doIconSize(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    String qs = req.getQueryString();
    if (qs == null)
      throw new ServletException(); 
    Cookie cookie = new Cookie("iconSize", Functions.validateIconSize(qs));
    cookie.setMaxAge(9999999);
    cookie.setSecure(req.isSecure());
    cookie.setHttpOnly(true);
    rsp.addCookie(cookie);
    String ref = req.getHeader("Referer");
    if (ref == null)
      ref = "."; 
    rsp.sendRedirect2(ref);
  }
  
  @RequirePOST
  public void doFingerprintCleanup(StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(ADMINISTER);
    FingerprintCleanupThread.invoke();
    rsp.setStatus(200);
    rsp.setContentType("text/plain");
    rsp.getWriter().println("Invoked");
  }
  
  @RequirePOST
  public void doWorkspaceCleanup(StaplerResponse rsp) throws IOException, ServletException {
    checkPermission(ADMINISTER);
    WorkspaceCleanupThread.invoke();
    rsp.setStatus(200);
    rsp.setContentType("text/plain");
    rsp.getWriter().println("Invoked");
  }
  
  public FormValidation doDefaultJDKCheck(StaplerRequest request, @QueryParameter String value) {
    if (!JDK.isDefaultName(value))
      return FormValidation.ok(); 
    if (JDK.isDefaultJDKValid(this))
      return FormValidation.ok(); 
    return FormValidation.errorWithMarkup(Messages.Hudson_NoJavaInPath(request.getContextPath()));
  }
  
  public FormValidation doCheckViewName(@QueryParameter String value) {
    checkPermission(View.CREATE);
    String name = Util.fixEmpty(value);
    if (name == null)
      return FormValidation.ok(); 
    if (getView(name) != null)
      return FormValidation.error(Messages.Hudson_ViewAlreadyExists(name)); 
    try {
      checkGoodName(name);
    } catch (Failure e) {
      return FormValidation.error(e.getMessage());
    } 
    return FormValidation.ok();
  }
  
  @Deprecated
  public FormValidation doViewExistsCheck(@QueryParameter String value) {
    checkPermission(View.CREATE);
    String view = Util.fixEmpty(value);
    if (view == null)
      return FormValidation.ok(); 
    if (getView(view) == null)
      return FormValidation.ok(); 
    return FormValidation.error(Messages.Hudson_ViewAlreadyExists(view));
  }
  
  public void doResources(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, Descriptor.FormException {
    String path = req.getRestOfPath();
    path = path.substring(path.indexOf('/', 1) + 1);
    int idx = path.lastIndexOf('.');
    String extension = path.substring(idx + 1);
    if (ALLOWED_RESOURCE_EXTENSIONS.contains(extension)) {
      URL url = this.pluginManager.uberClassLoader.getResource(path);
      if (url != null) {
        long expires = MetaClass.NO_CACHE ? 0L : TimeUnit.DAYS.toMillis(365L);
        rsp.serveFile(req, url, expires);
        return;
      } 
    } 
    rsp.sendError(404);
  }
  
  @SuppressFBWarnings(value = {"MS_MUTABLE_COLLECTION_PKGPROTECT"}, justification = "mutable to allow plugins to add additional extensions")
  public static final Set<String> ALLOWED_RESOURCE_EXTENSIONS = new HashSet(Arrays.asList("js|css|jpeg|jpg|png|gif|html|htm".split("\\|")));
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  @RestrictedSince("2.37")
  public FormValidation doCheckURIEncoding(StaplerRequest request) throws IOException { return ((URICheckEncodingMonitor)ExtensionList.lookupSingleton(URICheckEncodingMonitor.class)).doCheckURIEncoding(request); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  @RestrictedSince("2.37")
  public static boolean isCheckURIEncodingEnabled() { return ((URICheckEncodingMonitor)ExtensionList.lookupSingleton(URICheckEncodingMonitor.class)).isCheckEnabled(); }
  
  public Future<DependencyGraph> getFutureDependencyGraph() {
    synchronized (this.dependencyGraphLock) {
      if (this.scheduledFutureDependencyGraph != null)
        return this.scheduledFutureDependencyGraph; 
      if (this.calculatingFutureDependencyGraph != null)
        return this.calculatingFutureDependencyGraph; 
      return CompletableFuture.completedFuture(this.dependencyGraph);
    } 
  }
  
  public void rebuildDependencyGraph() throws IOException {
    DependencyGraph graph = new DependencyGraph();
    graph.build();
    this.dependencyGraph = graph;
  }
  
  public Future<DependencyGraph> rebuildDependencyGraphAsync() {
    synchronized (this.dependencyGraphLock) {
      if (this.scheduledFutureDependencyGraph != null)
        return this.scheduledFutureDependencyGraph; 
      return this.scheduledFutureDependencyGraph = scheduleCalculationOfFutureDependencyGraph(500, TimeUnit.MILLISECONDS);
    } 
  }
  
  private Future<DependencyGraph> scheduleCalculationOfFutureDependencyGraph(int delay, TimeUnit unit) { return Timer.get().schedule(() -> {
          Future<DependencyGraph> temp = null;
          synchronized (this.dependencyGraphLock) {
            if (this.calculatingFutureDependencyGraph != null)
              temp = this.calculatingFutureDependencyGraph; 
          } 
          if (temp != null)
            temp.get(); 
          synchronized (this.dependencyGraphLock) {
            this.calculatingFutureDependencyGraph = this.scheduledFutureDependencyGraph;
            this.scheduledFutureDependencyGraph = null;
          } 
          rebuildDependencyGraph();
          synchronized (this.dependencyGraphLock) {
            this.calculatingFutureDependencyGraph = null;
          } 
          return this.dependencyGraph;
        }delay, unit); }
  
  public DependencyGraph getDependencyGraph() { return this.dependencyGraph; }
  
  public List<ManagementLink> getManagementLinks() { return ManagementLink.all(); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public Map<ManagementLink.Category, List<ManagementLink>> getCategorizedManagementLinks() {
    Map<ManagementLink.Category, List<ManagementLink>> byCategory = new TreeMap<ManagementLink.Category, List<ManagementLink>>();
    for (ManagementLink link : ManagementLink.all()) {
      if (link.getIconFileName() == null)
        continue; 
      if (!get().hasPermission(link.getRequiredPermission()))
        continue; 
      ((List)byCategory.computeIfAbsent(link.getCategory(), c -> new ArrayList())).add(link);
    } 
    return byCategory;
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public SetupWizard getSetupWizard() { return this.setupWizard; }
  
  public User getMe() {
    User u = User.current();
    if (u == null)
      throw new AccessDeniedException("/me is not available when not logged in"); 
    return u;
  }
  
  @StaplerDispatchable
  public List<Widget> getWidgets() { return this.widgets; }
  
  public Object getTarget() {
    try {
      checkPermission(READ);
    } catch (AccessDeniedException e) {
      if (!isSubjectToMandatoryReadPermissionCheck(Stapler.getCurrentRequest().getRestOfPath()))
        return this; 
      throw e;
    } 
    return this;
  }
  
  public boolean isSubjectToMandatoryReadPermissionCheck(String restOfPath) {
    for (String name : ALWAYS_READABLE_PATHS) {
      if (restOfPath.startsWith("/" + name + "/") || restOfPath.equals("/" + name))
        return false; 
    } 
    for (String name : getUnprotectedRootActions()) {
      if (restOfPath.startsWith("/" + name + "/") || restOfPath.equals("/" + name))
        return false; 
    } 
    if ((isAgentJnlpPath(restOfPath, "jenkins") || isAgentJnlpPath(restOfPath, "slave")) && "true".equals(Stapler.getCurrentRequest().getParameter("encrypt")))
      return false; 
    return true;
  }
  
  private boolean isAgentJnlpPath(String restOfPath, String prefix) {
    return restOfPath.matches("(/manage)?/computer/[^/]+/" + prefix + "-agent[.]jnlp");
  }
  
  public Collection<String> getUnprotectedRootActions() {
    Set<String> names = new TreeSet<String>();
    names.add("jnlpJars");
    for (Action a : getActions()) {
      if (a instanceof hudson.model.UnprotectedRootAction) {
        String url = a.getUrlName();
        if (url == null)
          continue; 
        names.add(url);
      } 
    } 
    return names;
  }
  
  public View getStaplerFallback() { return getPrimaryView(); }
  
  boolean isDisplayNameUnique(String displayName, String currentJobName) {
    Collection<TopLevelItem> itemCollection = this.items.values();
    for (TopLevelItem item : itemCollection) {
      if (item.getName().equals(currentJobName))
        continue; 
      if (displayName.equals(item.getDisplayName()))
        return false; 
    } 
    return true;
  }
  
  boolean isNameUnique(String name, String currentJobName) {
    TopLevelItem topLevelItem = getItem(name);
    if (null == topLevelItem)
      return true; 
    if (topLevelItem.getName().equals(currentJobName))
      return true; 
    return false;
  }
  
  public FormValidation doCheckDisplayName(@QueryParameter String displayName, @QueryParameter String jobName) {
    displayName = displayName.trim();
    LOGGER.fine(() -> "Current job name is " + jobName);
    if (!isNameUnique(displayName, jobName))
      return FormValidation.warning(Messages.Jenkins_CheckDisplayName_NameNotUniqueWarning(displayName)); 
    if (!isDisplayNameUnique(displayName, jobName))
      return FormValidation.warning(Messages.Jenkins_CheckDisplayName_DisplayNameNotUniqueWarning(displayName)); 
    return FormValidation.ok();
  }
  
  @CheckForNull
  public static <T> T lookup(Class<T> type) {
    Jenkins j = getInstanceOrNull();
    return (T)((j != null) ? j.lookup.get(type) : null);
  }
  
  @SuppressFBWarnings(value = {"MS_CANNOT_BE_FINAL"}, justification = "cannot be made immutable without breaking compatibility")
  public static List<LogRecord> logRecords = Collections.emptyList();
  
  public static final XStream XSTREAM;
  
  public static final XStream2 XSTREAM2;
  
  private static final int TWICE_CPU_NUM = Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
  
  final ExecutorService threadPoolForLoad;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String UNCOMPUTED_VERSION = "?";
  
  private static void computeVersion(ServletContext context) {
    Properties props = new Properties();
    try {
      InputStream is = Jenkins.class.getResourceAsStream("jenkins-version.properties");
      try {
        if (is != null)
          props.load(is); 
        if (is != null)
          is.close(); 
      } catch (Throwable throwable) {
        if (is != null)
          try {
            is.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (IOException e) {
      e.printStackTrace();
    } 
    String ver = props.getProperty("version");
    if (ver == null)
      ver = "?"; 
    if (Main.isDevelopmentMode && "${project.version}".equals(ver))
      try {
        File dir = (new File(".")).getAbsoluteFile();
        while (dir != null) {
          File pom = new File(dir, "pom.xml");
          if (pom.exists() && "pom".equals(XMLUtils.getValue("/project/artifactId", pom))) {
            pom = pom.getCanonicalFile();
            LOGGER.info("Reading version from: " + pom.getAbsolutePath());
            ver = XMLUtils.getValue("/project/version", pom);
            break;
          } 
          dir = dir.getParentFile();
        } 
        LOGGER.info("Jenkins is in dev mode, using version: " + ver);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, e, () -> "Unable to read Jenkins version: " + e.getMessage());
      }  
    VERSION = ver;
    context.setAttribute("version", ver);
    CHANGELOG_URL = props.getProperty("changelog.url");
    VERSION_HASH = Util.getDigestOf(ver).substring(0, 8);
    SESSION_HASH = Util.getDigestOf(ver + ver).substring(0, 8);
    if (ver.equals("?") || SystemProperties.getBoolean("hudson.script.noCache")) {
      RESOURCE_PATH = "";
    } else {
      RESOURCE_PATH = "/static/" + SESSION_HASH;
    } 
    VIEW_RESOURCE_PATH = "/resources/" + SESSION_HASH;
  }
  
  @SuppressFBWarnings(value = {"MS_CANNOT_BE_FINAL"}, justification = "cannot be made immutable without breaking compatibility")
  public static String VERSION = "?";
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static String CHANGELOG_URL;
  
  public static String VERSION_HASH;
  
  @SuppressFBWarnings(value = {"MS_CANNOT_BE_FINAL"}, justification = "cannot be made immutable without breaking compatibility")
  public static String SESSION_HASH;
  
  @CheckForNull
  public static VersionNumber getVersion() { return toVersion(VERSION); }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @CheckForNull
  public static VersionNumber getStoredVersion() { return toVersion((get()).version); }
  
  @CheckForNull
  private static VersionNumber toVersion(@CheckForNull String versionString) {
    if (versionString == null)
      return null; 
    try {
      return new VersionNumber(versionString);
    } catch (NumberFormatException e) {
      try {
        int idx = versionString.indexOf(' ');
        if (idx > 0)
          return new VersionNumber(versionString.substring(0, idx)); 
      } catch (NumberFormatException numberFormatException) {}
      return null;
    } catch (IllegalArgumentException e) {
      return null;
    } 
  }
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public boolean shouldShowStackTrace() {
    return Boolean.getBoolean(Jenkins.class.getName() + ".SHOW_STACK_TRACE");
  }
  
  @SuppressFBWarnings(value = {"MS_CANNOT_BE_FINAL"}, justification = "cannot be made immutable without breaking compatibility")
  public static String RESOURCE_PATH = "";
  
  @SuppressFBWarnings(value = {"MS_CANNOT_BE_FINAL"}, justification = "cannot be made immutable without breaking compatibility")
  public static String VIEW_RESOURCE_PATH = "/resources/TBD";
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean PARALLEL_LOAD = SystemProperties.getBoolean(Jenkins.class.getName() + ".parallelLoad", true);
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean KILL_AFTER_LOAD = SystemProperties.getBoolean(Jenkins.class.getName() + ".killAfterLoad", false);
  
  @Deprecated
  public static boolean FLYWEIGHT_SUPPORT = true;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @Deprecated
  public static boolean CONCURRENT_BUILD = true;
  
  private static final String WORKSPACE_DIRNAME = SystemProperties.getString(Jenkins.class.getName() + ".workspaceDirName", "workspace");
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final String NAME_VALIDATION_REJECTS_TRAILING_DOT_PROP = Jenkins.class.getName() + ".nameValidationRejectsTrailingDot";
  
  private static final String DEFAULT_BUILDS_DIR = "${ITEM_ROOTDIR}/builds";
  
  private static final String OLD_DEFAULT_WORKSPACES_DIR = "${ITEM_ROOTDIR}/" + WORKSPACE_DIRNAME;
  
  private static final String DEFAULT_WORKSPACES_DIR = "${JENKINS_HOME}/workspace/${ITEM_FULL_NAME}";
  
  static final String BUILDS_DIR_PROP = Jenkins.class.getName() + ".buildsDir";
  
  static final String WORKSPACES_DIR_PROP = Jenkins.class.getName() + ".workspacesDir";
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean AUTOMATIC_AGENT_LAUNCH = SystemProperties.getBoolean(Jenkins.class.getName() + ".automaticAgentLaunch", true);
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static int EXTEND_TIMEOUT_SECONDS = SystemProperties.getInteger(Jenkins.class.getName() + ".extendTimeoutSeconds", Integer.valueOf(15)).intValue();
  
  private static final Logger LOGGER = Logger.getLogger(Jenkins.class.getName());
  
  private static final SecureRandom RANDOM = new SecureRandom();
  
  public static final PermissionGroup PERMISSIONS = Permission.HUDSON_PERMISSIONS;
  
  public static final Permission ADMINISTER = Permission.HUDSON_ADMINISTER;
  
  @Restricted({org.kohsuke.accmod.restrictions.Beta.class})
  public static final Permission MANAGE = new Permission(PERMISSIONS, "Manage", 
      Messages._Jenkins_Manage_Description(), ADMINISTER, 
      
      SystemProperties.getBoolean("jenkins.security.ManagePermission"), new PermissionScope[] { PermissionScope.JENKINS });
  
  public static final Permission SYSTEM_READ = new Permission(PERMISSIONS, "SystemRead", 
      Messages._Jenkins_SystemRead_Description(), ADMINISTER, 
      
      SystemProperties.getBoolean("jenkins.security.SystemReadPermission"), new PermissionScope[] { PermissionScope.JENKINS });
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  public static final Permission[] MANAGE_AND_SYSTEM_READ = { MANAGE, SYSTEM_READ };
  
  public static final Permission READ = new Permission(PERMISSIONS, "Read", Messages._Hudson_ReadPermission_Description(), Permission.READ, PermissionScope.JENKINS);
  
  @Deprecated
  public static final Permission RUN_SCRIPTS = new Permission(PERMISSIONS, "RunScripts", Messages._Hudson_RunScriptsPermission_Description(), ADMINISTER, PermissionScope.JENKINS);
  
  private static final Set<String> ALWAYS_READABLE_PATHS = new HashSet(Arrays.asList(new String[] { 
          "404", "_404", "_404_simple", "login", "loginError", "logout", "accessDenied", "adjuncts", "error", "oops", 
          "signup", "tcpSlaveAgentListener", "federatedLoginService", "securityRealm" }));
  
  public static final Authentication ANONYMOUS2;
  
  @Deprecated
  public static final Authentication ANONYMOUS;
  
  static  {
    paths = SystemProperties.getString(Jenkins.class.getName() + ".additionalReadablePaths");
    if (paths != null) {
      LOGGER.info(() -> "SECURITY-2047 override: Adding the following paths to ALWAYS_READABLE_PATHS: " + paths);
      ALWAYS_READABLE_PATHS.addAll((Collection)Arrays.stream(paths.split(",")).map(String::trim).collect(Collectors.toSet()));
    } 
    ANONYMOUS2 = new AnonymousAuthenticationToken("anonymous", "anonymous", Set.of(new SimpleGrantedAuthority("anonymous")));
    ANONYMOUS = new AnonymousAuthenticationToken("anonymous", "anonymous", new GrantedAuthority[] { new GrantedAuthorityImpl("anonymous") });
    try {
      XSTREAM = XSTREAM2 = new XStream2();
      XSTREAM.alias("jenkins", Jenkins.class);
      XSTREAM.alias("slave", hudson.slaves.DumbSlave.class);
      XSTREAM.alias("jdk", JDK.class);
      XSTREAM.alias("view", hudson.model.ListView.class);
      XSTREAM.alias("listView", hudson.model.ListView.class);
      XSTREAM.addImplicitArray(Jenkins.class, "_disabledAgentProtocols", "disabledAgentProtocol");
      XSTREAM.addImplicitArray(Jenkins.class, "_enabledAgentProtocols", "enabledAgentProtocol");
      XSTREAM2.addCriticalField(Jenkins.class, "securityRealm");
      XSTREAM2.addCriticalField(Jenkins.class, "authorizationStrategy");
      Node.Mode.class.getEnumConstants();
      assert PERMISSIONS != null;
      assert ADMINISTER != null;
    } catch (RuntimeException|Error paths) {
      LOGGER.log(Level.SEVERE, "Failed to load Jenkins.class", paths);
      throw paths;
    } 
  }
}
