package hudson.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.core.ClassLoaderReference;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.security.InputManipulationException;
import java.util.Collection;
import java.util.logging.Logger;
import jenkins.util.xstream.CriticalXStreamException;

public class RobustCollectionConverter extends CollectionConverter {
  private final SerializableConverter sc;
  
  public RobustCollectionConverter(XStream xs) { this(xs.getMapper(), xs.getReflectionProvider()); }
  
  public RobustCollectionConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
    super(mapper);
    this.sc = new SerializableConverter(mapper, reflectionProvider, new ClassLoaderReference(null));
  }
  
  public boolean canConvert(Class type) { return (super.canConvert(type) || type == java.util.concurrent.CopyOnWriteArrayList.class || type == java.util.concurrent.CopyOnWriteArraySet.class); }
  
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String s = reader.getAttribute("serialization");
    if (s != null && s.equals("custom"))
      return this.sc.unmarshal(reader, context); 
    return super.unmarshal(reader, context);
  }
  
  protected void populateCollection(HierarchicalStreamReader reader, UnmarshallingContext context, Collection collection) {
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      try {
        Object item = readBareItem(reader, context, collection);
        long nanoNow = System.nanoTime();
        collection.add(item);
        XStream2SecurityUtils.checkForCollectionDoSAttack(context, nanoNow);
      } catch (CriticalXStreamException e) {
        throw e;
      } catch (InputManipulationException e) {
        Logger.getLogger(RobustCollectionConverter.class.getName()).warning("DoS detected and prevented. If the heuristic was too aggressive, you can customize the behavior by setting the hudson.util.XStream2.collectionUpdateLimit system property. See https://www.jenkins.io/redirect/xstream-dos-prevention for more information.");
        throw new CriticalXStreamException(e);
      } catch (XStreamException|LinkageError e) {
        RobustReflectionConverter.addErrorInContext(context, e);
      } 
      reader.moveUp();
    } 
  }
}
