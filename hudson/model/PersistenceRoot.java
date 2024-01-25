package hudson.model;

import java.io.File;

public interface PersistenceRoot extends Saveable {
  File getRootDir();
}
