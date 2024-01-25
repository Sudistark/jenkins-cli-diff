package hudson.scm;

import hudson.model.AbstractProject;
import jenkins.model.Jenkins;

@Deprecated
final class AutoBrowserHolder {
  private int cacheGeneration;
  
  private RepositoryBrowser cache;
  
  private SCM owner;
  
  AutoBrowserHolder(SCM owner) { this.owner = owner; }
  
  public RepositoryBrowser get() {
    if (this.cacheGeneration == -1)
      return this.cache; 
    SCMDescriptor<?> d = this.owner.getDescriptor();
    RepositoryBrowser<?> dflt = this.owner.guessBrowser();
    if (dflt != null) {
      this.cache = dflt;
      this.cacheGeneration = -1;
      return this.cache;
    } 
    int g = d.getGeneration();
    if (g != this.cacheGeneration) {
      this.cacheGeneration = g;
      this.cache = infer();
    } 
    return this.cache;
  }
  
  private RepositoryBrowser infer() {
    for (AbstractProject p : Jenkins.get().allItems(AbstractProject.class)) {
      SCM scm = p.getScm();
      if (scm != null && scm.getClass() == this.owner.getClass() && scm.getBrowser() != null && scm
        .getDescriptor().isBrowserReusable(scm, this.owner))
        return scm.getBrowser(); 
    } 
    return null;
  }
}
