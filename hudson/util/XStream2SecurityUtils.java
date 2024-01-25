package hudson.util;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.security.InputManipulationException;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class XStream2SecurityUtils {
  public static void checkForCollectionDoSAttack(UnmarshallingContext context, long startNano) {
    int diff = (int)((System.nanoTime() - startNano) / 1000000000L);
    if (diff > 0) {
      Integer secondsUsed = (Integer)context.get("XStreamCollectionUpdateSeconds");
      if (secondsUsed != null) {
        Integer limit = (Integer)context.get("XStreamCollectionUpdateLimit");
        if (limit == null)
          throw new ConversionException("Missing limit for updating collections."); 
        int seconds = secondsUsed.intValue() + diff;
        if (seconds > limit.intValue())
          throw new InputManipulationException("Denial of Service attack assumed. Adding elements to collections or maps exceeds " + limit + " seconds."); 
        context.put("XStreamCollectionUpdateSeconds", Integer.valueOf(seconds));
      } 
    } 
  }
}
