package hudson.tasks;

import hudson.ExtensionList;
import hudson.ExtensionListView;
import hudson.ExtensionPoint;
import hudson.model.User;
import java.util.List;

public abstract class UserNameResolver implements ExtensionPoint {
  public abstract String findNameFor(User paramUser);
  
  public static String resolve(User u) {
    for (UserNameResolver r : all()) {
      String name = r.findNameFor(u);
      if (name != null)
        return name; 
    } 
    return null;
  }
  
  public static ExtensionList<UserNameResolver> all() { return ExtensionList.lookup(UserNameResolver.class); }
  
  @Deprecated
  public static final List<UserNameResolver> LIST = ExtensionListView.createList(UserNameResolver.class);
}
