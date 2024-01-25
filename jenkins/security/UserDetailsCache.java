package jenkins.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.security.UserMayOrMayNotExistException2;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import jenkins.util.SystemProperties;
import org.kohsuke.accmod.Restricted;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
@Extension
public final class UserDetailsCache {
  private static final String SYS_PROP_NAME = UserDetailsCache.class.getName() + ".EXPIRE_AFTER_WRITE_SEC";
  
  private static Integer EXPIRE_AFTER_WRITE_SEC = SystemProperties.getInteger(SYS_PROP_NAME, Integer.valueOf((int)TimeUnit.MINUTES.toSeconds(2L)));
  
  private final Cache<String, UserDetails> detailsCache;
  
  private final Cache<String, Boolean> existenceCache;
  
  @Restricted({org.kohsuke.accmod.restrictions.NoExternalUse.class})
  @SuppressFBWarnings(value = {"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"}, justification = "field is static for script console")
  public UserDetailsCache() {
    Integer expireAfterWriteSec = EXPIRE_AFTER_WRITE_SEC;
    if (expireAfterWriteSec == null || expireAfterWriteSec.intValue() <= 0) {
      expireAfterWriteSec = SystemProperties.getInteger(SYS_PROP_NAME, Integer.valueOf((int)TimeUnit.MINUTES.toSeconds(2L)));
      if (expireAfterWriteSec.intValue() <= 0)
        expireAfterWriteSec = Integer.valueOf((int)TimeUnit.MINUTES.toSeconds(2L)); 
      EXPIRE_AFTER_WRITE_SEC = expireAfterWriteSec;
    } 
    this.detailsCache = CacheBuilder.newBuilder().softValues().expireAfterWrite(EXPIRE_AFTER_WRITE_SEC.intValue(), TimeUnit.SECONDS).build();
    this.existenceCache = CacheBuilder.newBuilder().softValues().expireAfterWrite(EXPIRE_AFTER_WRITE_SEC.intValue(), TimeUnit.SECONDS).build();
  }
  
  public static UserDetailsCache get() { return (UserDetailsCache)ExtensionList.lookupSingleton(UserDetailsCache.class); }
  
  @CheckForNull
  public UserDetails getCached(String idOrFullName) throws UsernameNotFoundException {
    Boolean exists = (Boolean)this.existenceCache.getIfPresent(idOrFullName);
    if (exists != null && !exists.booleanValue())
      throw new UserMayOrMayNotExistException2(String.format("\"%s\" does not exist", new Object[] { idOrFullName })); 
    return (UserDetails)this.detailsCache.getIfPresent(idOrFullName);
  }
  
  @NonNull
  public UserDetails loadUserByUsername(String idOrFullName) throws UsernameNotFoundException {
    Boolean exists = (Boolean)this.existenceCache.getIfPresent(idOrFullName);
    if (exists != null && !exists.booleanValue())
      throw new UsernameNotFoundException(String.format("\"%s\" does not exist", new Object[] { idOrFullName })); 
    try {
      return (UserDetails)this.detailsCache.get(idOrFullName, new Retriever(this, idOrFullName));
    } catch (ExecutionException|com.google.common.util.concurrent.UncheckedExecutionException e) {
      if (e.getCause() instanceof UsernameNotFoundException)
        throw (UsernameNotFoundException)e.getCause(); 
      throw e;
    } 
  }
  
  public void invalidateAll() {
    this.existenceCache.invalidateAll();
    this.detailsCache.invalidateAll();
  }
  
  public void invalidate(String idOrFullName) {
    this.existenceCache.invalidate(idOrFullName);
    this.detailsCache.invalidate(idOrFullName);
  }
}
