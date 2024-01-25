package hudson.security.captcha;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.IOException;
import java.io.OutputStream;
import jenkins.model.Jenkins;

public abstract class CaptchaSupport extends AbstractDescribableImpl<CaptchaSupport> implements ExtensionPoint {
  public static DescriptorExtensionList<CaptchaSupport, Descriptor<CaptchaSupport>> all() { return Jenkins.get().getDescriptorList(CaptchaSupport.class); }
  
  public CaptchaSupportDescriptor getDescriptor() { return (CaptchaSupportDescriptor)super.getDescriptor(); }
  
  public abstract boolean validateCaptcha(String paramString1, String paramString2);
  
  public abstract void generateImage(String paramString, OutputStream paramOutputStream) throws IOException;
}
