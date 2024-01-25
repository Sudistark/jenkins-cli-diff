package hudson.util.xstream;

import com.google.common.collect.ImmutableMap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import java.util.HashMap;
import java.util.Map;

public class ImmutableMapConverter extends MapConverter {
  private final SerializableConverter sc;
  
  public ImmutableMapConverter(XStream xs) { this(xs.getMapper(), xs.getReflectionProvider()); }
  
  public ImmutableMapConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
    super(mapper);
    this.sc = new SerializableConverter(mapper, reflectionProvider);
  }
  
  public boolean canConvert(Class type) { return ImmutableMap.class.isAssignableFrom(type); }
  
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) { return ImmutableMap.copyOf((Map)super.unmarshal(reader, context)); }
  
  protected Object createCollection(Class type) { return new HashMap(); }
}
