package jenkins.security;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.io.IOException;
import java.util.function.Function;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.acegisecurity.AcegiSecurityException;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class AcegiSecurityExceptionFilter implements Filter {
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch (IOException x) {
      throw (IOException)translate(x, IOException::new);
    } catch (ServletException x) {
      throw (ServletException)translate(x, ServletException::new);
    } catch (RuntimeException x) {
      throw (RuntimeException)translate(x, RuntimeException::new);
    } 
  }
  
  private static <T extends Throwable> T translate(T t, Function<Throwable, T> ctor) {
    RuntimeException cause = convertedCause(t);
    if (cause != null) {
      T t2 = (T)(Throwable)ctor.apply(cause);
      t2.addSuppressed(t);
      return t2;
    } 
    return t;
  }
  
  @CheckForNull
  private static RuntimeException convertedCause(@CheckForNull Throwable t) {
    if (t instanceof AcegiSecurityException)
      return ((AcegiSecurityException)t).toSpring(); 
    if (t != null)
      return convertedCause(t.getCause()); 
    return null;
  }
  
  public void init(FilterConfig filterConfig) throws ServletException {}
  
  public void destroy() {}
}
