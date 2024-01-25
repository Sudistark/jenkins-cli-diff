package hudson.model;

public class NoFingerprintMatch implements ModelObject {
  private final String md5sum;
  
  public NoFingerprintMatch(String md5sum) { this.md5sum = md5sum; }
  
  public String getDisplayName() { return this.md5sum; }
}
