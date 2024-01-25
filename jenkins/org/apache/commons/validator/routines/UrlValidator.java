package jenkins.org.apache.commons.validator.routines;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.accmod.Restricted;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
public class UrlValidator implements Serializable {
  private static final long serialVersionUID = 7557161713937335013L;
  
  private static final int MAX_UNSIGNED_16_BIT_INT = 65535;
  
  public static final long ALLOW_ALL_SCHEMES = 1L;
  
  public static final long ALLOW_2_SLASHES = 2L;
  
  public static final long NO_FRAGMENTS = 4L;
  
  public static final long ALLOW_LOCAL_URLS = 8L;
  
  private static final String SCHEME_REGEX = "^\\p{Alpha}[\\p{Alnum}\\+\\-\\.]*";
  
  private static final Pattern SCHEME_PATTERN = Pattern.compile("^\\p{Alpha}[\\p{Alnum}\\+\\-\\.]*");
  
  private static final String AUTHORITY_CHARS_REGEX = "\\p{Alnum}\\-\\.";
  
  private static final String IPV6_REGEX = "::FFFF:(?:\\d{1,3}\\.){3}\\d{1,3}|[0-9a-fA-F:]+";
  
  private static final String USERINFO_CHARS_REGEX = "[a-zA-Z0-9%-._~!$&'()*+,;=]";
  
  private static final String USERINFO_FIELD_REGEX = "[a-zA-Z0-9%-._~!$&'()*+,;=]+(?::[a-zA-Z0-9%-._~!$&'()*+,;=]*)?@";
  
  private static final String AUTHORITY_REGEX = "(?:\\[(::FFFF:(?:\\d{1,3}\\.){3}\\d{1,3}|[0-9a-fA-F:]+)\\]|(?:(?:[a-zA-Z0-9%-._~!$&'()*+,;=]+(?::[a-zA-Z0-9%-._~!$&'()*+,;=]*)?@)?([\\p{Alnum}\\-\\.]*)))(?::(\\d*))?(.*)?";
  
  private static final Pattern AUTHORITY_PATTERN = Pattern.compile("(?:\\[(::FFFF:(?:\\d{1,3}\\.){3}\\d{1,3}|[0-9a-fA-F:]+)\\]|(?:(?:[a-zA-Z0-9%-._~!$&'()*+,;=]+(?::[a-zA-Z0-9%-._~!$&'()*+,;=]*)?@)?([\\p{Alnum}\\-\\.]*)))(?::(\\d*))?(.*)?");
  
  private static final int PARSE_AUTHORITY_IPV6 = 1;
  
  private static final int PARSE_AUTHORITY_HOST_IP = 2;
  
  private static final int PARSE_AUTHORITY_PORT = 3;
  
  private static final int PARSE_AUTHORITY_EXTRA = 4;
  
  private static final String PATH_REGEX = "^(/[-\\w:@&?=+,.!/~*'%$_;\\(\\)]*)?$";
  
  private static final Pattern PATH_PATTERN = Pattern.compile("^(/[-\\w:@&?=+,.!/~*'%$_;\\(\\)]*)?$");
  
  private static final String QUERY_REGEX = "^(\\S*)$";
  
  private static final Pattern QUERY_PATTERN = Pattern.compile("^(\\S*)$");
  
  private final long options;
  
  private final Set<String> allowedSchemes;
  
  private final RegexValidator authorityValidator;
  
  private static final String[] DEFAULT_SCHEMES = { "http", "https", "ftp" };
  
  private static final UrlValidator DEFAULT_URL_VALIDATOR = new UrlValidator();
  
  private final DomainValidator domainValidator;
  
  public static UrlValidator getInstance() { return DEFAULT_URL_VALIDATOR; }
  
  public UrlValidator() { this(null); }
  
  public UrlValidator(String[] schemes) { this(schemes, 0L); }
  
  public UrlValidator(long options) { this(null, null, options); }
  
  public UrlValidator(String[] schemes, long options) { this(schemes, null, options); }
  
  public UrlValidator(RegexValidator authorityValidator, long options) { this(null, authorityValidator, options); }
  
  public UrlValidator(String[] schemes, RegexValidator authorityValidator, long options) { this(schemes, authorityValidator, options, DomainValidator.getInstance(isOn(8L, options))); }
  
  public UrlValidator(String[] schemes, RegexValidator authorityValidator, long options, DomainValidator domainValidator) {
    this.options = options;
    if (domainValidator == null)
      throw new IllegalArgumentException("DomainValidator must not be null"); 
    if (domainValidator.isAllowLocal() != (((options & 0x8L) > 0L)))
      throw new IllegalArgumentException("DomainValidator disagrees with ALLOW_LOCAL_URLS setting"); 
    this.domainValidator = domainValidator;
    if (isOn(1L)) {
      this.allowedSchemes = Collections.emptySet();
    } else {
      if (schemes == null)
        schemes = DEFAULT_SCHEMES; 
      this.allowedSchemes = new HashSet(schemes.length);
      for (String scheme : schemes)
        this.allowedSchemes.add(scheme.toLowerCase(Locale.ENGLISH)); 
    } 
    this.authorityValidator = authorityValidator;
  }
  
  public boolean isValid(String value) {
    URI uri;
    if (value == null)
      return false; 
    try {
      uri = new URI(value);
    } catch (URISyntaxException e) {
      return false;
    } 
    String scheme = uri.getScheme();
    if (!isValidScheme(scheme))
      return false; 
    String authority = uri.getRawAuthority();
    if ("file".equals(scheme) && (authority == null || "".equals(authority)))
      return true; 
    if ("file".equals(scheme) && authority != null && authority.contains(":"))
      return false; 
    if (!isValidAuthority(authority))
      return false; 
    if (!isValidPath(uri.getRawPath()))
      return false; 
    if (!isValidQuery(uri.getRawQuery()))
      return false; 
    if (!isValidFragment(uri.getRawFragment()))
      return false; 
    return true;
  }
  
  protected boolean isValidScheme(String scheme) {
    if (scheme == null)
      return false; 
    if (!SCHEME_PATTERN.matcher(scheme).matches())
      return false; 
    if (isOff(1L) && !this.allowedSchemes.contains(scheme.toLowerCase(Locale.ENGLISH)))
      return false; 
    return true;
  }
  
  protected boolean isValidAuthority(String authority) {
    if (authority == null)
      return false; 
    if (this.authorityValidator != null && this.authorityValidator.isValid(authority))
      return true; 
    String authorityASCII = DomainValidator.unicodeToASCII(authority);
    Matcher authorityMatcher = AUTHORITY_PATTERN.matcher(authorityASCII);
    if (!authorityMatcher.matches())
      return false; 
    String ipv6 = authorityMatcher.group(1);
    if (ipv6 != null) {
      InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
      if (!inetAddressValidator.isValidInet6Address(ipv6))
        return false; 
    } else {
      String hostLocation = authorityMatcher.group(2);
      if (!this.domainValidator.isValid(hostLocation)) {
        InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
        if (!inetAddressValidator.isValidInet4Address(hostLocation))
          return false; 
      } 
      String port = authorityMatcher.group(3);
      if (port != null && port.length() > 0)
        try {
          int iPort = Integer.parseInt(port);
          if (iPort < 0 || iPort > 65535)
            return false; 
        } catch (NumberFormatException nfe) {
          return false;
        }  
    } 
    String extra = authorityMatcher.group(4);
    if (extra != null && extra.trim().length() > 0)
      return false; 
    return true;
  }
  
  protected boolean isValidPath(String path) {
    if (path == null)
      return false; 
    if (!PATH_PATTERN.matcher(path).matches())
      return false; 
    try {
      URI uri = new URI(null, "localhost", path, null);
      String norm = uri.normalize().getPath();
      if (norm.startsWith("/../") || norm
        .equals("/.."))
        return false; 
    } catch (URISyntaxException e) {
      return false;
    } 
    int slash2Count = countToken("//", path);
    if (isOff(2L) && slash2Count > 0)
      return false; 
    return true;
  }
  
  protected boolean isValidQuery(String query) {
    if (query == null)
      return true; 
    return QUERY_PATTERN.matcher(query).matches();
  }
  
  protected boolean isValidFragment(String fragment) {
    if (fragment == null)
      return true; 
    return isOff(4L);
  }
  
  protected int countToken(String token, String target) {
    int tokenIndex = 0;
    int count = 0;
    while (tokenIndex != -1) {
      tokenIndex = target.indexOf(token, tokenIndex);
      if (tokenIndex > -1) {
        tokenIndex++;
        count++;
      } 
    } 
    return count;
  }
  
  private boolean isOn(long flag) { return ((this.options & flag) > 0L); }
  
  private static boolean isOn(long flag, long options) { return ((options & flag) > 0L); }
  
  private boolean isOff(long flag) { return ((this.options & flag) == 0L); }
}
