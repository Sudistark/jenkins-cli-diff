package hudson.model;

import hudson.util.DescriptorList;
import java.util.List;

@Deprecated
public class UserProperties {
  @Deprecated
  public static final List<UserPropertyDescriptor> LIST = new DescriptorList(UserProperty.class);
}
