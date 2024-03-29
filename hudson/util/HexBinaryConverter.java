package hudson.util;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import hudson.Util;

public class HexBinaryConverter implements Converter {
  public boolean canConvert(Class type) { return (type == byte[].class); }
  
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    byte[] data = (byte[])source;
    writer.setValue(Util.toHexString(data));
  }
  
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    String data = reader.getValue();
    return Util.fromHexString(data);
  }
}
