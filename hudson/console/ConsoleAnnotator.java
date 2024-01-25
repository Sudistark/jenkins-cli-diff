package hudson.console;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.MarkupText;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ConsoleAnnotator<T> extends Object implements Serializable {
  private static final long serialVersionUID = 1L;
  
  @CheckForNull
  public abstract ConsoleAnnotator<T> annotate(@NonNull T paramT, @NonNull MarkupText paramMarkupText);
  
  public static <T> ConsoleAnnotator<T> cast(ConsoleAnnotator<? super T> a) { return a; }
  
  public static <T> ConsoleAnnotator<T> combine(Collection<? extends ConsoleAnnotator<? super T>> all) {
    switch (all.size()) {
      case 0:
        return null;
      case 1:
        return cast((ConsoleAnnotator)all.iterator().next());
    } 
    return new ConsoleAnnotatorAggregator(all);
  }
  
  public static <T> ConsoleAnnotator<T> initial(T context) { return combine(_for(context)); }
  
  public static <T> List<ConsoleAnnotator<T>> _for(T context) {
    List<ConsoleAnnotator<T>> r = new ArrayList<ConsoleAnnotator<T>>();
    for (ConsoleAnnotatorFactory f : ConsoleAnnotatorFactory.all()) {
      if (f.type().isInstance(context)) {
        ConsoleAnnotator ca = f.newInstance(context);
        if (ca != null)
          r.add(ca); 
      } 
    } 
    return r;
  }
}
