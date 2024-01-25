package hudson.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import org.apache.tools.ant.types.Resource;

public class StreamResource extends Resource {
  private final InputStream in;
  
  public StreamResource(String name, @NonNull InputStream in) {
    this.in = (InputStream)Objects.requireNonNull(in);
    setName(name);
  }
  
  public InputStream getInputStream() throws IOException { return this.in; }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o == null || getClass() != o.getClass())
      return false; 
    if (!super.equals(o))
      return false; 
    StreamResource resource = (StreamResource)o;
    return Objects.equals(this.in, resource.in);
  }
  
  public int hashCode() { return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.in }); }
}
