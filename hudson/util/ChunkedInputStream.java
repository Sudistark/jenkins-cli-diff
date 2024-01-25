package hudson.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ChunkedInputStream extends InputStream {
  private InputStream in;
  
  private int chunkSize;
  
  private int pos;
  
  private boolean bof = true;
  
  private boolean eof = false;
  
  private boolean closed = false;
  
  private static final Logger LOGGER = Logger.getLogger(ChunkedInputStream.class.getName());
  
  public ChunkedInputStream(InputStream in) throws IOException {
    if (in == null)
      throw new IllegalArgumentException("InputStream parameter may not be null"); 
    this.in = in;
    this.pos = 0;
  }
  
  public int read() throws IOException {
    if (advanceChunk())
      return -1; 
    this.pos++;
    return this.in.read();
  }
  
  public int read(byte[] b, int off, int len) throws IOException {
    if (advanceChunk())
      return -1; 
    len = Math.min(len, this.chunkSize - this.pos);
    int count = this.in.read(b, off, len);
    this.pos += count;
    return count;
  }
  
  private boolean advanceChunk() throws IOException {
    if (this.closed)
      throw new IOException("Attempted read from closed stream."); 
    if (this.eof)
      return true; 
    if (this.pos >= this.chunkSize) {
      nextChunk();
      if (this.eof)
        return true; 
    } 
    return false;
  }
  
  public int read(byte[] b) throws IOException { return read(b, 0, b.length); }
  
  private void readCRLF() throws IOException {
    int cr = this.in.read();
    int lf = this.in.read();
    if (cr != 13 || lf != 10)
      throw new IOException("CRLF expected at end of chunk: " + cr + "/" + lf); 
  }
  
  private void nextChunk() throws IOException {
    if (!this.bof)
      readCRLF(); 
    this.chunkSize = getChunkSizeFromInputStream(this.in);
    this.bof = false;
    this.pos = 0;
    if (this.chunkSize == 0) {
      this.eof = true;
      parseTrailerHeaders();
    } 
  }
  
  private static int getChunkSizeFromInputStream(InputStream in) throws IOException {
    int result;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int state = 0;
    while (state != -1) {
      int b = in.read();
      if (b == -1)
        throw new IOException("chunked stream ended unexpectedly"); 
      switch (state) {
        case 0:
          switch (b) {
            case 13:
              state = 1;
              continue;
            case 34:
              state = 2;
              break;
          } 
          baos.write(b);
          continue;
        case 1:
          if (b == 10) {
            state = -1;
            continue;
          } 
          throw new IOException("Protocol violation: Unexpected single newline character in chunk size");
        case 2:
          switch (b) {
            case 92:
              b = in.read();
              baos.write(b);
              continue;
            case 34:
              state = 0;
              break;
          } 
          baos.write(b);
          continue;
      } 
      throw new RuntimeException("assertion failed");
    } 
    String dataString = baos.toString(StandardCharsets.US_ASCII);
    int separator = dataString.indexOf(';');
    dataString = (separator > 0) ? dataString.substring(0, separator).trim() : dataString.trim();
    try {
      result = Integer.parseInt(dataString.trim(), 16);
    } catch (NumberFormatException e) {
      throw new IOException("Bad chunk size: " + dataString, e);
    } 
    return result;
  }
  
  private void parseTrailerHeaders() throws IOException { readCRLF(); }
  
  public void close() throws IOException {
    if (!this.closed)
      try {
        if (!this.eof)
          exhaustInputStream(this); 
      } finally {
        this.eof = true;
        this.closed = true;
      }  
  }
  
  static void exhaustInputStream(InputStream inStream) throws IOException {
    byte[] buffer = new byte[1024];
    while (inStream.read(buffer) >= 0);
  }
}
