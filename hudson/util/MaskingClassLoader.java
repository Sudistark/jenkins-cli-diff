package hudson.util;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MaskingClassLoader extends ClassLoader {
  private final List<String> masksClasses = new CopyOnWriteArrayList();
  
  private final List<String> masksResources = new CopyOnWriteArrayList();
  
  static  {
    registerAsParallelCapable();
  }
  
  public MaskingClassLoader(ClassLoader parent, String... masks) { this(parent, Arrays.asList(masks)); }
  
  public MaskingClassLoader(ClassLoader parent, Collection<String> masks) {
    super(parent);
    this.masksClasses.addAll(masks);
    for (String mask : masks)
      this.masksResources.add(mask.replace('.', '/')); 
  }
  
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    for (String mask : this.masksClasses) {
      if (name.startsWith(mask))
        throw new ClassNotFoundException(); 
    } 
    return super.loadClass(name, resolve);
  }
  
  public URL getResource(String name) {
    if (isMasked(name))
      return null; 
    return super.getResource(name);
  }
  
  public Enumeration<URL> getResources(String name) throws IOException {
    if (isMasked(name))
      return Collections.emptyEnumeration(); 
    return super.getResources(name);
  }
  
  public void add(String prefix) {
    this.masksClasses.add(prefix);
    if (prefix != null)
      this.masksResources.add(prefix.replace('.', '/')); 
  }
  
  private boolean isMasked(String name) {
    for (String mask : this.masksResources) {
      if (name.startsWith(mask))
        return true; 
    } 
    return false;
  }
}
