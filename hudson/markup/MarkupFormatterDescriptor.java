package hudson.markup;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

public abstract class MarkupFormatterDescriptor extends Descriptor<MarkupFormatter> {
  public static DescriptorExtensionList<MarkupFormatter, MarkupFormatterDescriptor> all() {
    return Jenkins.get()
      .getDescriptorList(MarkupFormatter.class);
  }
}
