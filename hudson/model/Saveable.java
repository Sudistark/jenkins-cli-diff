package hudson.model;

import java.io.IOException;

public interface Saveable {
  public static final Saveable NOOP = () -> {
    
    };
  
  void save() throws IOException;
}
