package hudson.scm.browsers;

public final class QueryBuilder {
  private final StringBuilder buf;
  
  public QueryBuilder(String s) {
    this.buf = new StringBuilder();
    add(s);
  }
  
  public QueryBuilder add(String s) {
    if (s == null)
      return this; 
    if (this.buf.length() == 0) {
      this.buf.append('?');
    } else {
      this.buf.append('&');
    } 
    this.buf.append(s);
    return this;
  }
  
  public String toString() { return this.buf.toString(); }
}
