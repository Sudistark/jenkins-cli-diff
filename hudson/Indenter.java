package hudson;

import hudson.model.Job;

public abstract class Indenter<J extends Job> extends Object {
  protected abstract int getNestLevel(J paramJ);
  
  public final String getCss(J job) { return "padding-left: " + getNestLevel(job) * 2 + "em"; }
  
  public final String getRelativeShift(J job) {
    int i = getNestLevel(job);
    if (i == 0)
      return null; 
    return "position:relative; left: " + i * 2 + "em";
  }
}
