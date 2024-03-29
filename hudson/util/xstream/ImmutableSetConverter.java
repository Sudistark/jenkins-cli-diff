package hudson.util.xstream;

import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import java.util.ArrayList;
import java.util.List;

public class ImmutableSetConverter extends CollectionConverter {
  private final SerializableConverter sc;
  
  public ImmutableSetConverter(XStream xs) { this(xs.getMapper(), xs.getReflectionProvider()); }
  
  public ImmutableSetConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
    super(mapper);
    this.sc = new SerializableConverter(mapper, reflectionProvider);
  }
  
  public boolean canConvert(Class type) { return ImmutableSet.class.isAssignableFrom(type); }
  
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) { return ImmutableSet.copyOf((List)super.unmarshal(reader, context)); }
  
  protected Object createCollection(Class type) { return new ArrayList(); }
}
