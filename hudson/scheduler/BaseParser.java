package hudson.scheduler;

import jenkins.util.SystemProperties;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

abstract class BaseParser extends Parser {
  static final int[] LOWER_BOUNDS = { 0, 0, 1, 1, 0 };
  
  static final int[] UPPER_BOUNDS = { 59, 23, 31, 12, 7 };
  
  protected Hash hash = Hash.zero();
  
  private String errorMessage;
  
  BaseParser(TokenStream input) { super(input); }
  
  public void setHash(Hash hash) {
    if (hash == null)
      hash = Hash.zero(); 
    this.hash = hash;
  }
  
  public String getErrorMessage() { return this.errorMessage; }
  
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
  
  protected long doRange(int start, int end, int step, int field) {
    rangeCheck(start, field);
    rangeCheck(end, field);
    if (step <= 0)
      error(Messages.BaseParser_MustBePositive(Integer.valueOf(step))); 
    if (start > end)
      error(Messages.BaseParser_StartEndReversed(Integer.valueOf(end), Integer.valueOf(start))); 
    long bits = 0L;
    int i;
    for (i = start; i <= end; i += step)
      bits |= 1L << i; 
    return bits;
  }
  
  protected long doRange(int step, int field) { return doRange(LOWER_BOUNDS[field], UPPER_BOUNDS[field], step, field); }
  
  protected long doHash(int step, int field) {
    int u = UPPER_BOUNDS[field];
    if (field == 2)
      u = 28; 
    if (field == 4)
      u = 6; 
    return doHash(LOWER_BOUNDS[field], u, step, field);
  }
  
  protected long doHash(int s, int e, int step, int field) {
    rangeCheck(s, field);
    rangeCheck(e, field);
    if (step > e - s + 1) {
      error(Messages.BaseParser_OutOfRange(Integer.valueOf(step), Integer.valueOf(1), Integer.valueOf(e - s + 1)));
      throw new AssertionError();
    } 
    if (step > 1) {
      long bits = 0L;
      int i;
      for (i = this.hash.next(step) + s; i <= e; i += step)
        bits |= 1L << i; 
      assert bits != 0L;
      return bits;
    } 
    if (step <= 0) {
      error(Messages.BaseParser_MustBePositive(Integer.valueOf(step)));
      throw new AssertionError();
    } 
    assert step == 1;
    return 1L << s + this.hash.next(e + 1 - s);
  }
  
  protected void rangeCheck(int value, int field) {
    if (value < LOWER_BOUNDS[field] || UPPER_BOUNDS[field] < value)
      error(Messages.BaseParser_OutOfRange(Integer.valueOf(value), Integer.valueOf(LOWER_BOUNDS[field]), Integer.valueOf(UPPER_BOUNDS[field]))); 
  }
  
  private void error(String msg) {
    setErrorMessage(msg);
    throw new InputMismatchException(this);
  }
  
  protected Hash getHashForTokens() { return HASH_TOKENS ? this.hash : Hash.zero(); }
  
  public static boolean HASH_TOKENS = !"false".equals(SystemProperties.getString(BaseParser.class.getName() + ".hash"));
  
  public static final int NO_STEP = 1;
}
