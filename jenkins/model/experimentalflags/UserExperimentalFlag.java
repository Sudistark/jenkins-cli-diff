package jenkins.model.experimentalflags;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.User;

public abstract class UserExperimentalFlag<T> extends Object implements ExtensionPoint {
  private final String flagKey;
  
  protected UserExperimentalFlag(@NonNull String flagKey) { this.flagKey = flagKey; }
  
  @NonNull
  public abstract T getDefaultValue();
  
  @Nullable
  public abstract Object serializeValue(T paramT);
  
  @Nullable
  protected abstract T deserializeValue(Object paramObject);
  
  public abstract String getDisplayName();
  
  @Nullable
  public abstract String getShortDescription();
  
  @NonNull
  public String getFlagKey() { return this.flagKey; }
  
  @NonNull
  public T getFlagValue() {
    User currentUser = User.current();
    if (currentUser == null)
      return (T)getDefaultValue(); 
    return (T)getFlagValue(currentUser);
  }
  
  @NonNull
  public T getFlagValue(User user) {
    UserExperimentalFlagsProperty property = (UserExperimentalFlagsProperty)user.getProperty(UserExperimentalFlagsProperty.class);
    if (property == null)
      return (T)getDefaultValue(); 
    Object value = property.getFlagValue(this.flagKey);
    if (value == null)
      return (T)getDefaultValue(); 
    T convertedValue = (T)deserializeValue(value);
    if (convertedValue == null)
      return (T)getDefaultValue(); 
    return convertedValue;
  }
  
  public String getFlagDescriptionPage() { return "flagDescription.jelly"; }
  
  public String getFlagConfigPage() { return "flagConfig.jelly"; }
  
  @NonNull
  public static ExtensionList<UserExperimentalFlag> all() { return ExtensionList.lookup(UserExperimentalFlag.class); }
  
  @CheckForNull
  public static <T> T getFlagValueForCurrentUser(String flagClassCanonicalName) {
    Class<? extends UserExperimentalFlag<T>> flagClass;
    try {
      Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(flagClassCanonicalName);
      if (!UserExperimentalFlag.class.isAssignableFrom(clazz))
        return null; 
      flagClass = clazz;
    } catch (Exception e) {
      return null;
    } 
    UserExperimentalFlag<T> userExperimentalFlag = (UserExperimentalFlag)all().get(flagClass);
    if (userExperimentalFlag == null)
      return null; 
    return (T)userExperimentalFlag.getFlagValue();
  }
}
