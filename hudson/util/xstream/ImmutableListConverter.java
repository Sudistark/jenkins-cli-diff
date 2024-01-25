package hudson.util.xstream;

import com.google.common.collect.ImmutableList;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import hudson.util.RobustReflectionConverter;
import java.util.ArrayList;
import java.util.List;
import jenkins.util.xstream.CriticalXStreamException;

public class ImmutableListConverter extends CollectionConverter {
  private final SerializableConverter sc;
  
  public ImmutableListConverter(XStream xs) { this(xs.getMapper(), xs.getReflectionProvider()); }
  
  public ImmutableListConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
    super(mapper);
    this.sc = new SerializableConverter(mapper, reflectionProvider);
  }
  
  public boolean canConvert(Class type) { return ImmutableList.class.isAssignableFrom(type); }
  
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String resolvesTo = reader.getAttribute("resolves-to");
    if ("com.google.common.collect.ImmutableList$SerializedForm".equals(resolvesTo)) {
      List items = new ArrayList();
      if (reader.hasMoreChildren()) {
        reader.moveDown();
        while (reader.hasMoreChildren()) {
          reader.moveDown();
          try {
            Object item = readItem(reader, context, items);
            items.add(item);
          } catch (CriticalXStreamException e) {
            throw e;
          } catch (XStreamException|LinkageError e) {
            RobustReflectionConverter.addErrorInContext(context, e);
          } 
          reader.moveUp();
        } 
        reader.moveUp();
      } 
      return ImmutableList.copyOf(items);
    } 
    return ImmutableList.copyOf((List)super.unmarshal(reader, context));
  }
  
  protected Object createCollection(Class type) { return new ArrayList(); }
}
