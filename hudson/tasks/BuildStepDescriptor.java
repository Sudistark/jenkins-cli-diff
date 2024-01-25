package hudson.tasks;

import hudson.model.AbstractProject;
import hudson.model.Describable;
import hudson.model.Descriptor;
import java.util.ArrayList;
import java.util.List;
import jenkins.model.Jenkins;

public abstract class BuildStepDescriptor<T extends BuildStep & Describable<T>> extends Descriptor<T> {
  protected BuildStepDescriptor(Class<? extends T> clazz) { super(clazz); }
  
  protected BuildStepDescriptor() {}
  
  public abstract boolean isApplicable(Class<? extends AbstractProject> paramClass);
  
  public static <T extends BuildStep & Describable<T>> List<Descriptor<T>> filter(List<Descriptor<T>> base, Class<? extends AbstractProject> type) {
    Descriptor pd = Jenkins.get().getDescriptor(type);
    List<Descriptor<T>> r = new ArrayList<Descriptor<T>>(base.size());
    for (Descriptor<T> d : base) {
      if (pd instanceof AbstractProject.AbstractProjectDescriptor && !((AbstractProject.AbstractProjectDescriptor)pd).isApplicable(d))
        continue; 
      if (d instanceof BuildStepDescriptor) {
        BuildStepDescriptor<T> bd = (BuildStepDescriptor)d;
        if (!bd.isApplicable(type))
          continue; 
        r.add(bd);
        continue;
      } 
      r.add(d);
    } 
    return r;
  }
}
