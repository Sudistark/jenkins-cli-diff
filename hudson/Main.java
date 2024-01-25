package hudson;

import com.thoughtworks.xstream.core.util.Base64Encoder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.DualOutputStream;
import hudson.util.EncodingStream;
import hudson.util.IOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jenkins.util.SystemProperties;
import org.apache.commons.io.IOUtils;

public class Main {
  public static void main(String[] args) {
    try {
      System.exit(run(args));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    } 
  }
  
  public static int run(String[] args) throws Exception {
    String home = getHudsonHome();
    if (home == null) {
      System.err.println("JENKINS_HOME is not set.");
      return -1;
    } 
    if (args.length < 2) {
      System.err.println("Usage: <job-name> <command> <args..>");
      return -1;
    } 
    return remotePost(args);
  }
  
  private static String getHudsonHome() {
    home = (String)EnvVars.masterEnvVars.get("JENKINS_HOME");
    if (home != null)
      return home; 
    return (String)EnvVars.masterEnvVars.get("HUDSON_HOME");
  }
  
  public static int remotePost(String[] args) throws Exception {
    String projectName = args[0];
    String home = getHudsonHome();
    if (!home.endsWith("/"))
      home = home + "/"; 
    String auth = (new URL(home)).getUserInfo();
    if (auth != null)
      auth = "Basic " + (new Base64Encoder()).encode(auth.getBytes(StandardCharsets.UTF_8)); 
    HttpURLConnection con = open(new URL(home));
    if (auth != null)
      con.setRequestProperty("Authorization", auth); 
    con.connect();
    if (con.getResponseCode() != 200 || con
      .getHeaderField("X-Hudson") == null) {
      System.err.println(home + " is not Hudson (" + home + ")");
      return -1;
    } 
    URL jobURL = new URL(home + "job/" + home + "/");
    HttpURLConnection con = open(new URL(jobURL, "acceptBuildResult"));
    if (auth != null)
      con.setRequestProperty("Authorization", auth); 
    con.connect();
    if (con.getResponseCode() != 200) {
      System.err.println("" + jobURL + " is not a valid external job (" + jobURL + " " + con.getResponseCode() + ")");
      return -1;
    } 
    String crumbField = null, crumbValue = null;
    try {
      HttpURLConnection con = open(new URL(home + "crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)'"));
      if (auth != null)
        con.setRequestProperty("Authorization", auth); 
      String line = IOUtils.readFirstLine(con.getInputStream(), "UTF-8");
      String[] components = line.split(":");
      if (components.length == 2) {
        crumbField = components[0];
        crumbValue = components[1];
      } 
    } catch (IOException iOException) {}
    tmpFile = File.createTempFile("jenkins", "log");
    try {
      int ret;
      try {
        OutputStream os = Files.newOutputStream(tmpFile.toPath(), new java.nio.file.OpenOption[0]);
        try {
          e = new OutputStreamWriter(os, StandardCharsets.UTF_8);
          try {
            e.write("<?xml version='1.1' encoding='UTF-8'?>");
            e.write("<run><log encoding='hexBinary' content-encoding='" + Charset.defaultCharset().name() + "'>");
            e.flush();
            long start = System.currentTimeMillis();
            List<String> cmd = new ArrayList<String>(Arrays.asList(args).subList(1, args.length));
            Proc.LocalProc localProc = new Proc.LocalProc((String[])cmd.toArray(new String[0]), (String[])null, System.in, new DualOutputStream(System.out, new EncodingStream(os)));
            ret = localProc.join();
            e.write("</log><result>" + ret + "</result><duration>" + System.currentTimeMillis() - start + "</duration></run>");
            e.close();
          } catch (Throwable throwable) {
            try {
              e.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            } 
            throw throwable;
          } 
          if (os != null)
            os.close(); 
        } catch (Throwable null) {
          if (os != null)
            try {
              os.close();
            } catch (Throwable throwable) {
              e.addSuppressed(throwable);
            }  
          throw e;
        } 
      } catch (InvalidPathException e) {
        throw new IOException(e);
      } 
      URL location = new URL(jobURL, "postBuildResult");
      while (true) {
        try {
          e = open(location);
          if (auth != null)
            e.setRequestProperty("Authorization", auth); 
          if (crumbField != null && crumbValue != null)
            e.setRequestProperty(crumbField, crumbValue); 
          e.setDoOutput(true);
          e.setFixedLengthStreamingMode((int)tmpFile.length());
          e.connect();
          try {
            InputStream in = Files.newInputStream(tmpFile.toPath(), new java.nio.file.OpenOption[0]);
            try {
              IOUtils.copy(in, e.getOutputStream());
              if (in != null)
                in.close(); 
            } catch (Throwable throwable) {
              if (in != null)
                try {
                  in.close();
                } catch (Throwable throwable1) {
                  throwable.addSuppressed(throwable1);
                }  
              throw throwable;
            } 
          } catch (InvalidPathException e) {
            throw new IOException(e);
          } 
          if (e.getResponseCode() != 200)
            IOUtils.copy(e.getErrorStream(), System.err); 
          return ret;
        } catch (HttpRetryException e) {
          if (e.getLocation() != null) {
            location = new URL(e.getLocation());
            continue;
          } 
          break;
        } 
      } 
      throw e;
    } finally {
      Files.delete(Util.fileToPath(tmpFile));
    } 
  }
  
  private static HttpURLConnection open(URL url) throws IOException {
    HttpURLConnection c = (HttpURLConnection)url.openConnection();
    c.setReadTimeout(TIMEOUT);
    c.setConnectTimeout(TIMEOUT);
    return c;
  }
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for debugging")
  public static boolean isUnitTest = false;
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "for debugging")
  public static boolean isDevelopmentMode = SystemProperties.getBoolean(Main.class.getName() + ".development");
  
  public static final int TIMEOUT = SystemProperties.getInteger(Main.class.getName() + ".timeout", Integer.valueOf(15000)).intValue();
}
