package hudson.model.queue;

import hudson.model.Action;
import hudson.model.Queue;
import java.util.List;

public interface FoldableAction extends Action {
  void foldIntoExisting(Queue.Item paramItem, Queue.Task paramTask, List<Action> paramList);
}
