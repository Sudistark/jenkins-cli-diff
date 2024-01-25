package hudson.model;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

final class RunnerStack {
  private final Map<Executor, Stack<Run.RunExecution>> stack = new WeakHashMap();
  
  void push(Run.RunExecution r) {
    Executor e = Executor.currentExecutor();
    Stack<Run.RunExecution> s = (Stack)this.stack.computeIfAbsent(e, k -> new Stack());
    s.push(r);
  }
  
  void pop() {
    Executor e = Executor.currentExecutor();
    Stack<Run.RunExecution> s = (Stack)this.stack.get(e);
    s.pop();
    if (s.isEmpty())
      this.stack.remove(e); 
  }
  
  @CheckForNull
  Run.RunExecution peek() {
    Executor e = Executor.currentExecutor();
    if (e != null) {
      Stack<Run.RunExecution> s = (Stack)this.stack.get(e);
      if (s != null && !s.isEmpty())
        return (Run.RunExecution)s.peek(); 
    } 
    return null;
  }
  
  static final RunnerStack INSTANCE = new RunnerStack();
}
