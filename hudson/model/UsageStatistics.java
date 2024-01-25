package hudson.model;

import com.jcraft.jzlib.GZIPOutputStream;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.PluginWrapper;
import hudson.Util;
import hudson.node_monitors.ArchitectureMonitor;
import hudson.security.Permission;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import jenkins.model.Jenkins;
import jenkins.util.SystemProperties;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class UsageStatistics extends PageDecorator implements PersistentDescriptor {
  private static final Logger LOG = Logger.getLogger(UsageStatistics.class.getName());
  
  private final String keyImage;
  
  private static final String DEFAULT_KEY_BYTES = "30819f300d06092a864886f70d010101050003818d0030818902818100c14970473bd90fd1f2d20e4fa6e36ea21f7d46db2f4104a3a8f2eb097d6e26278dfadf3fe9ed05bbbb00a4433f4b7151e6683a169182e6ff2f6b4f2bb6490b2cddef73148c37a2a7421fc75f99fb0fadab46f191806599a208652f4829fd6f76e13195fb81ff3f2fce15a8e9a85ebe15c07c90b34ebdb416bd119f0d74105f3b0203010001";
  
  public UsageStatistics() { this("30819f300d06092a864886f70d010101050003818d0030818902818100c14970473bd90fd1f2d20e4fa6e36ea21f7d46db2f4104a3a8f2eb097d6e26278dfadf3fe9ed05bbbb00a4433f4b7151e6683a169182e6ff2f6b4f2bb6490b2cddef73148c37a2a7421fc75f99fb0fadab46f191806599a208652f4829fd6f76e13195fb81ff3f2fce15a8e9a85ebe15c07c90b34ebdb416bd119f0d74105f3b0203010001"); }
  
  public UsageStatistics(String keyImage) {
    this.lastAttempt = -1L;
    this.keyImage = keyImage;
  }
  
  public boolean isDue() {
    if (!Jenkins.get().isUsageStatisticsCollected() || DISABLED)
      return false; 
    long now = System.currentTimeMillis();
    if (now - this.lastAttempt > DAY) {
      this.lastAttempt = now;
      return true;
    } 
    return false;
  }
  
  private RSAPublicKey getKey() {
    try {
      if (this.key == null) {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.key = (RSAPublicKey)keyFactory.generatePublic(new X509EncodedKeySpec(Util.fromHexString(this.keyImage)));
      } 
      return this.key;
    } catch (GeneralSecurityException e) {
      throw new Error(e);
    } 
  }
  
  public String getStatData() throws IOException {
    Jenkins j = Jenkins.get();
    JSONObject o = new JSONObject();
    o.put("stat", Integer.valueOf(1));
    o.put("install", j.getLegacyInstanceId());
    o.put("servletContainer", j.servletContext.getServerInfo());
    o.put("version", Jenkins.VERSION);
    List<JSONObject> nodes = new ArrayList<JSONObject>();
    for (Computer c : j.getComputers()) {
      JSONObject n = new JSONObject();
      if (c.getNode() == j) {
        n.put("master", Boolean.valueOf(true));
        n.put("jvm-vendor", System.getProperty("java.vm.vendor"));
        n.put("jvm-name", System.getProperty("java.vm.name"));
        n.put("jvm-version", System.getProperty("java.version"));
      } 
      n.put("executors", Integer.valueOf(c.getNumExecutors()));
      ArchitectureMonitor.DescriptorImpl descriptor = (ArchitectureMonitor.DescriptorImpl)j.getDescriptorByType(ArchitectureMonitor.DescriptorImpl.class);
      n.put("os", descriptor.get(c));
      nodes.add(n);
    } 
    o.put("nodes", nodes);
    List<JSONObject> plugins = new ArrayList<JSONObject>();
    for (PluginWrapper pw : j.getPluginManager().getPlugins()) {
      if (!pw.isActive())
        continue; 
      JSONObject p = new JSONObject();
      p.put("name", pw.getShortName());
      p.put("version", pw.getVersion());
      plugins.add(p);
    } 
    o.put("plugins", plugins);
    JSONObject jobs = new JSONObject();
    TopLevelItemDescriptor[] descriptors = (TopLevelItemDescriptor[])Items.all().toArray(new TopLevelItemDescriptor[0]);
    int[] counts = new int[descriptors.length];
    for (TopLevelItem item : j.allItems(TopLevelItem.class)) {
      TopLevelItemDescriptor d = item.getDescriptor();
      for (int i = 0; i < descriptors.length; i++) {
        if (d == descriptors[i]) {
          counts[i] = counts[i] + 1;
          break;
        } 
      } 
    } 
    for (i = 0; i < descriptors.length; i++)
      jobs.put(descriptors[i].getJsonSafeClassName(), Integer.valueOf(counts[i])); 
    o.put("jobs", jobs);
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      CombinedCipherOutputStream combinedCipherOutputStream = new CombinedCipherOutputStream(baos, getKey(), "AES");
      try {
        GZIPOutputStream gZIPOutputStream = new GZIPOutputStream(combinedCipherOutputStream);
        try {
          OutputStreamWriter w = new OutputStreamWriter(gZIPOutputStream, StandardCharsets.UTF_8);
          try {
            o.write(w);
            w.close();
          } catch (Throwable throwable) {
            try {
              w.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            } 
            throw throwable;
          } 
          gZIPOutputStream.close();
        } catch (Throwable throwable) {
          try {
            gZIPOutputStream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
        combinedCipherOutputStream.close();
      } catch (Throwable throwable) {
        try {
          combinedCipherOutputStream.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
      return Base64.getEncoder().encodeToString(baos.toByteArray());
    } catch (Throwable i) {
      Throwable e;
      LOG.log(Level.INFO, "Usage statistics could not be sent ({0})", e.getMessage());
      LOG.log(Level.FINE, "Error sending usage statistics", e);
      return null;
    } 
  }
  
  @NonNull
  public Permission getRequiredGlobalConfigPagePermission() { return Jenkins.MANAGE; }
  
  public boolean configure(StaplerRequest req, JSONObject json) throws Descriptor.FormException {
    try {
      Jenkins.get().setNoUsageStatistics(json.has("usageStatisticsCollected") ? null : Boolean.valueOf(true));
      return true;
    } catch (IOException e) {
      throw new Descriptor.FormException(e, "usageStatisticsCollected");
    } 
  }
  
  private static String getKeyAlgorithm(String algorithm) {
    int index = algorithm.indexOf('/');
    return (index > 0) ? algorithm.substring(0, index) : algorithm;
  }
  
  private static Cipher toCipher(RSAKey key, int mode) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(mode, (Key)key);
    return cipher;
  }
  
  private static final long DAY = TimeUnit.DAYS.toMillis(1L);
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for script console")
  public static boolean DISABLED = SystemProperties.getBoolean(UsageStatistics.class.getName() + ".disabled");
}
