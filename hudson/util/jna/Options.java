package hudson.util.jna;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;

@SuppressFBWarnings(value = {"MS_OOI_PKGPROTECT"}, justification = "for backward compatibility")
public interface Options {
  public static final Map<String, Object> UNICODE_OPTIONS = new Object();
}
