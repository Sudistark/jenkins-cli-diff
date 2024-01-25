package jenkins.util.xml;

import java.io.IOException;
import org.kohsuke.accmod.Restricted;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public final class RestrictiveEntityResolver implements EntityResolver {
  public static final RestrictiveEntityResolver INSTANCE = new RestrictiveEntityResolver();
  
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    throw new SAXException("Refusing to resolve entity with publicId(" + publicId + ") and systemId (" + systemId + ")");
  }
}
