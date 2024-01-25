package hudson.model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jvnet.tiger_types.Types;
import org.kohsuke.stapler.StaplerRequest;

public abstract class JobPropertyDescriptor extends Descriptor<JobProperty<?>> {
  protected JobPropertyDescriptor(Class<? extends JobProperty<?>> clazz) { super(clazz); }
  
  protected JobPropertyDescriptor() {}
  
  public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
    if (formData.isNullObject())
      formData = new JSONObject(); 
    return (JobProperty)super.newInstance(req, formData);
  }
  
  public boolean isApplicable(Class<? extends Job> jobType) {
    Type parameterization = Types.getBaseClass(this.clazz, JobProperty.class);
    if (parameterization instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType)parameterization;
      Class applicable = Types.erasure(Types.getTypeArgument(pt, 0));
      return applicable.isAssignableFrom(jobType);
    } 
    throw new AssertionError("" + this.clazz + " doesn't properly parameterize JobProperty. The isApplicable() method must be overridden.");
  }
  
  public static List<JobPropertyDescriptor> getPropertyDescriptors(Class<? extends Job> clazz) {
    List<JobPropertyDescriptor> r = new ArrayList<JobPropertyDescriptor>();
    for (JobPropertyDescriptor p : all()) {
      if (p.isApplicable(clazz))
        r.add(p); 
    } 
    return r;
  }
  
  public static Collection<JobPropertyDescriptor> all() { return Jenkins.get().getDescriptorList(JobProperty.class); }
}
