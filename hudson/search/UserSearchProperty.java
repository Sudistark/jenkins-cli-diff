package hudson.search;

import hudson.model.User;
import hudson.model.UserProperty;
import org.kohsuke.stapler.export.Exported;

public class UserSearchProperty extends UserProperty {
  private static final boolean DEFAULT_SEARCH_CASE_INSENSITIVE_MODE = true;
  
  private final boolean insensitiveSearch;
  
  public UserSearchProperty(boolean insensitiveSearch) { this.insensitiveSearch = insensitiveSearch; }
  
  @Exported
  public boolean getInsensitiveSearch() { return this.insensitiveSearch; }
  
  public static boolean isCaseInsensitive() {
    user = User.current();
    if (user == null)
      return true; 
    return ((UserSearchProperty)user.getProperty(UserSearchProperty.class)).getInsensitiveSearch();
  }
}
