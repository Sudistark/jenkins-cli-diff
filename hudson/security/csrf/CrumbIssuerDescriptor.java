package hudson.security.csrf;

import hudson.Util;
import hudson.model.Descriptor;

public abstract class CrumbIssuerDescriptor<T extends CrumbIssuer> extends Descriptor<CrumbIssuer> {
  private String crumbSalt;
  
  private String crumbRequestField;
  
  protected CrumbIssuerDescriptor(String salt, String crumbRequestField) {
    setCrumbSalt(salt);
    setCrumbRequestField(crumbRequestField);
  }
  
  public String getCrumbSalt() { return this.crumbSalt; }
  
  public void setCrumbSalt(String salt) {
    if (Util.fixEmptyAndTrim(salt) == null) {
      this.crumbSalt = "hudson.crumb";
    } else {
      this.crumbSalt = salt;
    } 
  }
  
  public String getCrumbRequestField() { return this.crumbRequestField; }
  
  public void setCrumbRequestField(String requestField) {
    if (Util.fixEmptyAndTrim(requestField) == null) {
      this.crumbRequestField = "Jenkins-Crumb";
    } else {
      this.crumbRequestField = requestField;
    } 
  }
}
