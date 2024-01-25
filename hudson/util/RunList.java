package hudson.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import hudson.model.AbstractBuild;
import hudson.model.Job;
import hudson.model.Node;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.model.View;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Predicate;

public class RunList<R extends Run> extends AbstractList<R> {
  private Iterable<R> base;
  
  private R first;
  
  private Integer size;
  
  public RunList() { this.base = Collections.emptyList(); }
  
  public RunList(Job j) { this.base = j.getBuilds(); }
  
  public RunList(View view) {
    Set<Job> jobs = new HashSet<Job>();
    for (TopLevelItem item : view.getItems())
      jobs.addAll(item.getAllJobs()); 
    List<Iterable<R>> runLists = new ArrayList<Iterable<R>>();
    for (Job job : jobs)
      runLists.add(job.getBuilds()); 
    this.base = combine(runLists);
  }
  
  public RunList(Collection<? extends Job> jobs) {
    List<Iterable<R>> runLists = new ArrayList<Iterable<R>>();
    for (Job j : jobs)
      runLists.add(j.getBuilds()); 
    this.base = combine(runLists);
  }
  
  public static <J extends Job<J, R>, R extends Run<J, R>> RunList<R> fromJobs(Iterable<? extends J> jobs) {
    List<Iterable<R>> runLists = new ArrayList<Iterable<R>>();
    for (Job j : jobs)
      runLists.add(j.getBuilds()); 
    return new RunList(combine(runLists));
  }
  
  private static <R extends Run> Iterable<R> combine(Iterable<Iterable<R>> runLists) { return Iterables.mergeSorted(runLists, new Object()); }
  
  private RunList(Iterable<R> c) { this.base = c; }
  
  public Iterator<R> iterator() { return this.base.iterator(); }
  
  @Deprecated
  public int size() {
    if (this.size == null) {
      int sz = 0;
      for (Iterator iterator = iterator(); iterator.hasNext(); ) {
        R r = (R)(Run)iterator.next();
        this.first = r;
        sz++;
      } 
      this.size = Integer.valueOf(sz);
    } 
    return this.size.intValue();
  }
  
  @Deprecated
  public R get(int index) { return (R)(Run)Iterators.get(iterator(), index); }
  
  public List<R> subList(int fromIndex, int toIndex) {
    int sublistSize = (toIndex < fromIndex) ? 0 : (toIndex - fromIndex);
    List<R> r = new ArrayList<R>(sublistSize);
    Iterator<R> itr = iterator();
    Iterators.skip(itr, fromIndex);
    for (int i = toIndex - fromIndex; i > 0; i--)
      r.add((Run)itr.next()); 
    return r;
  }
  
  public Spliterator<R> spliterator() { return this.base.spliterator(); }
  
  public int indexOf(Object o) {
    int index = 0;
    for (Iterator iterator = iterator(); iterator.hasNext(); ) {
      R r = (R)(Run)iterator.next();
      if (r.equals(o))
        return index; 
      index++;
    } 
    return -1;
  }
  
  public int lastIndexOf(Object o) {
    int a = -1;
    int index = 0;
    for (Iterator iterator = iterator(); iterator.hasNext(); ) {
      R r = (R)(Run)iterator.next();
      if (r.equals(o))
        a = index; 
      index++;
    } 
    return a;
  }
  
  public boolean isEmpty() { return !iterator().hasNext(); }
  
  @Deprecated
  public R getFirstBuild() {
    size();
    return (R)this.first;
  }
  
  public R getLastBuild() {
    Iterator<R> itr = iterator();
    return (R)(itr.hasNext() ? (Run)itr.next() : null);
  }
  
  public static <R extends Run> RunList<R> fromRuns(Collection<? extends R> runs) { return new RunList(runs); }
  
  public RunList<R> filter(Predicate<R> predicate) { return filter(new PredicateAdapter(predicate)); }
  
  @Deprecated
  public RunList<R> filter(Predicate<R> predicate) {
    this.size = null;
    this.first = null;
    this.base = Iterables.filter(this.base, predicate);
    return this;
  }
  
  private RunList<R> limit(Iterators.CountingPredicate<R> predicate) {
    this.size = null;
    this.first = null;
    Iterable<R> nested = this.base;
    this.base = new Object(this, nested, predicate);
    return this;
  }
  
  public RunList<R> limit(int n) { return limit(new Object(this, n)); }
  
  public RunList<R> failureOnly() { return filter(r -> (r.getResult() != Result.SUCCESS)); }
  
  public RunList<R> overThresholdOnly(Result threshold) { return filter(r -> (r.getResult() != null && r.getResult().isBetterOrEqualTo(threshold))); }
  
  public RunList<R> completedOnly() { return filter(r -> !r.isBuilding()); }
  
  public RunList<R> node(Node node) { return filter(r -> (r instanceof AbstractBuild && ((AbstractBuild)r).getBuiltOn() == node)); }
  
  public RunList<R> regressionOnly() { return filter(r -> (r.getBuildStatusSummary()).isWorse); }
  
  public RunList<R> byTimestamp(long start, long end) { return 
      limit(new Object(this, start))



      
      .filter(r -> (r.getTimeInMillis() < end)); }
  
  public RunList<R> newBuilds() {
    GregorianCalendar cal = new GregorianCalendar();
    cal.add(6, -7);
    long t = cal.getTimeInMillis();
    return filter(r -> !r.isBuilding())
      
      .limit(new Object(this, t));
  }
}
