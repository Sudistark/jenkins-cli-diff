package hudson.util;

import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class RingBufferLogHandler extends Handler {
  private static final int DEFAULT_RING_BUFFER_SIZE = Integer.getInteger(RingBufferLogHandler.class.getName() + ".defaultSize", 256).intValue();
  
  private int start;
  
  private final LogRecord[] records;
  
  private int size;
  
  @Deprecated
  public RingBufferLogHandler() { this(DEFAULT_RING_BUFFER_SIZE); }
  
  public RingBufferLogHandler(int ringSize) {
    this.start = 0;
    this.records = new LogRecord[ringSize];
  }
  
  public static int getDefaultRingBufferSize() { return DEFAULT_RING_BUFFER_SIZE; }
  
  public void publish(LogRecord record) {
    if (record == null)
      return; 
    synchronized (this) {
      int len = this.records.length;
      this.records[(this.start + this.size) % len] = record;
      if (this.size == len) {
        this.start = (this.start + 1) % len;
      } else {
        this.size++;
      } 
    } 
  }
  
  public void clear() {
    this.size = 0;
    this.start = 0;
  }
  
  public List<LogRecord> getView() { return new Object(this); }
  
  public void flush() {}
  
  public void close() {}
}
