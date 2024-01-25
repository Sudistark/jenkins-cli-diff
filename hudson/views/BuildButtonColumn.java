package hudson.views;

import hudson.model.AbstractItem;
import hudson.model.Messages;

public class BuildButtonColumn extends ListViewColumn {
  public String taskNoun(Object job) {
    if (job instanceof AbstractItem)
      return ((AbstractItem)job).getTaskNoun(); 
    return Messages.AbstractItem_TaskNoun();
  }
}
