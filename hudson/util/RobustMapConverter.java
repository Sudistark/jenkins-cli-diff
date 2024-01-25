package hudson.util;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.security.InputManipulationException;
import java.util.Map;
import java.util.logging.Logger;
import jenkins.util.xstream.CriticalXStreamException;

final class RobustMapConverter extends MapConverter {
  private static final Object ERROR = new Object();
  
  RobustMapConverter(Mapper mapper) { super(mapper); }
  
  protected void putCurrentEntryIntoMap(HierarchicalStreamReader reader, UnmarshallingContext context, Map map, Map target) {
    Object key = read(reader, context, map);
    Object value = read(reader, context, map);
    if (key != ERROR && value != ERROR)
      try {
        long nanoNow = System.nanoTime();
        target.put(key, value);
        XStream2SecurityUtils.checkForCollectionDoSAttack(context, nanoNow);
      } catch (InputManipulationException e) {
        Logger.getLogger(RobustMapConverter.class.getName()).warning("DoS detected and prevented. If the heuristic was too aggressive, you can customize the behavior by setting the hudson.util.XStream2.collectionUpdateLimit system property. See https://www.jenkins.io/redirect/xstream-dos-prevention for more information.");
        throw new CriticalXStreamException(e);
      }  
  }
  
  private Object read(HierarchicalStreamReader reader, UnmarshallingContext context, Map map) {
    reader.moveDown();
    try {
      return readBareItem(reader, context, map);
    } catch (CriticalXStreamException x) {
      throw x;
    } catch (XStreamException|LinkageError x) {
      RobustReflectionConverter.addErrorInContext(context, x);
      return ERROR;
    } finally {
      reader.moveUp();
    } 
  }
}
