package hudson.scheduler;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jenkins.util.antlr.JenkinsANTLRErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public final class CronTab {
  final long[] bits;
  
  int dayOfWeek;
  
  private String spec;
  
  @CheckForNull
  private String specTimezone;
  
  public CronTab(String format) { this(format, null); }
  
  public CronTab(String format, Hash hash) { this(format, 1, hash); }
  
  @Deprecated(since = "1.448")
  public CronTab(String format, int line) {
    this.bits = new long[4];
    set(format, line, null);
  }
  
  public CronTab(String format, int line, Hash hash) { this(format, line, hash, null); }
  
  public CronTab(String format, int line, Hash hash, @CheckForNull String timezone) {
    this.bits = new long[4];
    set(format, line, hash, timezone);
  }
  
  private void set(String format, int line, Hash hash) { set(format, line, hash, null); }
  
  private void set(String format, int line, Hash hash, String timezone) {
    CrontabLexer lexer = new CrontabLexer(CharStreams.fromString(format));
    lexer.removeErrorListeners();
    lexer.addErrorListener(new JenkinsANTLRErrorListener());
    lexer.setLine(line);
    CrontabParser parser = new CrontabParser(new CommonTokenStream(lexer));
    parser.removeErrorListeners();
    Objects.requireNonNull(parser);
    parser.addErrorListener(new JenkinsANTLRErrorListener(parser::getErrorMessage));
    parser.setHash(hash);
    this.spec = format;
    this.specTimezone = timezone;
    parser.startRule(this);
    if ((this.dayOfWeek & 0x80) != 0) {
      this.dayOfWeek |= 0x1;
      this.dayOfWeek &= 0xFFFFFF7F;
    } 
  }
  
  boolean check(Calendar cal) {
    Calendar checkCal = cal;
    if (this.specTimezone != null && !this.specTimezone.isEmpty()) {
      Calendar tzCal = Calendar.getInstance(TimeZone.getTimeZone(this.specTimezone));
      tzCal.setTime(cal.getTime());
      checkCal = tzCal;
    } 
    if (!checkBits(this.bits[0], checkCal.get(12)))
      return false; 
    if (!checkBits(this.bits[1], checkCal.get(11)))
      return false; 
    if (!checkBits(this.bits[2], checkCal.get(5)))
      return false; 
    if (!checkBits(this.bits[3], checkCal.get(2) + 1))
      return false; 
    if (!checkBits(this.dayOfWeek, checkCal.get(7) - 1))
      return false; 
    return true;
  }
  
  public Calendar ceil(long t) {
    Calendar cal = new GregorianCalendar(Locale.US);
    cal.setTimeInMillis(t);
    return ceil(cal);
  }
  
  public Calendar ceil(Calendar cal) {
    Calendar twoYearsFuture = (Calendar)cal.clone();
    twoYearsFuture.add(1, 2);
    label31: while (true) {
      if (cal.compareTo(twoYearsFuture) > 0)
        throw new RareOrImpossibleDateException(); 
      for (CalendarField f : CalendarField.ADJUST_ORDER) {
        int cur = f.valueOf(cal);
        int next = f.ceil(this, cur);
        if (cur != next) {
          for (CalendarField l = f.lowerField; l != null; l = l.lowerField)
            l.clear(cal); 
          if (next < 0) {
            f.rollUp(cal, 1);
            f.setTo(cal, f.first(this));
            continue label31;
          } 
          f.setTo(cal, next);
          if (f.valueOf(cal) != next) {
            f.rollUp(cal, 1);
            f.setTo(cal, f.first(this));
            continue label31;
          } 
          if (f.redoAdjustmentIfModified)
            continue label31; 
        } 
      } 
      break;
    } 
    return cal;
  }
  
  public Calendar floor(long t) {
    Calendar cal = new GregorianCalendar(Locale.US);
    cal.setTimeInMillis(t);
    return floor(cal);
  }
  
  public Calendar floor(Calendar cal) {
    Calendar twoYearsAgo = (Calendar)cal.clone();
    twoYearsAgo.add(1, -2);
    label27: while (true) {
      if (cal.compareTo(twoYearsAgo) < 0)
        throw new RareOrImpossibleDateException(); 
      for (CalendarField f : CalendarField.ADJUST_ORDER) {
        int cur = f.valueOf(cal);
        int next = f.floor(this, cur);
        if (cur != next) {
          for (CalendarField l = f.lowerField; l != null; l = l.lowerField)
            l.clear(cal); 
          if (next < 0) {
            f.rollUp(cal, -1);
            f.setTo(cal, f.last(this));
            f.addTo(cal, 1);
            CalendarField.MINUTE.addTo(cal, -1);
            continue label27;
          } 
          f.setTo(cal, next);
          f.addTo(cal, 1);
          CalendarField.MINUTE.addTo(cal, -1);
          if (f.redoAdjustmentIfModified)
            continue label27; 
        } 
      } 
      break;
    } 
    return cal;
  }
  
  void set(String format, Hash hash) { set(format, 1, hash); }
  
  private boolean checkBits(long bitMask, int n) { return ((bitMask | 1L << n) == bitMask); }
  
  public String toString() {
    return super.toString() + "[" + super.toString() + "," + 
      toString("minute", this.bits[0]) + "," + 
      toString("hour", this.bits[1]) + "," + 
      toString("dayOfMonth", this.bits[2]) + "," + 
      toString("month", this.bits[3]) + "]";
  }
  
  private String toString(String key, long bit) { return key + "=" + key; }
  
  @CheckForNull
  public String checkSanity() {
    int i;
    label31: for (i = 0; i < 5; i++) {
      long bitMask = (i < 4) ? this.bits[i] : this.dayOfWeek;
      for (int j = BaseParser.LOWER_BOUNDS[i]; j <= BaseParser.UPPER_BOUNDS[i]; j++) {
        if (!checkBits(bitMask, j)) {
          if (i > 0)
            return Messages.CronTab_do_you_really_mean_every_minute_when_you(this.spec, "H " + this.spec.substring(this.spec.indexOf(' ') + 1)); 
          break label31;
        } 
      } 
    } 
    int daysOfMonth = 0;
    for (int i = 1; i < 31; i++) {
      if (checkBits(this.bits[2], i))
        daysOfMonth++; 
    } 
    if (daysOfMonth > 5 && daysOfMonth < 28)
      return Messages.CronTab_short_cycles_in_the_day_of_month_field_w(); 
    String hashified = hashify(this.spec);
    if (hashified != null)
      return Messages.CronTab_spread_load_evenly_by_using_rather_than_(hashified, this.spec); 
    return null;
  }
  
  @CheckForNull
  public static String hashify(String spec) {
    if (spec.contains("H"))
      return null; 
    if (spec.startsWith("*/"))
      return "H" + spec.substring(1); 
    if (spec.matches("\\d+ .+"))
      return "H " + spec.substring(spec.indexOf(' ') + 1); 
    Matcher m = Pattern.compile("0(,(\\d+)(,\\d+)*)( .+)").matcher(spec);
    if (m.matches()) {
      int period = Integer.parseInt(m.group(2));
      if (period > 0) {
        StringBuilder b = new StringBuilder();
        int i;
        for (i = period; i < 60; i += period)
          b.append(',').append(i); 
        if (b.toString().equals(m.group(1)))
          return "H/" + period + m.group(4); 
      } 
    } 
    return null;
  }
  
  @CheckForNull
  public TimeZone getTimeZone() {
    if (this.specTimezone == null)
      return null; 
    return TimeZone.getTimeZone(this.specTimezone);
  }
}
