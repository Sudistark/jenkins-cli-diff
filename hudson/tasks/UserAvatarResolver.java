package hudson.tasks;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.User;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class UserAvatarResolver implements ExtensionPoint {
  static Pattern iconSizeRegex = Pattern.compile("(\\d+)x(\\d+)");
  
  public abstract String findAvatarFor(User paramUser, int paramInt1, int paramInt2);
  
  public static String resolve(User u, String avatarSize) {
    String avatar = resolveOrNull(u, avatarSize);
    return (avatar != null) ? avatar : "symbol-person-circle";
  }
  
  @CheckForNull
  public static String resolveOrNull(User u, String avatarSize) {
    Matcher matcher = iconSizeRegex.matcher(avatarSize);
    if (matcher.matches() && matcher.groupCount() == 2) {
      int width = Integer.parseInt(matcher.group(1));
      int height = Integer.parseInt(matcher.group(2));
      for (UserAvatarResolver r : all()) {
        String name = r.findAvatarFor(u, width, height);
        if (name != null)
          return name; 
      } 
    } else {
      LOGGER.warning(String.format("Could not split up the avatar size (%s) into a width and height.", new Object[] { avatarSize }));
    } 
    return null;
  }
  
  public static ExtensionList<UserAvatarResolver> all() { return ExtensionList.lookup(UserAvatarResolver.class); }
  
  private static final Logger LOGGER = Logger.getLogger(UserAvatarResolver.class.getName());
}
