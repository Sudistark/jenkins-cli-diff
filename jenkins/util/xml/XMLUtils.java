package jenkins.util.xml;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import jenkins.util.SystemProperties;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public final class XMLUtils {
  private static final Logger LOGGER = LogManager.getLogManager().getLogger(XMLUtils.class.getName());
  
  private static final String DISABLED_PROPERTY_NAME = XMLUtils.class.getName() + ".disableXXEPrevention";
  
  private static final String FEATURE_HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
  
  private static final String FEATURE_HTTP_XML_ORG_SAX_FEATURES_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
  
  public static void safeTransform(@NonNull Source source, @NonNull Result out) throws TransformerException, SAXException {
    InputSource src = SAXSource.sourceToInputSource(source);
    if (src != null) {
      SAXTransformerFactory stFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
      stFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      try {
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
      } catch (SAXException sAXException) {}
      try {
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      } catch (SAXException sAXException) {}
      xmlReader.setEntityResolver(RestrictiveEntityResolver.INSTANCE);
      SAXSource saxSource = new SAXSource(xmlReader, src);
      _transform(saxSource, out);
    } else if (SystemProperties.getBoolean(DISABLED_PROPERTY_NAME)) {
      LOGGER.log(Level.WARNING, "XML external entity (XXE) prevention has been disabled by the system property {0}=true Your system may be vulnerable to XXE attacks.", DISABLED_PROPERTY_NAME);
      if (LOGGER.isLoggable(Level.FINE))
        LOGGER.log(Level.FINE, "Caller stack trace: ", new Exception("XXE Prevention caller history")); 
      _transform(source, out);
    } else {
      throw new TransformerException("Could not convert source of type " + source.getClass() + " and XXEPrevention is enabled.");
    } 
  }
  
  @SuppressFBWarnings(value = {"XXE_DOCUMENT"}, justification = "newDocumentBuilderFactory() does what FindSecBugs recommends, yet FindSecBugs cannot see this")
  @NonNull
  public static Document parse(@NonNull InputStream stream) throws SAXException, IOException {
    DocumentBuilder docBuilder;
    try {
      docBuilder = newDocumentBuilderFactory().newDocumentBuilder();
      docBuilder.setEntityResolver(RestrictiveEntityResolver.INSTANCE);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Unexpected error creating DocumentBuilder.", e);
    } 
    return docBuilder.parse(new InputSource(stream));
  }
  
  @SuppressFBWarnings(value = {"XXE_DOCUMENT"}, justification = "newDocumentBuilderFactory() does what FindSecBugs recommends, yet FindSecBugs cannot see this")
  @NonNull
  public static Document parse(@NonNull Reader stream) throws SAXException, IOException {
    DocumentBuilder docBuilder;
    try {
      docBuilder = newDocumentBuilderFactory().newDocumentBuilder();
      docBuilder.setEntityResolver(RestrictiveEntityResolver.INSTANCE);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException("Unexpected error creating DocumentBuilder.", e);
    } 
    return docBuilder.parse(new InputSource(stream));
  }
  
  @NonNull
  public static Document parse(@NonNull File file) throws SAXException, IOException {
    if (!file.exists() || !file.isFile())
      throw new IllegalArgumentException(String.format("File %s does not exist or is not a 'normal' file.", new Object[] { file.getAbsolutePath() })); 
    try {
      InputStream fileInputStream = Files.newInputStream(file.toPath(), new java.nio.file.OpenOption[0]);
      try {
        Document document = parse(fileInputStream);
        if (fileInputStream != null)
          fileInputStream.close(); 
        return document;
      } catch (Throwable throwable) {
        if (fileInputStream != null)
          try {
            fileInputStream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  @Deprecated
  @NonNull
  public static Document parse(@NonNull File file, @NonNull String encoding) throws SAXException, IOException {
    if (!file.exists() || !file.isFile())
      throw new IllegalArgumentException(String.format("File %s does not exist or is not a 'normal' file.", new Object[] { file.getAbsolutePath() })); 
    try {
      InputStream fileInputStream = Files.newInputStream(file.toPath(), new java.nio.file.OpenOption[0]);
      try {
        InputStreamReader fileReader = new InputStreamReader(fileInputStream, encoding);
        try {
          Document document = parse(fileReader);
          fileReader.close();
          if (fileInputStream != null)
            fileInputStream.close(); 
          return document;
        } catch (Throwable throwable) {
          try {
            fileReader.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          } 
          throw throwable;
        } 
      } catch (Throwable throwable) {
        if (fileInputStream != null)
          try {
            fileInputStream.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (InvalidPathException e) {
      throw new IOException(e);
    } 
  }
  
  @NonNull
  public static String getValue(@NonNull String xpath, @NonNull File file) throws IOException, SAXException, XPathExpressionException { return getValue(xpath, file, Charset.defaultCharset().toString()); }
  
  @NonNull
  public static String getValue(@NonNull String xpath, @NonNull File file, @NonNull String fileDataEncoding) throws IOException, SAXException, XPathExpressionException {
    Document document = parse(file, fileDataEncoding);
    return getValue(xpath, document);
  }
  
  public static String getValue(String xpath, Document document) throws XPathExpressionException {
    XPath xPathProcessor = XPathFactory.newInstance().newXPath();
    return xPathProcessor.compile(xpath).evaluate(document);
  }
  
  private static void _transform(Source source, Result out) throws TransformerException, SAXException {
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
    Transformer t = factory.newTransformer();
    t.transform(source, out);
  }
  
  private static DocumentBuilderFactory newDocumentBuilderFactory() {
    documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setXIncludeAware(false);
    documentBuilderFactory.setExpandEntityReferences(false);
    setDocumentBuilderFactoryFeature(documentBuilderFactory, "http://javax.xml.XMLConstants/feature/secure-processing", true);
    setDocumentBuilderFactoryFeature(documentBuilderFactory, "http://xml.org/sax/features/external-general-entities", false);
    setDocumentBuilderFactoryFeature(documentBuilderFactory, "http://xml.org/sax/features/external-parameter-entities", false);
    setDocumentBuilderFactoryFeature(documentBuilderFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);
    return documentBuilderFactory;
  }
  
  private static void setDocumentBuilderFactoryFeature(DocumentBuilderFactory documentBuilderFactory, String feature, boolean state) {
    try {
      documentBuilderFactory.setFeature(feature, state);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, String.format("Failed to set the XML Document Builder factory feature %s to %s", new Object[] { feature, Boolean.valueOf(state) }), e);
    } 
  }
}
