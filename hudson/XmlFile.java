package hudson;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import hudson.util.AtomicFileWriter;
import hudson.util.XStream2;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class XmlFile {
  private final XStream xs;
  
  private final File file;
  
  private final boolean force;
  
  private static final Map<Object, Void> beingWritten = Collections.synchronizedMap(new IdentityHashMap());
  
  private static final ThreadLocal<File> writing = new ThreadLocal();
  
  public XmlFile(File file) { this(DEFAULT_XSTREAM, file); }
  
  public XmlFile(XStream xs, File file) { this(xs, file, true); }
  
  public XmlFile(XStream xs, File file, boolean force) {
    this.xs = xs;
    this.file = file;
    this.force = force;
  }
  
  public File getFile() { return this.file; }
  
  public XStream getXStream() { return this.xs; }
  
  public Object read() throws IOException {
    if (LOGGER.isLoggable(Level.FINE))
      LOGGER.fine("Reading " + this.file); 
    try {
      InputStream in = new BufferedInputStream(Files.newInputStream(this.file.toPath(), new java.nio.file.OpenOption[0]));
      try {
        Object object = this.xs.fromXML(in);
        in.close();
        return object;
      } catch (Throwable throwable) {
        try {
          in.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (RuntimeException|Error e) {
      throw new IOException("Unable to read " + this.file, e);
    } 
  }
  
  public Object unmarshal(Object o) throws IOException { return unmarshal(o, false); }
  
  public Object unmarshalNullingOut(Object o) throws IOException { return unmarshal(o, true); }
  
  private Object unmarshal(Object o, boolean nullOut) throws IOException {
    try {
      InputStream in = new BufferedInputStream(Files.newInputStream(this.file.toPath(), new java.nio.file.OpenOption[0]));
      try {
        if (nullOut) {
          Object object1 = ((XStream2)this.xs).unmarshal(DEFAULT_DRIVER.createReader(in), o, null, true);
          in.close();
          return object1;
        } 
        Object object = this.xs.unmarshal(DEFAULT_DRIVER.createReader(in), o);
        in.close();
        return object;
      } catch (Throwable throwable) {
        try {
          in.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        } 
        throw throwable;
      } 
    } catch (RuntimeException|Error e) {
      throw new IOException("Unable to read " + this.file, e);
    } 
  }
  
  public void write(Object o) throws IOException {
    if (LOGGER.isLoggable(Level.FINEST))
      LOGGER.log(Level.FINEST, new Throwable(), () -> "Writing " + this.file); 
    mkdirs();
    w = this.force ? new AtomicFileWriter(this.file) : new AtomicFileWriter(this.file.toPath(), StandardCharsets.UTF_8, false, false);
    try {
      w.write("<?xml version='1.1' encoding='UTF-8'?>\n");
      beingWritten.put(o, null);
      writing.set(this.file);
      try {
        this.xs.toXML(o, w);
      } finally {
        beingWritten.remove(o);
        writing.set(null);
      } 
      w.commit();
    } catch (RuntimeException e) {
      throw new IOException(e);
    } finally {
      w.abort();
    } 
  }
  
  public static Object replaceIfNotAtTopLevel(Object o, Supplier<Object> replacement) {
    File currentlyWriting = (File)writing.get();
    if (beingWritten.containsKey(o) || currentlyWriting == null)
      return o; 
    LOGGER.log(Level.WARNING, "JENKINS-45892: reference to " + o + " being saved from unexpected " + currentlyWriting, new IllegalStateException());
    return replacement.get();
  }
  
  public boolean exists() { return this.file.exists(); }
  
  public void delete() throws IOException { Files.deleteIfExists(Util.fileToPath(this.file)); }
  
  public void mkdirs() throws IOException { Util.createDirectories(Util.fileToPath(this.file.getParentFile()), new java.nio.file.attribute.FileAttribute[0]); }
  
  public String toString() { return this.file.toString(); }
  
  public Reader readRaw() throws IOException {
    try {
      InputStream fileInputStream = Files.newInputStream(this.file.toPath(), new java.nio.file.OpenOption[0]);
      try {
        return new InputStreamReader(fileInputStream, sniffEncoding());
      } catch (IOException ex) {
        Util.closeAndLogFailures(fileInputStream, LOGGER, "FileInputStream", this.file.toString());
        throw ex;
      } 
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  public String asString() {
    StringWriter w = new StringWriter();
    writeRawTo(w);
    return w.toString();
  }
  
  public void writeRawTo(Writer w) throws IOException {
    Reader r = readRaw();
    try {
      IOUtils.copy(r, w);
      if (r != null)
        r.close(); 
    } catch (Throwable throwable) {
      if (r != null)
        try {
          r.close();
        } catch (Throwable throwable1) {
          throwable.addSuppressed(throwable1);
        }  
      throw throwable;
    } 
  }
  
  public String sniffEncoding() {
    try {
      InputStream in = Files.newInputStream(this.file.toPath(), new java.nio.file.OpenOption[0]);
      try {
        InputSource input = new InputSource(this.file.toURI().toASCIIString());
        input.setByteStream(in);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        spf.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
        spf.setNamespaceAware(true);
        spf.newSAXParser().parse(input, new Object(this));
        throw new AssertionError();
      } catch (Throwable throwable) {
        if (in != null)
          try {
            in.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (Eureka e) {
      if (e.encoding != null)
        return e.encoding; 
      return "UTF-8";
    } catch (SAXException e) {
      throw new IOException("Failed to detect encoding of " + this.file, e);
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } catch (ParserConfigurationException e) {
      throw new AssertionError(e);
    } 
  }
  
  private static final Logger LOGGER = Logger.getLogger(XmlFile.class.getName());
  
  private static final HierarchicalStreamDriver DEFAULT_DRIVER = XStream2.getDefaultDriver();
  
  private static final XStream DEFAULT_XSTREAM = new XStream2(DEFAULT_DRIVER);
}
