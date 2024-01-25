package hudson.model.labels;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import java.util.Collection;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class LabelAtomProperty extends AbstractDescribableImpl<LabelAtomProperty> implements ExtensionPoint {
  public Collection<? extends Action> getActions(LabelAtom atom) { return Collections.emptyList(); }
  
  public static DescriptorExtensionList<LabelAtomProperty, LabelAtomPropertyDescriptor> all() { return Jenkins.get().getDescriptorList(LabelAtomProperty.class); }
}
