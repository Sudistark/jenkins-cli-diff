package hudson;

import hudson.util.CopyOnWriteList;
import java.util.List;

public class ExtensionListView {
  public static <T> List<T> createList(Class<T> type) { return new Object(type); }
  
  public static <T> CopyOnWriteList<T> createCopyOnWriteList(Class<T> type) { return new Object(type); }
}
