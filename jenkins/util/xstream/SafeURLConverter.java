package jenkins.util.xstream;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.basic.URLConverter;
import hudson.remoting.URLDeserializationHelper;
import java.io.IOException;
import java.net.URL;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class SafeURLConverter extends URLConverter {
  public Object fromString(String str) {
    URL url = (URL)super.fromString(str);
    try {
      return URLDeserializationHelper.wrapIfRequired(url);
    } catch (IOException e) {
      throw new ConversionException(e);
    } 
  }
}
