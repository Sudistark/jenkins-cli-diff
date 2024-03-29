package hudson.util.xstream;

import com.google.common.collect.ImmutableSortedSet;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.SerializableConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;
import java.util.ArrayList;
import java.util.List;

public class ImmutableSortedSetConverter extends CollectionConverter {
  private final SerializableConverter sc;
  
  public ImmutableSortedSetConverter(XStream xs) { this(xs.getMapper(), xs.getReflectionProvider()); }
  
  public ImmutableSortedSetConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
    super(mapper);
    this.sc = new SerializableConverter(mapper, reflectionProvider);
  }
  
  public boolean canConvert(Class type) { return ImmutableSortedSet.class.isAssignableFrom(type); }
  
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) { return ImmutableSortedSet.copyOf((List)super.unmarshal(reader, context)); }
  
  protected Object createCollection(Class type) { return new ArrayList(); }
}
