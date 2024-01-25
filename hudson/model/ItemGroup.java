package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.security.access.AccessDeniedException;

public interface ItemGroup<T extends Item> extends PersistenceRoot, ModelObject {
  String getFullName();
  
  String getFullDisplayName();
  
  Collection<T> getItems();
  
  default Collection<T> getItems(Predicate<T> pred) {
    return (Collection)getItemsStream(pred)
      .collect(Collectors.toList());
  }
  
  default Stream<T> getItemsStream() { return getItems().stream(); }
  
  default Stream<T> getItemsStream(Predicate<T> pred) { return getItemsStream().filter(pred); }
  
  String getUrl();
  
  String getUrlChildPrefix();
  
  @CheckForNull
  T getItem(String paramString) throws AccessDeniedException;
  
  File getRootDirFor(T paramT);
  
  default void onRenamed(T item, String oldName, String newName) throws IOException {}
  
  void onDeleted(T paramT) throws IOException;
  
  default <T extends Item> List<T> getAllItems(Class<T> type) { return Items.getAllItems(this, type); }
  
  default <T extends Item> List<T> getAllItems(Class<T> type, Predicate<T> pred) { return Items.getAllItems(this, type, pred); }
  
  default <T extends Item> Iterable<T> allItems(Class<T> type) { return Items.allItems(this, type); }
  
  default <T extends Item> Iterable<T> allItems(Class<T> type, Predicate<T> pred) { return Items.allItems(this, type, pred); }
  
  default List<Item> getAllItems() { return getAllItems(Item.class); }
  
  default Iterable<Item> allItems() { return allItems(Item.class); }
}
