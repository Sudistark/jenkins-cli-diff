package jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.util.Comparator;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kohsuke.accmod.Restricted;

public abstract class IdStrategy extends AbstractDescribableImpl<IdStrategy> implements ExtensionPoint, Comparator<String> {
  private static final Pattern PSEUDO_UNICODE_PATTERN = Pattern.compile("\\$[a-f0-9]{4}");
  
  private static final Pattern CAPITALIZATION_PATTERN = Pattern.compile("~[a-z]");
  
  @SuppressFBWarnings(value = {"MS_SHOULD_BE_FINAL"}, justification = "used in several plugins")
  public static IdStrategy CASE_INSENSITIVE = new CaseInsensitive();
  
  @Deprecated
  public String filenameOf(@NonNull String id) { return null; }
  
  @Deprecated
  @Restricted({org.kohsuke.accmod.restrictions.ProtectedExternally.class})
  public String legacyFilenameOf(@NonNull String id) { return null; }
  
  @Deprecated
  public String idFromFilename(@NonNull String filename) { return filename; }
  
  @NonNull
  public String keyFor(@NonNull String id) { return id; }
  
  public boolean equals(@NonNull String id1, @NonNull String id2) { return (compare(id1, id2) == 0); }
  
  public IdStrategyDescriptor getDescriptor() { return (IdStrategyDescriptor)super.getDescriptor(); }
  
  public boolean equals(Object obj) { return (this == obj || (obj != null && getClass().equals(obj.getClass()))); }
  
  public int hashCode() { return getClass().hashCode(); }
  
  public String toString() { return getClass().getName(); }
  
  public static DescriptorExtensionList<IdStrategy, IdStrategyDescriptor> all() { return Jenkins.get().getDescriptorList(IdStrategy.class); }
  
  String applyPatternRepeatedly(@NonNull Pattern pattern, @NonNull String filename, @NonNull Function<String, Character> converter) {
    StringBuilder id = new StringBuilder();
    int beginIndex = 0;
    Matcher matcher = pattern.matcher(filename);
    while (matcher.find()) {
      String group = matcher.group();
      id.append(filename, beginIndex, matcher.start());
      id.append(converter.apply(group));
      beginIndex = matcher.end();
    } 
    id.append(filename.substring(beginIndex));
    return id.toString();
  }
  
  Character convertPseudoUnicode(String matchedGroup) { return Character.valueOf((char)Integer.parseInt(matchedGroup.substring(1), 16)); }
  
  public abstract int compare(@NonNull String paramString1, @NonNull String paramString2);
}
