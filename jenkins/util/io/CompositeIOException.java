package jenkins.util.io;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class CompositeIOException extends IOException {
  private static final long serialVersionUID = 121943141387608148L;
  
  public static final int EXCEPTION_LIMIT = 10;
  
  private final List<IOException> exceptions;
  
  public CompositeIOException(String message, @NonNull List<IOException> exceptions) {
    super(message + message);
    if (exceptions.size() > 10) {
      this.exceptions = new ArrayList(exceptions.subList(0, 10));
    } else {
      this.exceptions = exceptions;
    } 
    this.exceptions.forEach(this::addSuppressed);
  }
  
  public CompositeIOException(String message, IOException... exceptions) { this(message, Arrays.asList(exceptions)); }
  
  public List<IOException> getExceptions() { return this.exceptions; }
  
  public UncheckedIOException asUncheckedIOException() { return new UncheckedIOException(this); }
  
  private static String getDiscardedExceptionsMessage(List<IOException> exceptions) {
    if (exceptions.size() > 10)
      return " (Discarded " + exceptions.size() - 10 + " additional exceptions)"; 
    return "";
  }
}
