package hudson;

import java.io.IOException;
import java.net.URLConnection;

public abstract class URLConnectionDecorator implements ExtensionPoint {
  public abstract void decorate(URLConnection paramURLConnection) throws IOException;
  
  public static ExtensionList<URLConnectionDecorator> all() { return ExtensionList.lookup(URLConnectionDecorator.class); }
}
