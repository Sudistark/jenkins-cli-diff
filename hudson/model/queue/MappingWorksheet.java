package hudson.model.queue;

import com.google.common.collect.Iterables;
import hudson.model.Computer;
import hudson.model.Queue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MappingWorksheet {
  public final List<ExecutorChunk> executors;
  
  public final List<WorkChunk> works;
  
  public final Queue.BuildableItem item;
  
  public MappingWorksheet(Queue.BuildableItem item, List<? extends ExecutorSlot> offers) { this(item, offers, LoadPredictor.all()); }
  
  public MappingWorksheet(Queue.BuildableItem item, List<? extends ExecutorSlot> offers, Collection<? extends LoadPredictor> loadPredictors) {
    this.item = item;
    Map<Computer, List<ExecutorSlot>> j = new HashMap<Computer, List<ExecutorSlot>>();
    for (ExecutorSlot o : offers) {
      Computer c = o.getExecutor().getOwner();
      List<ExecutorSlot> l = (List)j.computeIfAbsent(c, k -> new ArrayList());
      l.add(o);
    } 
    long duration = item.task.getEstimatedDuration();
    if (duration > 0L) {
      long now = System.currentTimeMillis();
      for (Map.Entry<Computer, List<ExecutorSlot>> e : j.entrySet()) {
        List<ExecutorSlot> list = (List)e.getValue();
        int max = ((Computer)e.getKey()).countExecutors();
        Timeline timeline = new Timeline();
        int peak = 0;
        label51: for (LoadPredictor lp : loadPredictors) {
          for (FutureLoad fl : Iterables.limit(lp.predict(this, (Computer)e.getKey(), now, now + duration), 100)) {
            peak = Math.max(peak, timeline.insert(fl.startTime, fl.startTime + fl.duration, fl.numExecutors));
            if (peak >= max)
              break label51; 
          } 
        } 
        int minIdle = max - peak;
        if (minIdle < 0)
          minIdle = 0; 
        if (minIdle < list.size())
          e.setValue(list.subList(0, minIdle)); 
      } 
    } 
    List<ExecutorChunk> executors = new ArrayList<ExecutorChunk>();
    for (List<ExecutorSlot> group : j.values()) {
      if (group.isEmpty())
        continue; 
      ExecutorChunk ec = new ExecutorChunk(this, group, executors.size());
      if (ec.node == null)
        continue; 
      executors.add(ec);
    } 
    this.executors = Collections.unmodifiableList(executors);
    Map<Object, List<SubTask>> m = new LinkedHashMap<Object, List<SubTask>>();
    for (SubTask meu : item.task.getSubTasks()) {
      Object c = meu.getSameNodeConstraint();
      if (c == null)
        c = new Object(); 
      List<SubTask> l = (List)m.computeIfAbsent(c, k -> new ArrayList());
      l.add(meu);
    } 
    List<WorkChunk> works = new ArrayList<WorkChunk>();
    for (List<SubTask> group : m.values())
      works.add(new WorkChunk(this, group, works.size())); 
    this.works = Collections.unmodifiableList(works);
  }
  
  public WorkChunk works(int index) { return (WorkChunk)this.works.get(index); }
  
  public ExecutorChunk executors(int index) { return (ExecutorChunk)this.executors.get(index); }
}
