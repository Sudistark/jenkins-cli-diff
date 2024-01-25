package jenkins.scm;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Item;
import java.util.ArrayList;
import java.util.List;

public abstract class SCMDecisionHandler implements ExtensionPoint {
  public abstract boolean shouldPoll(@NonNull Item paramItem);
  
  @NonNull
  public static ExtensionList<SCMDecisionHandler> all() { return ExtensionList.lookup(SCMDecisionHandler.class); }
  
  @CheckForNull
  public static SCMDecisionHandler firstShouldPollVeto(@NonNull Item item) {
    for (SCMDecisionHandler handler : all()) {
      if (!handler.shouldPoll(item))
        return handler; 
    } 
    return null;
  }
  
  @NonNull
  public static List<SCMDecisionHandler> listShouldPollVetos(@NonNull Item item) {
    List<SCMDecisionHandler> result = new ArrayList<SCMDecisionHandler>();
    for (SCMDecisionHandler handler : all()) {
      if (!handler.shouldPoll(item))
        result.add(handler); 
    } 
    return result;
  }
}
