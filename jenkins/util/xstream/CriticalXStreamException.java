package jenkins.util.xstream;

import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.ConversionException;

public class CriticalXStreamException extends ConversionException {
  private static final long serialVersionUID = 1L;
  
  public CriticalXStreamException(XStreamException cause) { super(cause); }
}
